// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.storage;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Expiration;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StoredData.CorruptionRecord;
import com.google.appinventor.server.storage.StoredData.FeedbackData;
import com.google.appinventor.server.storage.StoredData.FileData;
import com.google.appinventor.server.storage.StoredData.MotdData;
import com.google.appinventor.server.storage.StoredData.NonceData;
import com.google.appinventor.server.storage.StoredData.ProjectData;
import com.google.appinventor.server.storage.StoredData.UserData;
import com.google.appinventor.server.storage.StoredData.UserFileData;
import com.google.appinventor.server.storage.StoredData.UserProjectData;
import com.google.appinventor.server.storage.StoredData.RendezvousData;
import com.google.appinventor.server.storage.StoredData.WhiteListData;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.Motd;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

import java.io.ByteArrayOutputStream;

// GCS imports
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Date;

import javax.annotation.Nullable;

/**
 * Implements the StorageIo interface using Objectify as the underlying data
 * store.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class ObjectifyStorageIo implements  StorageIo {
  static final Flag<Boolean> requireTos = Flag.createFlag("require.tos", false);

  private static final Logger LOG = Logger.getLogger(ObjectifyStorageIo.class.getName());

  private static final String DEFAULT_ENCODING = "UTF-8";

  private static final long MOTD_ID = 1;

  // TODO(user): need a way to modify this. Also, what is really a good value?
  private static final int MAX_JOB_RETRIES = 10;

  private final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();

  private final GcsService gcsService =
    GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());

  private final String GCS_BUCKET_NAME = Flag.createFlag("gcs.bucket", "").get();

  private static final long TWENTYFOURHOURS = 24*3600*1000; // 24 hours in milliseconds

  private final boolean useGcs = Flag.createFlag("use.gcs", false).get();


  // Use this class to define the work of a job that can be retried. The
  // "datastore" argument to run() is the Objectify object for this job
  // (created with ObjectifyService.beginTransaction()). Note that all operations
  // on "datastore" should be for objects in the same entity group.
  @VisibleForTesting
  abstract class JobRetryHelper {
    public abstract void run(Objectify datastore) throws ObjectifyException;
    /*
     * Called before retrying the job. Note that the underlying datastore
     * still has the transaction active, so restrictions about operations
     * over multiple entity groups still apply.
     */
    public void onNonFatalError() {
      // Default is to do nothing
    }
  }

  // Create a final object of this class to hold a modifiable result value that
  // can be used in a method of an inner class.
  private class Result<T> {
    T t;
  }

  private FileService fileService;

  static {
    // Register the data object classes stored in the database
    ObjectifyService.register(UserData.class);
    ObjectifyService.register(ProjectData.class);
    ObjectifyService.register(UserProjectData.class);
    ObjectifyService.register(FileData.class);
    ObjectifyService.register(UserFileData.class);
    ObjectifyService.register(MotdData.class);
    ObjectifyService.register(RendezvousData.class);
    ObjectifyService.register(WhiteListData.class);
    ObjectifyService.register(FeedbackData.class);
    ObjectifyService.register(NonceData.class);
    ObjectifyService.register(CorruptionRecord.class);
  }

  ObjectifyStorageIo() {
    fileService = FileServiceFactory.getFileService();
    memcache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    initMotd();
  }

  // for testing
  ObjectifyStorageIo(FileService fileService) {
    this.fileService = fileService;
    initMotd();
  }

  @Override
  public User getUser(String userId) {
    return getUser(userId, null);
  }

  /*
   * Note that the User returned by this method will always have isAdmin set to
   * false. We leave it to the caller to determine whether the user has admin
   * priviledges.
   */
  @Override
  public User getUser(final String userId, final String email) {
    String cachekey = User.usercachekey + "|" + userId;
    User tuser = (User) memcache.get(cachekey);
    if (tuser != null && tuser.getUserTosAccepted() && ((email == null) || (tuser.getUserEmail().equals(email)))) {
      return tuser;
    } else {                    // If not in memcache, or tos
                                // not yet accepted, fetch from datastore
      tuser = new User(userId, email, false, false, null);
    }
    final User user = tuser;
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(userKey(userId));
          if (userData == null) {
            userData = createUser(datastore, userId, email);
          } else if (email != null && !email.equals(userData.email)) {
            userData.email = email;
            datastore.put(userData);
          }
          user.setUserEmail(userData.email);
          user.setUserTosAccepted(userData.tosAccepted || !requireTos.get());
          user.setSessionId(userData.sessionid);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
    memcache.put(cachekey, user, Expiration.byDeltaSeconds(60)); // Remember for one minute
    // The choice of one minute here is arbitrary. getUser() is called on every authenticated
    // RPC call to the system (out of OdeAuthFilter), so using memcache will save a significant
    // number of calls to the datastore. If someone is idle for more then a minute, it isn't
    // unreasonable to hit the datastore again. By pruning memcache ourselves, we have a
    // bit more control (maybe) of how things are flushed from memcache. Otherwise we are
    // at the whim of whatever algorithm App Engine employs now or in the future.
    return user;
  }

  private UserData createUser(Objectify datastore, String userId, String email) {
    UserData userData = new UserData();
    userData.id = userId;
    userData.tosAccepted = false;
    userData.settings = "";
    userData.email = email == null ? "" : email;
    datastore.put(userData);
    return userData;
  }

  @Override
  public void setTosAccepted(final String userId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.tosAccepted = true;
            datastore.put(userData);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserEmail(final String userId, final String email) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.email = email;
            datastore.put(userData);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserSessionId(final String userId, final String sessionId) {
    String cachekey = User.usercachekey + "|" + userId;
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.sessionid = sessionId;
            datastore.put(userData);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
    memcache.delete(cachekey);  // Flush cached copy because it changed
  }

  @Override
  public String loadSettings(final String userId) {
    final Result<String> settings = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(UserData.class, userId);
          if (userData != null) {
            settings.t = userData.settings;
          } else {
            settings.t = "";
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
    return settings.t;
  }

  @Override
  public void storeSettings(final String userId, final String settings) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.settings = settings;
            userData.visited = new Date(); // Indicate that this person was active now
            datastore.put(userData);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public long createProject(final String userId, final Project project,
      final String projectSettings) {
    final Result<Long> projectId = new Result<Long>();
    final List<String> blobsToDelete = new ArrayList<String>();
    final List<FileData> addedFiles = new ArrayList<FileData>();

    try {
      // first job is on the project entity, creating the ProjectData object
      // and the associated files.
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) throws ObjectifyException {
          long date = System.currentTimeMillis();
          ProjectData pd = new ProjectData();
          pd.id = null;  // let Objectify auto-generate the project id
          pd.dateCreated = date;
          pd.dateModified = date;
          pd.history = project.getProjectHistory();
          pd.name = project.getProjectName();
          pd.settings = projectSettings;
          pd.type = project.getProjectType();
          datastore.put(pd); // put the project in the db so that it gets assigned an id

          assert pd.id != null;
          projectId.t = pd.id;
          // After the job commits projectId.t should end up with the last value
          // we've gotten for pd.id (i.e. the one that committed if there
          // was no error).
          // Note that while we cannot expect to read back a value that we've
          // written in this job, reading the assigned id from pd should work.

          Key<ProjectData> projectKey = projectKey(projectId.t);
          for (TextFile file : project.getSourceFiles()) {
            try {
              addedFiles.add(createRawFile(projectKey, FileData.RoleEnum.SOURCE,
                  file.getFileName(), file.getContent().getBytes(DEFAULT_ENCODING)));
            } catch (BlobWriteException e) {
              rememberBlobsToDelete();
              // Note that this makes the BlobWriteException fatal. The job will
              // not be retried if we get this exception.
              throw CrashReport.createAndLogError(LOG, null,
                collectProjectErrorInfo(userId, projectId.t, file.getFileName()), e);
            } catch (IOException e) { // GCS throws this
              throw CrashReport.createAndLogError(LOG, null,
                collectProjectErrorInfo(userId, projectId.t, file.getFileName()), e);
            }
          }
          for (RawFile file : project.getRawSourceFiles()) {
            try {
              addedFiles.add(createRawFile(projectKey, FileData.RoleEnum.SOURCE, file.getFileName(),
                  file.getContent()));
            } catch (BlobWriteException e) {
              rememberBlobsToDelete();
              // Note that this makes the BlobWriteException fatal. The job will
              // not be retried if we get this exception.
              throw CrashReport.createAndLogError(LOG, null,
                  collectProjectErrorInfo(userId, projectId.t, file.getFileName()), e);
            } catch (IOException e) {
              throw CrashReport.createAndLogError(LOG, null,
                collectProjectErrorInfo(userId, projectId.t, file.getFileName()), e);
            }
          }
          datastore.put(addedFiles);  // batch put
        }

        @Override
        public void onNonFatalError() {
          rememberBlobsToDelete();
        }

        private void rememberBlobsToDelete() {
          for (FileData addedFile : addedFiles) {
            if (addedFile.isBlob && addedFile.blobstorePath != null) {
              blobsToDelete.add(addedFile.blobstorePath);
            }
          }
          // clear addedFiles in case we end up here more than once
          addedFiles.clear();
        }
      });

      // second job is on the user entity
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserProjectData upd = new UserProjectData();
          upd.projectId = projectId.t;
          upd.settings = projectSettings;
          upd.state = UserProjectData.StateEnum.OPEN;
          upd.userKey = userKey(userId);
          datastore.put(upd);
        }
      });
    } catch (ObjectifyException e) {
      for (FileData addedFile : addedFiles) {
        if (addedFile.isBlob && addedFile.blobstorePath != null) {
          blobsToDelete.add(addedFile.blobstorePath);
        }
      }
      // clear addedFiles in case we end up here more than once
      addedFiles.clear();
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId.t), e);
    } finally {
      // Need to delete any orphaned blobs outside of the transaction to avoid multiple entity
      // group errors. The lookup of the blob key seems to be the thing that
      // triggers the error.
      for (String blobToDelete: blobsToDelete) {
        deleteBlobstoreFile(blobToDelete);
      }
    }
    return projectId.t;
  }

  /*
   *  Creates and returns a new FileData object with the specified fields.
   *  Does not check for the existence of the object and does not update
   *  the database.
   */
  private FileData createRawFile(Key<ProjectData> projectKey, FileData.RoleEnum role,
    String fileName, byte[] content) throws BlobWriteException, ObjectifyException, IOException {
    FileData file = new FileData();
    file.fileName = fileName;
    file.projectKey = projectKey;
    file.role = role;
    if (useGCSforFile(fileName, content.length)) {
      file.isGCS = true;
      file.gcsName = makeGCSfileName(fileName, projectKey.getId());
      if (content.length > 0) { // If there is actual content
        GcsOutputChannel outputChannel =
          gcsService.createOrReplace(new GcsFilename(GCS_BUCKET_NAME, file.gcsName), GcsFileOptions.getDefaultInstance());
        outputChannel.write(ByteBuffer.wrap(content));
        outputChannel.close();
      }
    } else if (useBlobstoreForFile(fileName, content.length)) {
      file.isBlob = true;
      file.blobstorePath = uploadToBlobstore(content, makeBlobName(projectKey.getId(), fileName));
    } else {
      file.content = content;
    }
    return file;
  }

  @Override
  public void deleteProject(final String userId, final long projectId) {
    // blobs associated with the project
    final List<String> blobPaths = new ArrayList<String>();
    final List<String> gcsPaths = new ArrayList<String>();
    try {
      // first job deletes the UserProjectData in the user's entity group
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          // delete the UserProjectData object
          Key<UserData> userKey = userKey(userId);
          datastore.delete(userProjectKey(userKey, projectId));
          // delete any FileData objects associated with this project
        }
      });
      // second job deletes the project files and ProjectData in the project's
      // entity group
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<ProjectData> projectKey = projectKey(projectId);
          Query<FileData> fdq = datastore.query(FileData.class).ancestor(projectKey);
          for (FileData fd: fdq) {
            if (fd.isGCS) {
              gcsPaths.add(fd.gcsName);
            } else if (fd.isBlob) {
              blobPaths.add(fd.blobstorePath);
            }
          }
          datastore.delete(fdq);
          // finally, delete the ProjectData object
          datastore.delete(projectKey);
        }
      });
      // have to delete the blobs outside of the user and project jobs
      for (String blobPath: blobPaths) {
        deleteBlobstoreFile(blobPath);
      }
      // Now delete the gcs files
      for (String gcsName: gcsPaths) {
        try {
          gcsService.delete(new GcsFilename(GCS_BUCKET_NAME, gcsName));
        } catch (IOException e) {
          LOG.log(Level.WARNING, "Unable to delete " + gcsName + " from GCS while deleting project", e);
        }
      }
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }

  }

  @Override
  public List<Long> getProjects(final String userId) {
    final List<Long> projects = new ArrayList<Long>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<UserData> userKey = userKey(userId);
          for (UserProjectData upd : datastore.query(UserProjectData.class).ancestor(userKey)) {
            projects.add(upd.projectId);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }

    return projects;
  }

  @Override
  public String loadProjectSettings(final String userId, final long projectId) {
    if (!getProjects(userId).contains(projectId)) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId),
          new UnauthorizedAccessException(userId, projectId, null));
    }
    final Result<String> settings = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            settings.t = pd.settings;
          } else {
            settings.t = "";
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return settings.t;
  }

  @Override
  public void storeProjectSettings(final String userId, final long projectId,
      final String settings) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            pd.settings = settings;
            datastore.put(pd);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
  }

  @Override
  public String getProjectType(final String userId, final long projectId) {
//    final Result<String> projectType = new Result<String>();
//    try {
//      runJobWithRetries(new JobRetryHelper() {
//        @Override
//        public void run(Objectify datastore) {
//          ProjectData pd = datastore.find(projectKey(projectId));
//          if (pd != null) {
//            projectType.t = pd.type;
//          } else {
//            projectType.t = "";
//          }
//        }
//      });
//    } catch (ObjectifyException e) {
//      throw CrashReport.createAndLogError(LOG, null,
//          collectUserProjectErrorInfo(userId, projectId), e);
//    }
    // We only have one project type, no need to ask about it
    return YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE;
  }

  @Override
  public UserProject getUserProject(final String userId, final long projectId) {
    final Result<ProjectData> projectData = new Result<ProjectData>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            projectData.t = pd;
          } else {
            projectData.t = null;
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    if (projectData == null) {
      return null;
    } else {
      return new UserProject(projectId, projectData.t.name,
          projectData.t.type, projectData.t.dateCreated,
          projectData.t.dateModified);
    }
  }

  @Override
  public String getProjectName(final String userId, final long projectId) {
    final Result<String> projectName = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            projectName.t = pd.name;
          } else {
            projectName.t = "";
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return projectName.t;
  }

  @Override
  public long getProjectDateModified(final String userId, final long projectId) {
    final Result<Long> modDate = new Result<Long>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            modDate.t = pd.dateModified;
          } else {
            modDate.t = Long.valueOf(0);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return modDate.t;
  }

  @Override
  public String getProjectHistory(final String userId, final long projectId) {
    if (!getProjects(userId).contains(projectId)) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId),
          new UnauthorizedAccessException(userId, projectId, null));
    }
    final Result<String> projectHistory = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            projectHistory.t = pd.history;
          } else {
            projectHistory.t = "";
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return projectHistory.t;
  }

  @Override
  public void addFilesToUser(final String userId, final String... fileNames) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<UserData> userKey = userKey(userId);
          List<UserFileData> addedFiles = new ArrayList<UserFileData>();
          for (String fileName : fileNames) {
            UserFileData ufd = createUserFile(datastore, userKey, fileName);
            if (ufd != null) {
              addedFiles.add(ufd);
            }
          }
          datastore.put(addedFiles);  // batch put
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserErrorInfo(userId, fileNames[0]), e);
    }
  }

  /*
   * Creates a UserFileData object for the given userKey and fileName, if it
   * doesn't already exist. Returns the new UserFileData object, or null if
   * already existed. This method does not add the UserFileData object to the
   * datastore.
   */
  private UserFileData createUserFile(Objectify datastore, Key<UserData> userKey,
      String fileName) {
    UserFileData ufd = datastore.find(userFileKey(userKey, fileName));
    if (ufd == null) {
      ufd = new UserFileData();
      ufd.fileName = fileName;
      ufd.userKey = userKey;
      return ufd;
    }
    return null;
  }

  @Override
  public List<String> getUserFiles(final String userId) {
    final List<String> fileList = new ArrayList<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<UserData> userKey = userKey(userId);
          for (UserFileData ufd : datastore.query(UserFileData.class).ancestor(userKey)) {
            fileList.add(ufd.fileName);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
    return fileList;
  }

  @Override
  public void uploadUserFile(final String userId, final String fileName,
      final String content, final String encoding) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          byte[] bytes;
          try {
            bytes = content.getBytes(encoding);
          } catch (UnsupportedEncodingException e) {
            // Note: this RuntimeException should propagate up out of runJobWithRetries
            throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding, "
                + collectUserErrorInfo(userId, fileName), e);
          }
          addUserFileContents(datastore, userId, fileName, bytes);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId, fileName), e);
    }
  }

  @Override
  public void uploadRawUserFile(final String userId, final String fileName,
      final byte[] content) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          addUserFileContents(datastore, userId, fileName, content);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId, fileName), e);
    }
  }

  /*
   * We expect the UserFileData object for the given userId and fileName to
   * already exist in the datastore. Find the object and update its contents.
   */
  private void addUserFileContents(Objectify datastore, String userId, String fileName, byte[] content) {
    UserFileData ufd = datastore.find(userFileKey(userKey(userId), fileName));
    Preconditions.checkState(ufd != null);
    ufd.content = content;
    datastore.put(ufd);
  }

  @Override
  public String downloadUserFile(final String userId, final String fileName,
      final String encoding) {
    final Result<String> result = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          try {
            result.t = new String(downloadRawUserFile(userId, fileName), encoding);
          } catch (UnsupportedEncodingException e) {
            throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding, " +
                collectUserErrorInfo(userId, fileName), e);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId, fileName), e);
    }
    return result.t;
  }

  @Override
  public byte[] downloadRawUserFile(final String userId, final String fileName) {
    final Result<byte[]> result = new Result<byte[]>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserFileData ufd = datastore.find(userFileKey(userKey(userId), fileName));
          if (ufd != null) {
            result.t = ufd.content;
          } else {
            throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId, fileName),
                new FileNotFoundException(fileName));
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId, fileName), e);
    }
    return result.t;
  }

  @Override
  public void deleteUserFile(final String userId, final String fileName) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<UserFileData> ufdKey = userFileKey(userKey(userId), fileName);
          if (datastore.find(ufdKey) != null) {
            datastore.delete(ufdKey);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId, fileName), e);
    }
  }

  @Override
  public int getMaxJobSizeBytes() {
    // TODO(user): what should this mean?
    return 5 * 1024 * 1024;
  }

  @Override
  public void addSourceFilesToProject(final String userId, final long projectId,
      final boolean changeModDate, final String... fileNames) {
    if (!getProjects(userId).contains(projectId)) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId),
          new UnauthorizedAccessException(userId, projectId, null));
    }
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          addFilesToProject(datastore, projectId, FileData.RoleEnum.SOURCE, changeModDate, fileNames);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileNames[0]), e);
    }
  }

  @Override
  public void addOutputFilesToProject(final String userId, final long projectId,
      final String... fileNames) {
    if (!getProjects(userId).contains(projectId)) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId),
          new UnauthorizedAccessException(userId, projectId, null));
    }
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          addFilesToProject(datastore, projectId, FileData.RoleEnum.TARGET, false, fileNames);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileNames[0]), e);
    }
  }

  private void addFilesToProject(Objectify datastore, long projectId, FileData.RoleEnum role,
      boolean changeModDate, String... fileNames) {
    List<FileData> addedFiles = new ArrayList<FileData>();
    Key<ProjectData> projectKey = projectKey(projectId);
    for (String fileName : fileNames) {
      FileData fd = createProjectFile(datastore, projectKey, role, fileName);
      if (fd != null) {
        addedFiles.add(fd);
      }
    }
    datastore.put(addedFiles); // batch put
    if (changeModDate) {
      updateProjectModDate(datastore, projectId);
    }
  }

  private FileData createProjectFile(Objectify datastore, Key<ProjectData> projectKey,
      FileData.RoleEnum role, String fileName) {
    FileData fd = datastore.find(projectFileKey(projectKey, fileName));
    if (fd == null) {
      fd = new FileData();
      fd.fileName = fileName;
      fd.projectKey = projectKey;
      fd.role = role;
      return fd;
    } else if (!fd.role.equals(role)) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(null, projectKey.getId(), fileName),
          new IllegalStateException("File role change is not supported"));
    }
    return null;
  }

  @Override
  public void removeSourceFilesFromProject(final String userId, final long projectId,
      final boolean changeModDate, final String... fileNames) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          removeFilesFromProject(datastore, projectId, FileData.RoleEnum.SOURCE, changeModDate, fileNames);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileNames[0]), e);
    }
  }

  @Override
  public void removeOutputFilesFromProject(final String userId, final long projectId,
      final String... fileNames) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          removeFilesFromProject(datastore, projectId, FileData.RoleEnum.TARGET, false, fileNames);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileNames[0]), e);
    }
  }

  private void removeFilesFromProject(Objectify datastore, long projectId,
      FileData.RoleEnum role, boolean changeModDate, String... fileNames) {
    Key<ProjectData> projectKey = projectKey(projectId);
    List<Key<FileData>> filesToRemove = new ArrayList<Key<FileData>>();
    for (String fileName : fileNames) {
      FileData fd = datastore.find(projectFileKey(projectKey, fileName));
      if (fd != null) {
        if (fd.role.equals(role)) {
          filesToRemove.add(projectFileKey(projectKey, fileName));
        } else {
          throw CrashReport.createAndLogError(LOG, null,
              collectProjectErrorInfo(null, projectId, fileName),
              new IllegalStateException("File role change is not supported"));
        }
      }
    }
    datastore.delete(filesToRemove);  // batch delete
    if (changeModDate) {
      updateProjectModDate(datastore, projectId);
    }
  }

  @Override
  public List<String> getProjectSourceFiles(final String userId, final long projectId) {
    if (!getProjects(userId).contains(projectId)) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId),
          new UnauthorizedAccessException(userId, projectId, null));
    }
    final Result<List<String>> result = new Result<List<String>>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          result.t = getProjectFiles(datastore, projectId, FileData.RoleEnum.SOURCE);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return result.t;
  }

  @Override
  public List<String> getProjectOutputFiles(final String userId, final long projectId) {
   if (!getProjects(userId).contains(projectId)) {
     throw CrashReport.createAndLogError(LOG, null,
         collectUserProjectErrorInfo(userId, projectId),
         new UnauthorizedAccessException(userId, projectId, null));
   }
   final Result<List<String>> result = new Result<List<String>>();
   try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          result.t = getProjectFiles(datastore, projectId, FileData.RoleEnum.TARGET);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return result.t;
  }

  private List<String> getProjectFiles(Objectify datastore, long projectId,
                                       FileData.RoleEnum role) {
    Key<ProjectData> projectKey = projectKey(projectId);
    List<String> fileList = new ArrayList<String>();
    for (FileData fd : datastore.query(FileData.class).ancestor(projectKey)) {
      if (fd.role.equals(role)) {
        fileList.add(fd.fileName);
      }
    }
    return fileList;
  }

  @Override
  public long uploadFile(final long projectId, final String fileName, final String userId,
      final String content, final String encoding) throws BlocksTruncatedException {
    try {
      return uploadRawFile(projectId, fileName, userId, false, content.getBytes(encoding));
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding,"
          + collectProjectErrorInfo(null, projectId, fileName), e);
    }
  }

  @Override
  public long uploadFileForce(final long projectId, final String fileName, final String userId,
      final String content, final String encoding) {
    try {
      return uploadRawFileForce(projectId, fileName, userId, content.getBytes(encoding));
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding,"
          + collectProjectErrorInfo(null, projectId, fileName), e);
    }
  }

  private long updateProjectModDate(Objectify datastore, long projectId) {
    long modDate = System.currentTimeMillis();
    ProjectData pd = datastore.find(projectKey(projectId));
    if (pd != null) {
      pd.dateModified = modDate;
      datastore.put(pd);
      return modDate;
    } else {
      throw CrashReport.createAndLogError(LOG, null, null,
          new IllegalArgumentException("project " + projectId + " doesn't exist"));
    }
  }

  @Override
  public long uploadRawFileForce(final long projectId, final String fileName, final String userId,
      final byte[] content) {
    try {
      return uploadRawFile(projectId, fileName, userId, true, content);
    } catch (BlocksTruncatedException e) {
      // Won't get here, exception isn't thrown when force is true
      return 0;
    }
  }

  @Override
  public long uploadRawFile(final long projectId, final String fileName, final String userId,
      final boolean force, final byte[] content) throws BlocksTruncatedException {
    final Result<Long> modTime = new Result<Long>();
    final boolean useBlobstore = useBlobstoreForFile(fileName, content.length);
    final boolean useGCS = useGCSforFile(fileName, content.length);
    final Result<String> oldBlobstorePath = new Result<String>();
    final boolean considerBackup = (useGcs?((fileName.contains("src/") && fileName.endsWith(".blk")) // AI1 Blocks Files
        || (fileName.contains("src/") && fileName.endsWith(".bky")) // Blockly files
        || (fileName.contains("src/") && fileName.endsWith(".scm"))) // Form Definitions
      :false);

    try {
      runJobWithRetries(new JobRetryHelper() {
        FileData fd;

        @Override
        public void run(Objectify datastore) throws ObjectifyException {
          fd = datastore.find(projectFileKey(projectKey(projectId), fileName));

          // <Screen>.yail files are missing when user converts AI1 project to AI2
          // instead of blowing up, just create a <Screen>.yail file
          if (fd == null && fileName.endsWith(".yail")){
            fd = createProjectFile(datastore, projectKey(projectId), FileData.RoleEnum.SOURCE, fileName);
          }

          Preconditions.checkState(fd != null);

          if ((content.length < 120) && (fileName.endsWith(".bky"))) { // Likely this is an empty blocks workspace
            if (!force) {            // force is true if we *really* want to save it!
              checkForBlocksTruncation(fd); // See if we had previous content and throw and exception if so
            }
          }

          if (fd.isBlob) {
            // mark the old blobstore blob for deletion
           oldBlobstorePath.t = fd.blobstorePath;
          }
          if (useGCS) {
            fd.isGCS = true;
            fd.gcsName = makeGCSfileName(fileName, projectId);
            try {
              if (content.length > 0) { // If there is actual content
                GcsOutputChannel outputChannel =
                  gcsService.createOrReplace(new GcsFilename(GCS_BUCKET_NAME, fd.gcsName), GcsFileOptions.getDefaultInstance());
                outputChannel.write(ByteBuffer.wrap(content));
                outputChannel.close();
              }
            } catch (IOException e) {
              throw CrashReport.createAndLogError(LOG, null,
                collectProjectErrorInfo(userId, projectId, fileName), e);
            }
            // If the content was previously stored in the datastore, clear it out.
            fd.content = null;
            fd.isBlob = false;  // in case we are converting from a blob
            fd.blobstorePath = null;
          } else if (useBlobstore) {
            try {
              fd.blobstorePath = uploadToBlobstore(content, makeBlobName(projectId, fileName));
            } catch (BlobWriteException e) {
              // Note that this makes the BlobWriteException fatal. The job will
              // not be retried if we get this exception.
              throw CrashReport.createAndLogError(LOG, null,
                  collectProjectErrorInfo(userId, projectId, fileName), e);
            }
            // If the content was previously stored in the datastore or GCS, clear it out.
            fd.isBlob = true;
            fd.isGCS = false;
            fd.gcsName = null;
            fd.content = null;
          } else {
            if (fd.isGCS) {     // Was a GCS file, must have gotten smaller
              try {             // and is now stored in the data store
                gcsService.delete(new GcsFilename(GCS_BUCKET_NAME, fd.gcsName));
              } catch (IOException e) {
                throw CrashReport.createAndLogError(LOG, null,
                  collectProjectErrorInfo(userId, projectId, fileName), e);
              }
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
          if (considerBackup) {
            if ((fd.lastBackup + TWENTYFOURHOURS) < System.currentTimeMillis()) {
              try {
                String gcsName = makeGCSfileName(fileName + "." + formattedTime() + ".backup", projectId);
                GcsOutputChannel outputChannel =
                    gcsService.createOrReplace((new GcsFilename(GCS_BUCKET_NAME, gcsName)), GcsFileOptions.getDefaultInstance());
                outputChannel.write(ByteBuffer.wrap(content));
                outputChannel.close();
                fd.lastBackup = System.currentTimeMillis();
              } catch (IOException e) {
                throw CrashReport.createAndLogError(LOG, null,
                    collectProjectErrorInfo(userId, projectId, fileName + "(backup)"), e);
              }
            }
          }
          datastore.put(fd);
          modTime.t = updateProjectModDate(datastore, projectId);
        }

        @Override
        public void onNonFatalError() {
          if (fd != null && fd.blobstorePath != null) {
            oldBlobstorePath.t = fd.blobstorePath;
          }
        }
      });
      // It would have been convenient to delete the old blobstore file within the run() method
      // above but that caused an exception where the app engine datastore claimed to be doing
      // operations on multiple entity groups within the same transaction.  Apparently the blobstore
      // operations are, at least partially, also datastore operations.
      if (oldBlobstorePath.t != null) {
        deleteBlobstoreFile(oldBlobstorePath.t);
      }
    } catch (ObjectifyException e) {
      if (e.getMessage().startsWith("Blocks")) { // Convert Exception
        throw new BlocksTruncatedException();
      }
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileName), e);
    }
    return modTime.t;
  }

  protected void deleteBlobstoreFile(String blobstorePath) {
    // It would be nice if there were an AppEngineFile.delete() method but alas there isn't, so we
    // have to get the BlobKey and delete via the BlobstoreService.
    BlobKey blobKey = null;
    try {
      AppEngineFile blobstoreFile = new AppEngineFile(blobstorePath);
      blobKey = fileService.getBlobKey(blobstoreFile);
      BlobstoreServiceFactory.getBlobstoreService().delete(blobKey);
    } catch (RuntimeException e) {
      // Log blob delete errors but don't make them fatal
      CrashReport.createAndLogError(LOG, null, "Error deleting blob with path " +
          blobstorePath + " and key " + blobKey, e);
    }
  }

  private String uploadToBlobstore(byte[] content, String name)
      throws BlobWriteException, ObjectifyException {
    // Create a new Blob file with generic mime-type "application/octet-stream"
    AppEngineFile blobstoreFile = null;
    try {
      blobstoreFile = fileService.createNewBlobFile("application/octet-stream", name);

      // Open a channel to write to it
      FileWriteChannel blobstoreWriteChannel = fileService.openWriteChannel(blobstoreFile, true);

      OutputStream blobstoreOutputStream = Channels.newOutputStream(blobstoreWriteChannel);
      ByteStreams.copy(ByteStreams.newInputStreamSupplier(content), blobstoreOutputStream);
      blobstoreOutputStream.flush();
      blobstoreOutputStream.close();
      blobstoreWriteChannel.closeFinally();
    } catch (IOException e) {
      throw new BlobWriteException(e, "Error writing blob with name " + name);
    } catch (Exception e) {
      throw new ObjectifyException(e);
    }

    return blobstoreFile.getFullPath();
  }

  @VisibleForTesting
  boolean useBlobstoreForFile(String fileName, int length) {
    if (useGcs)
      return false;               // Disable for now
    boolean shouldUse =  fileName.contains("assets/")
      || fileName.endsWith(".apk");
    if (shouldUse)
      return true;              // Use GCS for package output and assets
    boolean mayUse = (fileName.contains("src/") && fileName.endsWith(".blk")) // AI1 Blocks Files
      || (fileName.contains("src/") && fileName.endsWith(".bky")); // Blockly files
    if (mayUse && length > 50000) // Only use Blobstore for larger blocks files
      return true;
    return false;
  }

  // Experimental -- Use the Google Cloud Store for a file
  @VisibleForTesting
  boolean useGCSforFile(String fileName, int length) {
    if (!useGcs)                // Using legacy blob store solution
      return false;
    boolean shouldUse =  fileName.contains("assets/")
      || fileName.endsWith(".apk");
    if (shouldUse)
      return true;              // Use GCS for package output and assets
    boolean mayUse = (fileName.contains("src/") && fileName.endsWith(".blk")) // AI1 Blocks Files
      || (fileName.contains("src/") && fileName.endsWith(".bky")); // Blockly files
    if (mayUse && length > 50000) // Only use GCS for larger blocks files
      return true;
    return false;
  }

  // Make a GCS file name
  String makeGCSfileName(String fileName, long projectId) {
    return (projectId + "/" + fileName);
  }

  @Override
  public long deleteFile(final String userId, final long projectId, final String fileName) {
    if (!getProjects(userId).contains(projectId)) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId),
          new UnauthorizedAccessException(userId, projectId, null));
    }
    final Result<Long> modTime = new Result<Long>();
    final Result<String> oldBlobstorePath = new Result<String>();
    final Result<String> oldgcsName = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<FileData> fileKey = projectFileKey(projectKey(projectId), fileName);
          FileData fileData = datastore.find(fileKey);
          if (fileData != null) {
            oldBlobstorePath.t = fileData.blobstorePath;
            if (fileData.isGCS) {
              oldgcsName.t = fileData.gcsName;
            }
          }
          datastore.delete(fileKey);
          modTime.t = updateProjectModDate(datastore, projectId);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileName), e);
    }
    if (oldBlobstorePath.t != null) {
      deleteBlobstoreFile(oldBlobstorePath.t);
    }
    if (oldgcsName.t != null) {
      try {
        gcsService.delete(new GcsFilename(GCS_BUCKET_NAME, oldgcsName.t));
      } catch (IOException e) {
        LOG.log(Level.WARNING, "Unable to delete " + oldgcsName + " from GCS.", e);
      }
    }
    return (modTime.t == null) ? 0 : modTime.t;
  }

  // TODO(user) - just use "UTF-8" (instead of having an encoding argument),
  // which will never cause UnsupportedEncodingException. (Here and in other
  // methods with the encoding arg.
  @Override
  public String downloadFile(final String userId, final long projectId, final String fileName,
      final String encoding) {
    try {
      return new String(downloadRawFile(userId, projectId, fileName), encoding);
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding, "
          + collectProjectErrorInfo(userId, projectId, fileName), e);
    }
  }

  @Override
  public void recordCorruption(String userId, long projectId, String fileId, String message) {
    Objectify datastore = ObjectifyService.begin();
    final CorruptionRecord data = new CorruptionRecord();
    data.timestamp = new Date();
    data.id = null;
    data.userId = userId;
    data.fileId = fileId;
    data.projectId = projectId;
    data.message = message;
    try {
      runJobWithRetries(new JobRetryHelper() {
          @Override
          public void run(Objectify datastore) {
            datastore.put(data);
          }
        });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  @Override
  public byte[] downloadRawFile(final String userId, final long projectId, final String fileName) {
    if (!getProjects(userId).contains(projectId)) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId),
          new UnauthorizedAccessException(userId, projectId, null));
    }
    final Result<byte[]> result = new Result<byte[]>();
    final Result<FileData> fd = new Result<FileData>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<FileData> fileKey = projectFileKey(projectKey(projectId), fileName);
          fd.t = datastore.find(fileKey);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileName), e);
    }
    // read the blob/GCS File outside of the job
    FileData fileData = fd.t;
    if (fileData != null) {
      if (fileData.isGCS) {     // It's in the Cloud Store
        try {
          GcsFilename gcsFileName = new GcsFilename(GCS_BUCKET_NAME, fileData.gcsName);
          int bytesRead = 0;
          int fileSize = (int) gcsService.getMetadata(gcsFileName).getLength();
          ByteBuffer resultBuffer = ByteBuffer.allocate(fileSize);
          GcsInputChannel readChannel = gcsService.openReadChannel(gcsFileName, 0);
          try {
            while (bytesRead < fileSize) {
              bytesRead += readChannel.read(resultBuffer);
              if (bytesRead < fileSize) {
                LOG.log(Level.INFO, "readChannel: bytesRead = " + bytesRead + " fileSize = " + fileSize);
              }
            }
          } finally {
            readChannel.close();
          }
          result.t = resultBuffer.array();
        } catch (IOException e) {
          throw CrashReport.createAndLogError(LOG, null,
              collectProjectErrorInfo(userId, projectId, fileName), e);
        }
      } else if (fileData.isBlob) {
        try {
          result.t = getBlobstoreBytes(fileData.blobstorePath);
        } catch (BlobReadException e) {
          throw CrashReport.createAndLogError(LOG, null,
              collectProjectErrorInfo(userId, projectId, fileName), e);
        }
      } else {
        if (fileData.content == null) {
          result.t = new byte[0];
        } else {
          result.t = fileData.content;
        }
      }
    } else {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileName),
          new FileNotFoundException("No data for " + fileName));
    }
    return result.t;
  }

  // Note: this must be called outside of any transaction, since getBlobKey()
  // uses the current transaction and it will most likely have the wrong
  // entity group!
  private byte[] getBlobstoreBytes(String blobstorePath) throws BlobReadException {
    AppEngineFile blobstoreFile = new AppEngineFile(blobstorePath);
    BlobKey blobKey = fileService.getBlobKey(blobstoreFile);
    if (blobKey == null) {
      throw new BlobReadException("getBlobKey() returned null for " + blobstorePath);
    }
    try {
      InputStream blobInputStream = new BlobstoreInputStream(blobKey);
      return ByteStreams.toByteArray(blobInputStream);
    } catch (IOException e) {
      throw new BlobReadException(e, "Error trying to read blob from " + blobstorePath
          + ", blobkey = " + blobKey);
    }
  }

  /**
   *  Exports project files as a zip archive
   * @param userId a user Id (the request is made on behalf of this user)
   * @param projectId  project ID
   * @param includeProjectHistory  whether or not to include the project history
   * @param includeAndroidKeystore  whether or not to include the Android keystore
   * @param zipName  the name of the zip file, if a specific one is desired

   * @return  project with the content as requested by params.
   */
  @Override
  public ProjectSourceZip exportProjectSourceZip(final String userId, final long projectId,
                                                 final boolean includeProjectHistory,
                                                 final boolean includeAndroidKeystore,
                                                 @Nullable String zipName) throws IOException {
    final Result<Integer> fileCount = new Result<Integer>();
    fileCount.t = 0;
    final Result<String> projectHistory = new Result<String>();
    projectHistory.t = null;
    // We collect up all the file data for the project in a transaction but
    // then we read the data and write the zip file outside of the transaction
    // to avoid problems reading blobs in a transaction with the wrong
    // entity group.
    final List<FileData> fileData = new ArrayList<FileData>();
    final Result<String> projectName = new Result<String>();
    projectName.t = null;
    String fileName = null;

    ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
    final ZipOutputStream out = new ZipOutputStream(zipFile);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<ProjectData> projectKey = projectKey(projectId);
          boolean foundFiles = false;
          for (FileData fd : datastore.query(FileData.class).ancestor(projectKey)) {
            String fileName = fd.fileName;
            if (fd.role.equals(FileData.RoleEnum.SOURCE)) {
              if (fileName.equals(FileExporter.REMIX_INFORMATION_FILE_PATH)) {
                // Skip legacy remix history files that were previous stored with the project
                continue;
              }
              fileData.add(fd);
              foundFiles = true;
            }
          }
          if (foundFiles) {
            ProjectData pd = datastore.find(projectKey);
            projectName.t = pd.name;
            if (includeProjectHistory && !Strings.isNullOrEmpty(pd.history)) {
              projectHistory.t = pd.history;
            }
          }
        }
      });

      // Process the file contents outside of the job since we can't read
      // blobs in the job.
      for (FileData fd : fileData) {
        fileName = fd.fileName;
        byte[] data;
        if (fd.isBlob) {
          try {
            data = getBlobstoreBytes(fd.blobstorePath);
          } catch (BlobReadException e) {
            throw CrashReport.createAndLogError(LOG, null,
                collectProjectErrorInfo(userId, projectId, fileName), e);
          }
        } else if (fd.isGCS) {
          try {
            GcsFilename gcsFileName = new GcsFilename(GCS_BUCKET_NAME, fd.gcsName);
            int bytesRead = 0;
            int fileSize = (int) gcsService.getMetadata(gcsFileName).getLength();
            ByteBuffer resultBuffer = ByteBuffer.allocate(fileSize);
            GcsInputChannel readChannel = gcsService.openReadChannel(gcsFileName, 0);
            try {
              while (bytesRead < fileSize) {
                bytesRead += readChannel.read(resultBuffer);
                if (bytesRead < fileSize) {
                  LOG.log(Level.INFO, "readChannel: bytesRead = " + bytesRead + " fileSize = " + fileSize);
                }
              }
            } finally {
              readChannel.close();
            }
            data = resultBuffer.array();
          } catch (IOException e) {
            throw CrashReport.createAndLogError(LOG, null,
              collectProjectErrorInfo(userId, projectId, fileName), e);
          }
        } else {
          data = fd.content;
        }
        if (data == null) {     // This happens if file creation is interrupted
          data = new byte[0];
        }
        out.putNextEntry(new ZipEntry(fileName));
        out.write(data, 0, data.length);
        out.closeEntry();
        fileCount.t++;
      }
      if (projectHistory.t != null) {
        byte[] data = projectHistory.t.getBytes(StorageUtil.DEFAULT_CHARSET);
        out.putNextEntry(new ZipEntry(FileExporter.REMIX_INFORMATION_FILE_PATH));
        out.write(data, 0, data.length);
        out.closeEntry();
        fileCount.t++;
      }
    } catch (ObjectifyException e) {
      CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileName), e);
      throw new IOException("Reflecting exception for userid " + userId +
          " projectId " + projectId + ", original exception " + e.getMessage());
    } catch (RuntimeException e) {
      CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileName), e);
      throw new IOException("Reflecting exception for userid " + userId +
          " projectId " + projectId + ", original exception " + e.getMessage());
    }

    if (fileCount.t == 0) {
      // can't close out since will get a ZipException due to the lack of files
      throw new IllegalArgumentException("No files to download");
    }

    if (includeAndroidKeystore) {
      try {
        runJobWithRetries(new JobRetryHelper() {
            @Override
            public void run(Objectify datastore) {
              try {
                Key<UserData> userKey = userKey(userId);
                for (UserFileData ufd : datastore.query(UserFileData.class).ancestor(userKey)) {
                  if (ufd.fileName.equals(StorageUtil.ANDROID_KEYSTORE_FILENAME) &&
                      (ufd.content.length > 0)) {
                    out.putNextEntry(new ZipEntry(StorageUtil.ANDROID_KEYSTORE_FILENAME));
                    out.write(ufd.content, 0, ufd.content.length);
                    out.closeEntry();
                    fileCount.t++;
                  }
                }
              } catch (IOException e) {
                throw CrashReport.createAndLogError(LOG, null,
                    collectProjectErrorInfo(userId, projectId,
                        StorageUtil.ANDROID_KEYSTORE_FILENAME), e);
              }
            }
          });
      } catch (ObjectifyException e) {
        throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
      }
    }

    out.close();

    if (zipName == null) {
      zipName = projectName.t + ".aia";
    }
    ProjectSourceZip projectSourceZip =
        new ProjectSourceZip(zipName, zipFile.toByteArray(), fileCount.t);
    projectSourceZip.setMetadata(projectName.t);
    return projectSourceZip;
  }

  @Override
  public Motd getCurrentMotd() {
    final Result<Motd> motd = new Result<Motd>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          MotdData motdData = datastore.find(MotdData.class, MOTD_ID);
          if (motdData != null) { // it shouldn't be!
            motd.t =  new Motd(motdData.id, motdData.caption, motdData.content);
          } else {
            motd.t = new Motd(MOTD_ID, "Oops, no message of the day!", null);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
    return motd.t;
  }

  @Override
  public String findUserByEmail(final String email) throws NoSuchElementException {
    Objectify datastore = ObjectifyService.begin();
    // note: if there are multiple users with the same email we'll only
    // get the first one. we don't expect this to happen
    UserData userData = datastore.query(UserData.class).filter("email", email).get();
    if (userData == null) {
      throw new NoSuchElementException("Couldn't find a user with email " + email);
    }
    return userData.id;
  }

  @Override
  public String findIpAddressByKey(final String key) {
    Objectify datastore = ObjectifyService.begin();
    RendezvousData data  = datastore.query(RendezvousData.class).filter("key", key).get();
    if (data == null) {
      return null;
    } else {
      return data.ipAddress;
    }
  }

  @Override
  public void storeIpAddressByKey(final String key, final String ipAddress) {
    Objectify datastore = ObjectifyService.begin();
    final RendezvousData data  = datastore.query(RendezvousData.class).filter("key", key).get();
    try {
      runJobWithRetries(new JobRetryHelper() {
          @Override
          public void run(Objectify datastore) {
            RendezvousData new_data = null;
            if (data == null) {
              new_data = new RendezvousData();
              new_data.id = null;
              new_data.key = key;
              new_data.ipAddress = ipAddress;
              new_data.used = new Date(); // So we can cleanup old entries
              datastore.put(new_data);
            } else {
              new_data = data;
              new_data.ipAddress = ipAddress;
              new_data.used = new Date();
              datastore.put(new_data);
          }
          }
        });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  @Override
  public boolean checkWhiteList(String email) {
    Objectify datastore = ObjectifyService.begin();
    WhiteListData data = datastore.query(WhiteListData.class).filter("emailLower", email.toLowerCase()).get();
    if (data == null)
      return false;
    return true;
  }

  @Override
  public void storeFeedback(final String notes, final String foundIn, final String faultData,
    final String comments, final String datestamp, final String email, final String projectId) {
    Objectify datastore = ObjectifyService.begin();
    try {
      runJobWithRetries(new JobRetryHelper() {
          @Override
          public void run(Objectify datastore) {
            FeedbackData data = new FeedbackData();
            data.id = null;
            data.notes = notes;
            data.foundIn = foundIn;
            data.faultData = faultData;
            data.comments = comments;
            data.datestamp = datestamp;
            data.email = email;
            data.projectId = projectId;
            datastore.put(data);
          }
        });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  private void initMotd() {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          MotdData motdData = datastore.find(MotdData.class, MOTD_ID);
          if (motdData == null) {
            MotdData firstMotd = new MotdData();
            firstMotd.id = MOTD_ID;
            firstMotd.caption = "Hello!";
            firstMotd.content = "Welcome to the experimental App Inventor system from MIT. " +
                "This is still a prototype.  It would be a good idea to frequently back up " +
                "your projects to local storage.";
            datastore.put(firstMotd);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, "Initing MOTD", e);
    }
  }

  // Nonce Management Routines.
  // The Nonce is used to map to userId and ProjectId and is used
  // for non-authenticated access to a built APK file.

  public void storeNonce(final String nonceValue, final String userId, final long projectId) {
    Objectify datastore = ObjectifyService.begin();
    final NonceData data  = datastore.query(NonceData.class).filter("nonce", nonceValue).get();
    try {
      runJobWithRetries(new JobRetryHelper() {
          @Override
          public void run(Objectify datastore) {
            NonceData new_data = null;
            if (data == null) {
              new_data = new NonceData();
              new_data.id = null;
              new_data.nonce = nonceValue;
              new_data.userId = userId;
              new_data.projectId = projectId;
              new_data.timestamp = new Date();
              datastore.put(new_data);
            } else {
              new_data = data;
              new_data.userId = userId;
              new_data.projectId = projectId;
              new_data.timestamp = new Date();
              datastore.put(new_data);
          }
          }
        });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  public Nonce getNoncebyValue(String nonceValue) {
    Objectify datastore = ObjectifyService.begin();
    NonceData data  = datastore.query(NonceData.class).filter("nonce", nonceValue).get();
    if (data == null) {
      return null;
    } else {
      return (new Nonce(nonceValue, data.userId, data.projectId, data.timestamp));
    }
  }

  // Cleanup expired nonces which are older then 3 hours. Normal Nonce lifetime
  // is 2 hours. So for one hour they persist and return "link expired" instead of
  // "link not found" (after the object itself is removed).
  //
  // Note: We only process up to 10 here to limit the amount of processing time
  // we spend here. If we remove up to 10 for each call, we should keep ahead
  // of the growing garbage.
  //
  // Also note that we are not running in a transaction, there is no need
  public void cleanupNonces() {
    Objectify datastore = ObjectifyService.begin();
    // We do not use runJobWithRetries because if we fail here, we will be
    // called again the next time someone attempts to download a built APK
    // via a QR Code.
    try {
      datastore.delete(datastore.query(NonceData.class)
        .filter("timestamp <", new Date((new Date()).getTime() - 3600*3*1000L))
        .limit(10).fetchKeys());
    } catch (Exception ex) {
        LOG.log(Level.WARNING, "Exception during cleanupNonces", ex);
    }

  }

  // Create a name for a blob from a project id and file name. This is mostly
  // to help with debugging and viewing the blobstore via the admin console.
  // We don't currently use these blob names anywhere else.
  private String makeBlobName(long projectId, String fileName) {
    return projectId + "/" + fileName;
  }

  private Key<UserData> userKey(String userId) {
    return new Key<UserData>(UserData.class, userId);
  }

  private Key<ProjectData> projectKey(long projectId) {
    return new Key<ProjectData>(ProjectData.class, projectId);
  }

  private Key<UserProjectData> userProjectKey(Key<UserData> userKey, long projectId) {
    return new Key<UserProjectData>(userKey, UserProjectData.class, projectId);
  }

  private Key<UserFileData> userFileKey(Key<UserData> userKey, String fileName) {
    return new Key<UserFileData>(userKey, UserFileData.class, fileName);
  }

  private Key<FileData> projectFileKey(Key<ProjectData> projectKey, String fileName) {
    return new Key<FileData>(projectKey, FileData.class, fileName);
  }

  /**
   * Call job.run() in a transaction and commit the transaction if no exceptions
   * occur. If we get a {@link java.util.ConcurrentModificationException}
   * or {@link com.google.appinventor.server.storage.ObjectifyException}
   * we will retry the job (at most {@code MAX_JOB_RETRIES times}).
   * Any other exception will cause the job to fail immediately.
   * @param job
   * @throws ObjectifyException
   */
  @VisibleForTesting
  void runJobWithRetries(JobRetryHelper job) throws ObjectifyException {
    int tries = 0;
    while (tries <= MAX_JOB_RETRIES) {
      Objectify datastore = ObjectifyService.beginTransaction();
      try {
        job.run(datastore);
        datastore.getTxn().commit();
        break;
      } catch (ConcurrentModificationException ex) {
        job.onNonFatalError();
        LOG.log(Level.WARNING, "Optimistic concurrency failure", ex);
      } catch (ObjectifyException oe) {
        String message = oe.getMessage();
        if (message != null && message.startsWith("Blocks")) { // This one is fatal!
          throw oe;
        }
        // maybe this should be a fatal error? I think the only thing
        // that creates this exception (other than this method) is uploadToBlobstore
        job.onNonFatalError();
      } finally {
        if (datastore.getTxn().isActive()) {
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

  private static String collectUserErrorInfo(final String userId) {
    return collectUserErrorInfo(userId, CrashReport.NOT_AVAILABLE);
  }

  private static String collectUserErrorInfo(final String userId, String fileName) {
    return "user=" + userId + ", file=" + fileName;
  }

  private static String collectProjectErrorInfo(final String userId, final long projectId,
      final String fileName) {
    return "user=" + userId + ", project=" + projectId + ", file=" + fileName;
  }

  private static String collectUserProjectErrorInfo(final String userId, final long projectId) {
    return "user=" + userId + ", project=" + projectId;
  }

  // ********* METHODS BELOW ARE ONLY FOR TESTING *********

  @VisibleForTesting
  void createRawUserFile(String userId, String fileName, byte[] content) {
    Objectify datastore = ObjectifyService.begin();
    UserFileData ufd = createUserFile(datastore, userKey(userId), fileName);
    if (ufd != null) {
      ufd.content = content;
      datastore.put(ufd);
    }
  }

  @VisibleForTesting
  boolean isBlobFile(long projectId, String fileName) {
    Objectify datastore = ObjectifyService.begin();
    FileData fd = datastore.find(projectFileKey(projectKey(projectId), fileName));
    if (fd != null) {
      return fd.isBlob;
    } else {
      return false;
    }
  }

  @VisibleForTesting
  ProjectData getProject(long projectId) {
    return ObjectifyService.begin().find(projectKey(projectId));
  }

  // Return time in ISO_8660 format
  private static String formattedTime() {
    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    return formatter.format(new java.util.Date());
  }

  // We are called when our caller detects we are about to write a trivial (empty)
  // workspace. We check to see if previously the workspace was non-trivial and
  // if so, throw the BlocksTruncatedException. This will be passed through the RPC
  // layer to the client code which will put up a dialog box for the user to review
  // See Ode.java for more information
  private void checkForBlocksTruncation(FileData fd) throws ObjectifyException {
    if (fd.isBlob || fd.isGCS || fd.content.length > 120)
      throw new ObjectifyException("BlocksTruncated"); // Hack
    // I'm avoiding having to modify every use of runJobWithRetries to handle a new
    // exception, so we use this dodge.
  }

}
