package com.google.appinventor.server.storage.database.datastore;

import com.android.tools.r8.C.a.a.E;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.storage.ErrorUtils;
import com.google.appinventor.server.storage.FileDataRoleEnum;
import com.google.appinventor.server.storage.ObjectifyStorageIo;
import com.google.appinventor.server.storage.UnauthorizedAccessException;
import com.google.appinventor.server.storage.UnifiedFile;
import com.google.appinventor.server.storage.database.DatabaseAccessException;
import com.google.appinventor.server.storage.database.DatabaseService;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class ProviderDatastoreAppEngine extends DatabaseService {
  private static final Logger LOG = Logger.getLogger(ProviderDatastoreAppEngine.class.getName());

  // TODO(user): need a way to modify this. Also, what is really a good value?
  private static final int MAX_JOB_RETRIES = 10;

  public ProviderDatastoreAppEngine() {
    ObjectifyService.register(StoredData.UserData.class);
    ObjectifyService.register(StoredData.ProjectData.class);
    ObjectifyService.register(StoredData.UserProjectData.class);
    ObjectifyService.register(StoredData.FileData.class);
    ObjectifyService.register(StoredData.UserFileData.class);
    ObjectifyService.register(StoredData.RendezvousData.class);
    ObjectifyService.register(StoredData.WhiteListData.class);
    ObjectifyService.register(StoredData.FeedbackData.class);
    ObjectifyService.register(StoredData.NonceData.class);
    ObjectifyService.register(StoredData.CorruptionRecord.class);
    ObjectifyService.register(StoredData.PWData.class);
    ObjectifyService.register(StoredData.SplashData.class);
    ObjectifyService.register(StoredData.Backpack.class);
    ObjectifyService.register(StoredData.AllowedTutorialUrls.class);
    ObjectifyService.register(StoredData.AllowedIosExtensions.class);
  }

  @Override
  public User findOrCreateUser(final String userId, final String email, final boolean requireTos) {
    final AtomicReference<StoredData.UserData> finalUserData = new AtomicReference<>();

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserData userData = datastore.find(userKey(userId));
          boolean viaemail = false; // Which datastore copy did we find it with...
          Objectify qDatastore = null;
          if (userData == null) { // Attempt to find them by email
            LOG.info("Did not find userId " + userId);
            if (email != null) {
              qDatastore = ObjectifyService.begin(); // Need an instance not in this transaction
              userData = qDatastore.query(StoredData.UserData.class).filter("email", email).get();
              if (userData == null) { // Still null!
                userData = qDatastore.query(StoredData.UserData.class).filter("emaillower", email.toLowerCase()).get();
              }

              // Need to fix userId...
              if (userData != null) {
                LOG.info("Found based on email, userData.id = " + userData.id);
                viaemail = true;
              }
            }
            if (userData == null) { // No joy, create it.
              userData = createUser(datastore, userId, email);
            }
          } else if (email != null && !email.equals(userData.email)) {
            userData.email = email;
            userData.emaillower = email.toLowerCase();
            datastore.put(userData);
          }

          // Add emaillower if it isn't already there
          if (userData.emaillower == null) {
            userData.emaillower = userData.email.toLowerCase();
            if (viaemail) {
              qDatastore.put(userData);
            } else {
              datastore.put(userData);
            }
          }

          finalUserData.set(userData);
        }
      }, false);                // Transaction not needed. If we fail there is nothing to rollback
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }


    final User user = new User(userId, email, false, false, null);
    final StoredData.UserData userData = finalUserData.get();
    user.setUserId(userData.id);
    user.setUserEmail(userData.email);
    user.setUserTosAccepted(userData.tosAccepted || !requireTos);
    user.setIsAdmin(userData.isAdmin);
    user.setSessionId(userData.sessionid);
    user.setPassword(userData.password);

    return user;
  }

  private StoredData.UserData createUser(Objectify datastore, String userId, String email) {
    String emaillower = null;
    if (email != null) {
      emaillower = email.toLowerCase();
    }
    StoredData.UserData userData = new StoredData.UserData();
    userData.id = userId;
    userData.tosAccepted = false;
    userData.settings = "";
    userData.email = email == null ? "" : email;
    userData.emaillower = email == null ? "" : emaillower;
    datastore.put(userData);
    return userData;
  }

  @Override
  public User getUserFromEmail(final String email) {
    Objectify datastore = ObjectifyService.begin();
    String newId = UUID.randomUUID().toString();
    // First try lookup using entered case (which will be the case for Google Accounts)
    StoredData.UserData user = datastore.query(StoredData.UserData.class).filter("email", email).get();
    if (user == null) {
      LOG.info("getUserFromEmail: first attempt failed using " + email);
      // Now try lower case version
      user = datastore.query(StoredData.UserData.class).filter("emaillower", email).get();
      if (user == null) {       // Finally, create it (in lower case)
        LOG.info("getUserFromEmail: second attempt failed using " + email);
        user = createUser(datastore, newId, email);
      }
    }

    User retUser = new User(user.id, email, user.tosAccepted, false, user.sessionid);
    retUser.setPassword(user.password);
    return retUser;
  }

  @Override
  public void setTosAccepted(final String userId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.tosAccepted = true;
            datastore.put(userData);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserEmail(final String userId, final String email) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.email = email;
            datastore.put(userData);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserSessionId(final String userId, final String sessionId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.sessionid = sessionId;
            datastore.put(userData);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserPassword(final String userId, final String password) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.password = password;
            datastore.put(userData);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public String loadUserDataSettings(final String userId) {
    final AtomicReference<String> settings = new AtomicReference<>("");

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserData userData = datastore.find(StoredData.UserData.class, userId);
          if (userData != null) {
            settings.set(userData.settings);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }

    return settings.get();
  }

  @Override
  public void storeUserDataSettings(final String userId, final String settings) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.settings = settings;
            userData.visited = new Date(); // Indicate that this person was active now
            datastore.put(userData);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public Long createProjectData(final Project project, final String projectSettings) throws DatabaseAccessException {
    final AtomicReference<Long> projectId = new AtomicReference<>(null);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          long date = System.currentTimeMillis();
          StoredData.ProjectData pd = new StoredData.ProjectData();
          pd.id = null;  // let Objectify auto-generate the project id
          pd.dateCreated = date;
          pd.dateModified = date;
          pd.dateBuilt = 0;
          pd.history = project.getProjectHistory();
          pd.name = project.getProjectName();
          pd.settings = projectSettings;
          pd.type = project.getProjectType();
          datastore.put(pd); // put the project in the db so that it gets assigned an id

          assert pd.id != null;
          projectId.set(pd.id);
        }

      }, false);
    } catch (ObjectifyException e) {
      throw new DatabaseAccessException(e);
    }

    return projectId.get();
  }

  @Override
  public void createUserProjectData(final String userId, final Long projectId, final String projectSettings) throws DatabaseAccessException {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserProjectData upd = new StoredData.UserProjectData();
          upd.projectId = projectId;
          upd.settings = projectSettings;
          upd.state = StoredData.UserProjectData.StateEnum.OPEN;
          upd.userKey = userKey(userId);
          datastore.put(upd);
        }
      }, false);
    } catch (ObjectifyException e) {
      throw new DatabaseAccessException(e);
    }
  }

  @Override
  public void createProjectFileData(final String userId, final Long projectId, final FileDataRoleEnum role,
                                    final List<UnifiedFile> files) throws DatabaseAccessException {
    final List<StoredData.FileData> addedFiles = new ArrayList<>();

    for (UnifiedFile unifiedFile : files) {
      final Key<StoredData.ProjectData> projectKey = projectKey(projectId);

      StoredData.FileData file = new StoredData.FileData();
      file.fileName = unifiedFile.getFileName();
      file.projectKey = projectKey;
      file.role = role;
      file.userId = userId;
      if (unifiedFile.getFilesystemName() != null) {
        file.isGCS = true;
        file.gcsName = unifiedFile.getFilesystemName();
      } else {
        file.content = unifiedFile.getContent();
      }

      addedFiles.add(file);
    }

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          datastore.put(addedFiles);
        }
      }, true);
    } catch (ObjectifyException e) {
      throw new DatabaseAccessException(e);
    }
  }

  public void deleteUserProject(final String userId, final Long projectId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          // delete the UserProjectData object
          Key<StoredData.UserData> userKey = userKey(userId);
          datastore.delete(userProjectKey(userKey, projectId));
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
  }

  public List<String> deleteProjectData(final String userId, final Long projectId) {
    // blobs associated with the project
    final List<String> blobKeys = new ArrayList<>();
    final List<String> gcsPaths = new ArrayList<>();

    try {
      // entity group
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<StoredData.ProjectData> projectKey = projectKey(projectId);
          Query<StoredData.FileData> fdq = datastore.query(StoredData.FileData.class).ancestor(projectKey);
          for (StoredData.FileData fd: fdq) {
            if (isTrue(fd.isGCS)) {
              gcsPaths.add(fd.gcsName);
            } else if (fd.isBlob) {
              blobKeys.add(fd.blobKey);
            }
          }
          datastore.delete(fdq);
          // finally, delete the ProjectData object
          datastore.delete(projectKey);
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }

    // have to delete the blobs outside of the user and project jobs
    for (String blobKeyString: blobKeys) {
      deleteBlobstoreFile(blobKeyString);
    }

    // We send the gcs paths to be deleted by the filesystem service
    return gcsPaths;
  }


  private void deleteBlobstoreFile(String blobKeyString) {
    // It would be nice if there were an AppEngineFile.delete() method but alas there isn't, so we
    // have to get the BlobKey and delete via the BlobstoreService.
    BlobKey blobKey = null;
    try {
      blobKey = new BlobKey(blobKeyString);
      BlobstoreServiceFactory.getBlobstoreService().delete(blobKey);
    } catch (RuntimeException e) {
      // Log blob delete errors but don't make them fatal
      throw CrashReport.createAndLogError(LOG, null, "Error deleting blob with blobKey " +
          blobKey, e);
    }
  }

  @Override
  public void setProjectMovedToTrashFlag(final String userId, final long projectId, final boolean flag) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData projectData = datastore.find(projectKey(projectId));
          if (projectData != null) {
            projectData.projectMovedToTrashFlag = flag;
            datastore.put(projectData);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public List<Long> getProjectIdsByUser(final String userId) {
    final List<Long> projects = new ArrayList<>();

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<StoredData.UserData> userKey = userKey(userId);
          for (StoredData.UserProjectData upd : datastore.query(StoredData.UserProjectData.class).ancestor(userKey)) {
            projects.add(upd.projectId);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }

    return projects;
  }

  @Override
  public String getProjectSettings(final String userId, final long projectId) {
    final AtomicReference<String> settings = new AtomicReference<>("");

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            settings.set(pd.settings);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }

    return settings.get();
  }

  @Override
  public void setProjectSettings(final String userId, final long projectId, final String projectSettings) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            pd.settings = projectSettings;
            datastore.put(pd);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
  }

  @Override
  public UserProject getUserProject(final String userId, final long projectId) {
    final AtomicReference<StoredData.ProjectData> projectData = new AtomicReference<>(null);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            projectData.set(pd);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }

    final StoredData.ProjectData projectDataVal = projectData.get();
    if (projectDataVal == null) {
      return null;
    }

    return mapProjectDataToUserProject(projectId, projectDataVal);
  }

  @Override
  public List<UserProject> getUserProjects(final String userId, final List<Long> projectIds) {
    final AtomicReference<Map<Long, StoredData.ProjectData>> projectDatas = new AtomicReference<>(null);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Map<Long, StoredData.ProjectData> pd = datastore.get(StoredData.ProjectData.class, projectIds);
          if (pd != null) {
            projectDatas.set(pd);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserErrorInfo(userId), e);
    }

    final Map<Long, StoredData.ProjectData> projectDatasVal = projectDatas.get();
    if (projectDatasVal == null) {
      throw new RuntimeException("getUserProjects wants to return null, userId = " + userId);
      // Note we directly throw a RuntimeException instead of calling CrashReport
      // because we don't have an explicitly caught exception to hand it.
    }

    List<UserProject> uProjects = new ArrayList<>();
    for (StoredData.ProjectData projectData : projectDatasVal.values()) {
      uProjects.add(mapProjectDataToUserProject(projectData.id, projectData));
    }

    return uProjects;
  }

  @Override
  public String getProjectName(final String userId, final long projectId) {
    final AtomicReference<String> projectName = new AtomicReference<>("");

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            projectName.set(pd.name);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }

    return projectName.get();
  }

  @Override
  public Long getProjectDateModified(final String userId, final long projectId) {
    final AtomicReference<Long> modDate = new AtomicReference<>(0L);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            modDate.set(pd.dateModified);
          }
        }
      }, false); // Transaction not needed, and we want the caching we get if we don't
      // use them.
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return modDate.get();
  }

  @Override
  public Long getProjectDateBuilt(final String userId, final long projectId) {
    final AtomicReference<Long> builtDate = new AtomicReference<>(0L);
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            builtDate.set(pd.dateBuilt);
          }
        }
      }, false); // Transaction not needed, and we want the caching we get if we don't
      // use them.
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return builtDate.get();
  }

  @Override
  public void setProjectBuiltDate(final String userId, final long projectId, final long builtDate) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            pd.dateBuilt = builtDate;
            datastore.put(pd);
          }
        }
      }, false); // Transaction not needed, and we want the caching we get if we don't
      // use them.
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
  }

  @Override
  public String getProjectHistory(final String userId, final long projectId) {
    final AtomicReference<String> projectHistory = new AtomicReference<>("");
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            projectHistory.set(pd.history);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return projectHistory.get();
  }

  @Override
  public Long getProjectDateCreated(final String userId, final long projectId) {
    final AtomicReference<Long> dateCreated = new AtomicReference<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            dateCreated.set(pd.dateCreated);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return dateCreated.get();
  }

  @Override
  public void createUserFileData(final String userId, final String fileName) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<StoredData.UserData> userKey = userKey(userId);
          StoredData.UserFileData ufd = createUserFile(datastore, userKey, fileName);
          if (ufd != null) {
            datastore.put(ufd);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserErrorInfo(userId, fileName), e);
    }
  }

  /**
   * Creates a UserFileData object for the given userKey and fileName, if it
   * doesn't already exist. Returns the new UserFileData object, or null if
   * already existed. This method does not add the UserFileData object to the
   * datastore.
   */
  private StoredData.UserFileData createUserFile(Objectify datastore, Key<StoredData.UserData> userKey, String fileName) {
    StoredData.UserFileData ufd = datastore.find(userFileKey(userKey, fileName));
    if (ufd == null) {
      ufd = new StoredData.UserFileData();
      ufd.fileName = fileName;
      ufd.userKey = userKey;
      return ufd;
    }

    return null;
  }

  @Override
  public List<String> getUserFileNames(final String userId) {
    final List<String> fileList = new ArrayList<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<StoredData.UserData> userKey = userKey(userId);
          for (StoredData.UserFileData ufd : datastore.query(StoredData.UserFileData.class).ancestor(userKey)) {
            fileList.add(ufd.fileName);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
    return fileList;
  }

  @Override
  public void uploadUserFile(final String userId, final String fileName, final byte[] content) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserFileData ufd = datastore.find(userFileKey(userKey(userId), fileName));
          /*
           * We look for the UserFileData object for the given userId and fileName.
           * If it doesn't exit, we create it.
           *
           * SPECIAL CASE: If fileName == StorageUtil.USER_BACKBACK_FILENAME and the
           * content is "[]", we *delete* the file because the default value returned
           * if the file doesn't exist is "[]" (the JSON empty list). This is to reduce
           * the clutter of files for the case where someone doesn't have anything in
           * the backpack. We pay $$ for storage.
           */
          byte [] empty = new byte[] { (byte)0x5b, (byte)0x5d }; // "[]" in bytes
          if (ufd == null) {          // File doesn't exist
            if (fileName.equals(StorageUtil.USER_BACKPACK_FILENAME) &&
                Arrays.equals(empty, content)) {
              return;                 // Nothing to do
            }
            ufd = new StoredData.UserFileData();
            ufd.fileName = fileName;
            ufd.userKey = userKey(userId);
          } else {
            if (fileName.equals(StorageUtil.USER_BACKPACK_FILENAME) &&
                Arrays.equals(empty, content)) {
              // Storing an empty backback, just delete the file
              datastore.delete(userFileKey(userKey(userId), fileName));
              return;
            }
          }
          ufd.content = content;
          datastore.put(ufd);
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId, fileName), e);
    }
  }

  @Override
  public byte[] getUserFile(final String userId, final String fileName) {
    final AtomicReference<byte[]> result = new AtomicReference<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserFileData ufd = datastore.find(userFileKey(userKey(userId), fileName));
          if (ufd != null) {
            result.set(ufd.content);
          } else {
            throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId, fileName),
                new FileNotFoundException(fileName));
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId, fileName), e);
    }
    return result.get();
  }

  @Override
  public void deleteUserFile(final String userId, final String fileName) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<StoredData.UserFileData> ufdKey = userFileKey(userKey(userId), fileName);
          if (datastore.find(ufdKey) != null) {
            datastore.delete(ufdKey);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId, fileName), e);
    }
  }

  @Override
  public void addFileToProject(final String userId, final Long projectId, final FileDataRoleEnum role,
                               final boolean changeModDate, final String fileName) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<StoredData.ProjectData> projectKey = projectKey(projectId);
          StoredData.FileData fd = createProjectFile(datastore, projectKey, role, fileName);
          fd.userId = userId;
          datastore.put(fd);
          if (changeModDate) {
            updateProjectModDate(datastore, projectId);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
    }
  }

  private StoredData.FileData createProjectFile(Objectify datastore, Key<StoredData.ProjectData> projectKey,
                                                                                      FileDataRoleEnum role, String fileName) {
    StoredData.FileData fd = datastore.find(projectFileKey(projectKey, fileName));
    if (fd == null) {
      fd = new StoredData.FileData();
      fd.fileName = fileName;
      fd.projectKey = projectKey;
      fd.role = role;
    } else if (!fd.role.equals(role)) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(null, projectKey.getId(), fileName),
          new IllegalStateException("File role change is not supported"));
    }

    return fd;
  }

  private Long updateProjectModDate(Objectify datastore, long projectId) {
    long modDate = System.currentTimeMillis();
    StoredData.ProjectData pd = datastore.find(projectKey(projectId));
    if (pd != null) {
      // Only update the ProjectData dateModified if it is more then a minute
      // in the future. Do this to avoid unnecessary datastore puts.
      if (modDate > (pd.dateModified + 1000*60)) {
        pd.dateModified = modDate;
        datastore.put(pd);
      } else {
        // return the (old) dateModified
        modDate = pd.dateModified;
      }
      return modDate;
    } else {
      throw CrashReport.createAndLogError(LOG, null, null,
          new IllegalArgumentException("project " + projectId + " doesn't exist"));
    }
  }

  @Override
  public void removeFileFromProject(final String userId, final Long projectId, final FileDataRoleEnum role,
                                    final boolean changeModDate, final String fileName) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<StoredData.ProjectData> projectKey = projectKey(projectId);
          Key<StoredData.FileData> key = projectFileKey(projectKey, fileName);
          StoredData.FileData fd = datastore.find(key);
          if (fd != null) {
            if (fd.role.equals(role)) {
              datastore.delete(projectFileKey(projectKey, fileName));
            } else {
              throw CrashReport.createAndLogError(LOG, null,
                  ErrorUtils.collectProjectErrorInfo(null, projectId, fileName),
                  new IllegalStateException("File role change is not supported"));
            }
          }

          if (changeModDate) {
            updateProjectModDate(datastore, projectId);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
    }
  }

  @Override
  public List<String> getProjectFiles(final String userId, final long projectId, FileDataRoleEnum role) {
    final List<String> fileList = new ArrayList<>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<StoredData.ProjectData> projectKey = projectKey(projectId);
          for (StoredData.FileData fd : datastore.query(StoredData.FileData.class).ancestor(projectKey)) {
            if (fd.role.equals(role)) {
              fileList.add(fd.fileName);
            }
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectUserProjectErrorInfo(userId, projectId), e);
    }
    return fileList;
  }

  @Override
  public UploadProjectFileResult uploadProjectFile(final String userId, final long projectId, final String fileName,
                            final boolean force, final byte[] content, final Long backupThreshold, final String filesystemName) throws BlocksTruncatedException {
    final UploadProjectFileResult uploadProjectFileResult = new UploadProjectFileResult();

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) throws ObjectifyException {
          StoredData.FileData fd = datastore.find(projectFileKey(projectKey(projectId), fileName));

          // <Screen>.yail files are missing when user converts AI1 project to AI2
          // instead of blowing up, just create a <Screen>.yail file
          if (fd == null && (fileName.endsWith(".yail") || (fileName.endsWith(".png")))){
            fd = createProjectFile(datastore, projectKey(projectId), FileDataRoleEnum.SOURCE, fileName);
            fd.userId = userId;
          }

          Preconditions.checkState(fd != null);

          if (fd.userId != null && !fd.userId.isEmpty()) {
            if (!fd.userId.equals(userId)) {
              throw CrashReport.createAndLogError(LOG, null,
                  ErrorUtils.collectUserProjectErrorInfo(userId, projectId),
                  new UnauthorizedAccessException(userId, projectId, null));
            }
          }

          if ((content.length < 125) && (fileName.endsWith(".bky"))) { // Likely this is an empty blocks workspace
            if (!force) {            // force is true if we *really* want to save it!
              checkForBlocksTruncation(fd); // See if we had previous content and throw and exception if so
            }
          }

          if (filesystemName != null) {
            fd.isGCS = true;
            fd.gcsName = filesystemName;
            uploadProjectFileResult.fileRole = fd.role;
            // If the content was previously stored in the datastore, clear it out.
            fd.content = null;
            fd.isBlob = false;  // in case we are converting from a blob
            fd.blobstorePath = null;
          } else {
            if (isTrue(fd.isGCS)) {     // Was a GCS file, must have gotten smaller
              uploadProjectFileResult.needsFilesystemDelete = true;
              fd.isGCS = false;
              fd.gcsName = null;
            }
            // Note, Don't have to do anything if the file was in the
            // Blobstore and shrank because the code above (3 lines
            // into the function) already handles removing the old
            // contents from the Blobstore.
            fd.isBlob = false;
            fd.blobstorePath = null;
            fd.content = content;
          }

          if (backupThreshold != null) {
            if ((fd.lastBackup + backupThreshold) < System.currentTimeMillis()) {
              uploadProjectFileResult.shouldDoFilesystemBackup = true;
              fd.lastBackup = System.currentTimeMillis();
            }
          }

          // Old file not marked with ownership, mark it now
          if (fd.userId == null || fd.userId.isEmpty()) {
            fd.userId = userId;
          }
          datastore.put(fd);
          uploadProjectFileResult.lastModifiedDate = updateProjectModDate(datastore, projectId);
        }
      }, false); // Use transaction for blobstore, otherwise we don't need one
      // and without one the caching code comes into play.
    } catch (ObjectifyException e) {
      if (e.getMessage().startsWith("Blocks")) { // Convert Exception
        throw new BlocksTruncatedException();
      }
      throw CrashReport.createAndLogError(LOG, null,
          ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
    }

    return uploadProjectFileResult;
  }

  // We are called when our caller detects we are about to write a trivial (empty)
  // workspace. We check to see if previously the workspace was non-trivial and
  // if so, throw the BlocksTruncatedException. This will be passed through the RPC
  // layer to the client code which will put up a dialog box for the user to review
  // See Ode.java for more information
  private void checkForBlocksTruncation(StoredData.FileData fd) throws ObjectifyException {
    if (fd.isBlob || isTrue(fd.isGCS) || fd.content.length > 120)
      throw new ObjectifyException("BlocksTruncated"); // Hack
    // I'm avoiding having to modify every use of runJobWithRetries to handle a new
    // exception, so we use this dodge.
  }

  @Override
  public boolean assertUserIdOwnerOfProject(final String userId, final long projectId) {
    final AtomicReference<Boolean> ownsProject = new AtomicReference<>(false);
    try {
      runJobWithRetries(new JobRetryHelper() {
        @SuppressWarnings("RedundantThrows")
        @Override
        public void run(Objectify datastore) throws ObjectifyException, IOException {
          Key<StoredData.UserData> userKey = userKey(userId);
          Key<StoredData.UserProjectData> userProjectKey = userProjectKey(userKey, projectId);
          StoredData.UserProjectData data = datastore.find(userProjectKey);
          if (data != null) {  // User doesn't have the corresponding project.
            ownsProject.set(true);
          }
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }

    return ownsProject.get();
  }

  @VisibleForTesting
  private abstract static class JobRetryHelper {
    private IOException exception = null;

    public abstract void run(Objectify datastore) throws ObjectifyException, IOException;

    /*
     * Called before retrying the job. Note that the underlying datastore
     * still has the transaction active, so restrictions about operations
     * over multiple entity groups still apply.
     */
    public void onNonFatalError() {
      // Default is to do nothing
    }

    public void onIOException(IOException error) {
      exception = error;
    }

    public IOException getIOException() {
      return exception;
    }
  }

  @VisibleForTesting
  private void runJobWithRetries(JobRetryHelper job, boolean useTransaction) throws ObjectifyException {
    int tries = 0;
    while (tries <= MAX_JOB_RETRIES) {
      Objectify datastore;
      if (useTransaction) {
        datastore = ObjectifyService.beginTransaction();
      } else {
        datastore = ObjectifyService.begin();
      }
      try {
        job.run(datastore);
        if (useTransaction) {
          datastore.getTxn().commit();
        }
        break;
      } catch (ConcurrentModificationException ex) {
        job.onNonFatalError();
        LOG.log(Level.WARNING, "Optimistic concurrency failure", ex);
      } catch (ObjectifyException oe) {
        String message = oe.getMessage();
        if (message != null &&
            (message.startsWith("Blocks") || message.startsWith("User Al"))) { // This one is fatal!
          throw oe;
        }
        // maybe this should be a fatal error? I think only thing
        // that creates this exception is this method.
        job.onNonFatalError();
      } catch (IOException e) {
        job.onIOException(e);
        break;
      } finally {
        if (useTransaction && datastore.getTxn().isActive()) {
          try {
            datastore.getTxn().rollback();
          } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Transaction rollback failed", e);
          }
        }
      }
      tries++;
    }
    if (tries > MAX_JOB_RETRIES) {
      throw new ObjectifyException("Couldn't commit job after max retries.");
    }
  }

  private UserProject mapProjectDataToUserProject(final Long projectId, final StoredData.ProjectData projectData) {
    return new UserProject(projectId, projectData.name,
        projectData.type, projectData.dateCreated,
        projectData.dateModified, projectData.dateBuilt, projectData.projectMovedToTrashFlag);
  }

  private Key<StoredData.UserData> userKey(String userId) {
    return new Key<>(StoredData.UserData.class, userId);
  }

  private Key<StoredData.UserProjectData> userProjectKey(Key<StoredData.UserData> userKey, long projectId) {
    return new Key<>(userKey, StoredData.UserProjectData.class, projectId);
  }

  private Key<StoredData.UserFileData> userFileKey(Key<StoredData.UserData> userKey, String fileName) {
    return new Key<>(userKey, StoredData.UserFileData.class, fileName);
  }

  private Key<StoredData.ProjectData> projectKey(long projectId) {
    return new Key<>(StoredData.ProjectData.class, projectId);
  }

  private Key<StoredData.FileData> projectFileKey(Key<StoredData.ProjectData> projectKey, String fileName) {
    return new Key<>(projectKey, StoredData.FileData.class, fileName);
  }

  private boolean isTrue(Boolean b) {
    return b != null && b;
  }
}
