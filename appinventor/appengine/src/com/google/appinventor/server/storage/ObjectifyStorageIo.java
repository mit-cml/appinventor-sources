// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.server.storage;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.storage.StoredData.FileData;
import com.google.appinventor.server.storage.StoredData.MotdData;
import com.google.appinventor.server.storage.StoredData.ProjectData;
import com.google.appinventor.server.storage.StoredData.UserData;
import com.google.appinventor.server.storage.StoredData.UserFileData;
import com.google.appinventor.server.storage.StoredData.UserProjectData;
import com.google.appinventor.shared.rpc.Motd;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

  private static final String ANDROID_KEYSTORE_FILENAME = "android.keystore";

  // Use this class to define the work of a job that can be retried. The
  // "datastore" argument to run() is the Objectify object for this job
  // (created with ObjectifyService.beginTransaction()). Note that all operations
  // on "datastore" should be for objects in the same entity group.
  @VisibleForTesting
  abstract class JobRetryHelper {
    public abstract void run(Objectify datastore) throws ObjectifyException;
    public void onNonFatalError() throws ObjectifyException {
      // Default is to do nothing
    }
  }

  // Create a final object of this class to hold a modifiable result value that
  // can be used in a method of an inner class.
  private class Result<T> {
    T t;
  }

  static {
    // Register the data object classes stored in the database
    ObjectifyService.register(UserData.class);
    ObjectifyService.register(ProjectData.class);
    ObjectifyService.register(UserProjectData.class);
    ObjectifyService.register(FileData.class);
    ObjectifyService.register(UserFileData.class);
    ObjectifyService.register(MotdData.class);
  }

  ObjectifyStorageIo() {
    initMotd();
  }

  @Override
  public User getUser(String userId) {
    return getUser(userId, null);
  }

  @Override
  public User getUser(final String userId, final String email) {
    final User user = new User(userId, email, false);
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
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
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
    try {
      // first job is on the project entity, creating the ProjectData object
      // and the associated files.
      runJobWithRetries(new JobRetryHelper() {
        List<FileData> addedFiles = new ArrayList<FileData>();

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
          try {
            for (TextFile file : project.getSourceFiles()) {
              addedFiles.add(createRawFile(projectKey, FileData.RoleEnum.SOURCE, file.getFileName(),
                  file.getContent().getBytes(DEFAULT_ENCODING)));
            }
          } catch (UnsupportedEncodingException e) {  // shouldn't happen!
            throw CrashReport.createAndLogError(LOG, null, project.getProjectName(), e);
          }
          for (RawFile file : project.getRawSourceFiles()) {
            addedFiles.add(createRawFile(projectKey, FileData.RoleEnum.SOURCE, file.getFileName(),
                file.getContent()));
          }
          datastore.put(addedFiles);  // batch put
        }

        public void onNonFatalError() {
          for (FileData addedFile : addedFiles) {
            if (addedFile.isBlob && addedFile.blobstorePath != null) {
              deleteBlobstoreFile(addedFile.blobstorePath);
            }
          }
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
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId.t), e);
    }
    return projectId.t;
  }

  /*
   *  Creates and returns a new FileData object with the specified fields.
   *  Does not check for the existence of the object and does not update
   *  the database.
   */
  private FileData createRawFile(Key<ProjectData> projectKey, FileData.RoleEnum role,
      String fileName, byte[] content) throws ObjectifyException {
    FileData file = new FileData();
    file.fileName = fileName;
    file.projectKey = projectKey;
    file.role = role;
    if (useBlobstoreForFile(fileName)) {
      file.isBlob = true;
      file.blobstorePath = uploadToBlobstore(content);
    } else {
      file.content = content;
    }
    return file;
  }

  @Override
  public void deleteProject(final String userId, final long projectId) {
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
      // second job delete the project files and ProjectData in the project's
      // entity group
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<ProjectData> projectKey = projectKey(projectId);
          Query<FileData> fdq = datastore.query(FileData.class).ancestor(projectKey);
          datastore.delete(fdq);
          // finally, delete the ProjectData object
          datastore.delete(projectKey);
        }
      });
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
    final Result<String> projectType = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            projectType.t = pd.type;
          } else {
            projectType.t = "";
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return projectType.t;
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
  public long getProjectDateCreated(final String userId, final long projectId) {
    final Result<Long> dateCreated = new Result<Long>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            dateCreated.t = pd.dateCreated;
          } else {
            dateCreated.t = Long.valueOf(0);
          }
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return dateCreated.t;
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
          collectProjectErrorInfo(projectId, fileNames[0]), e);
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
          collectProjectErrorInfo(projectId, fileNames[0]), e);
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
          collectProjectErrorInfo(projectKey.getId(), fileName),
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
          collectProjectErrorInfo(projectId, fileNames[0]), e);
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
          collectProjectErrorInfo(projectId, fileNames[0]), e);
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
          throw CrashReport.createAndLogError(LOG, null, collectProjectErrorInfo(projectId, fileName),
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
      final String content, final String encoding) {
    try {
      return uploadRawFile(projectId, fileName, userId, content.getBytes(encoding));
    } catch (UnsupportedEncodingException e) {
      throw CrashReport.createAndLogError(LOG, null, "Unsupported file content encoding,"
          + collectProjectErrorInfo(projectId, fileName), e);
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
  public long uploadRawFile(final long projectId, final String fileName, final String userId,
      final byte[] content) {
    final Result<Long> modTime = new Result<Long>();
    final boolean useBlobstore = useBlobstoreForFile(fileName);
    final Result<String> oldBlobstorePath = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        FileData fd;

        @Override
        public void run(Objectify datastore) throws ObjectifyException {
          fd = datastore.find(projectFileKey(projectKey(projectId), fileName));
          Preconditions.checkState(fd != null);
          if (fd.isBlob) {
            // mark the old blobstore blob for deletion
           oldBlobstorePath.t = fd.blobstorePath;
          }
          if (useBlobstore) {
            fd.isBlob = true;
            fd.blobstorePath = uploadToBlobstore(content);
            // If the content was previously stored in the datastore, clear it out.
            fd.content = null;
          } else {
            fd.content = content;
          }
          datastore.put(fd);
          modTime.t = updateProjectModDate(datastore, projectId);
        }

        @Override
        public void onNonFatalError() throws ObjectifyException {
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
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(projectId, fileName), e);
    }
    return modTime.t;
  }

  private void deleteBlobstoreFile(String blobstorePath) {
    // It would be nice if there were an AppEngineFile.delete() method but alas there isn't, so we
    // have to get the BlobKey and delte via the BlobstoreService.
    AppEngineFile blobstoreFile = new AppEngineFile(blobstorePath);
    BlobKey blobKey = FileServiceFactory.getFileService().getBlobKey(blobstoreFile);
    BlobstoreServiceFactory.getBlobstoreService().delete(blobKey);
  }

  private String uploadToBlobstore(byte[] content) throws ObjectifyException {
    // Get a file service
    FileService fileService = FileServiceFactory.getFileService();

    // Create a new Blob file with generic mime-type "application/octet-stream"
    AppEngineFile blobstoreFile = null;
    try {
      blobstoreFile = fileService.createNewBlobFile("application/octet-stream");

      // Open a channel to write to it
      FileWriteChannel blobstoreWriteChannel = fileService.openWriteChannel(blobstoreFile, true);

      OutputStream blobstoreOutputStream = Channels.newOutputStream(blobstoreWriteChannel);
      ByteStreams.copy(ByteStreams.newInputStreamSupplier(content), blobstoreOutputStream);
      blobstoreOutputStream.flush();
      blobstoreOutputStream.close();
      blobstoreWriteChannel.closeFinally();
    } catch (IOException e) {
      throw new ObjectifyException(e);
    } catch (Exception ex) {
      throw new ObjectifyException(ex);
    }

    return blobstoreFile.getFullPath();
  }

  @VisibleForTesting
  boolean useBlobstoreForFile(String fileName) {
    return fileName.contains("assets/")
           || fileName.endsWith(".apk")
           || (fileName.contains("src/") && fileName.endsWith(".blk"));
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
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<FileData> fileKey = projectFileKey(projectKey(projectId), fileName);
          FileData fileData = datastore.find(fileKey);
          if (fileData != null) {
            oldBlobstorePath.t = fileData.blobstorePath;
          }
          datastore.delete(fileKey);
          modTime.t = updateProjectModDate(datastore, projectId);
        }
      });
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(projectId, fileName), e);
    }
    if (oldBlobstorePath.t != null) {
      deleteBlobstoreFile(oldBlobstorePath.t);
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
          + collectProjectErrorInfo(projectId, fileName), e);
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
          collectProjectErrorInfo(projectId, fileName), e);
    }
    FileData fileData = fd.t;
    if (fileData != null) {
      if (fileData.isBlob) {
        try {
          result.t = getBlobstoreBytes(fileData.blobstorePath);
        } catch (IOException e) {
          throw CrashReport.createAndLogError(LOG, null,
                                              collectProjectErrorInfo(projectId, fileName),
                                              new FileNotFoundException(fileName));
        }
      } else {
        result.t = fileData.content;
      }
    } else {
      throw CrashReport.createAndLogError(LOG, null,
                                          collectProjectErrorInfo(projectId, fileName),
                                          new FileNotFoundException(fileName));
    }
    return result.t;
  }

  private byte[] getBlobstoreBytes(String blobstorePath) throws IOException {
    AppEngineFile blobstoreFile = new AppEngineFile(blobstorePath);
    FileReadChannel blobstoreReadChannel =
        FileServiceFactory.getFileService().openReadChannel(blobstoreFile, false);
    InputStream blobstoreInputStream = Channels.newInputStream(blobstoreReadChannel);
    return ByteStreams.toByteArray(blobstoreInputStream);
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
  public ProjectSourceZip exportProjectSourceZip(final String userId, final long projectId,
                                                 final boolean includeProjectHistory,
                                                 final boolean includeAndroidKeystore,
                                                 @Nullable String zipName) throws IOException {
    final Result<Integer> fileCount = new Result<Integer>();
    fileCount.t = 0;
    final Result<String> projectName = new Result<String>();
    projectName.t = "";

    ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
    final ZipOutputStream out = new ZipOutputStream(zipFile);

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          try {
            Key<ProjectData> projectKey = projectKey(projectId);
            List<String> fileList = new ArrayList<String>();
            for (FileData fd : datastore.query(FileData.class).ancestor(projectKey)) {
              if (fd.role.equals(FileData.RoleEnum.SOURCE)) {
                String fileName = fd.fileName;

                if (fileName.equals(FileExporter.REMIX_INFORMATION_FILE_PATH)) {
                  // Skip legacy remix history files that were previous stored with the project
                  continue;
                }
                byte[] data;
                if (fd.isBlob) {
                  data = getBlobstoreBytes(fd.blobstorePath);
                } else {
                  data = fd.content;
                }
                out.putNextEntry(new ZipEntry(fileName));
                out.write(data, 0, data.length);
                out.closeEntry();
                fileCount.t++;
              }
            }

            if (fileCount.t > 0) {
              ProjectData pd = datastore.find(projectKey(projectId));
              if (includeProjectHistory) {
                if (!Strings.isNullOrEmpty(pd.history)) {
                  byte[] data = pd.history.getBytes(StorageUtil.DEFAULT_CHARSET);
                  out.putNextEntry(new ZipEntry(FileExporter.REMIX_INFORMATION_FILE_PATH));
                  out.write(data, 0, data.length);
                  out.closeEntry();
                  fileCount.t++;
                }
              }
              projectName.t = pd.name;
            }

          } catch (IOException e) {
            System.err.println("Unexpected io exception for userid " + userId +
                               " projectId " + projectId);
            throw CrashReport.createAndLogError(LOG, null,
                                                collectProjectErrorInfo(projectId, null), e);
          }
        }
      });
    } catch (ObjectifyException e) {
      System.err.println("Unexpected objectify exception for userid " + userId +
          " projectId " + projectId);
      CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(projectId, null), e);
      throw new IOException("Reflecting exception for userid " + userId +
          " projectId " + projectId);
    } catch (RuntimeException e) {
      System.err.println("Unexpected runtime exception for userid " + userId +
          " projectId " + projectId);
      throw new IOException("Reflecting exception for userid " + userId +
          " projectId " + projectId);
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
                  if (ufd.fileName.equals(ANDROID_KEYSTORE_FILENAME) && (ufd.content.length > 0)) {
                    out.putNextEntry(new ZipEntry(ANDROID_KEYSTORE_FILENAME));
                    out.write(ufd.content, 0, ufd.content.length);
                    out.closeEntry();
                    fileCount.t++;
                  }
                }
              } catch (IOException e) {
                System.err.println("Unexpected io exception for userid " + userId +
                                   " projectId " + projectId);
                throw CrashReport.createAndLogError(LOG, null,
                                                    collectProjectErrorInfo(projectId, null), e);
              }
            }
          });
      } catch (ObjectifyException e) {
        throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
      }
    }

    out.close();

    if (zipName == null) {
      zipName = projectName.t + ".zip";
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
        job.onNonFatalError();
      } finally {
        if (datastore.getTxn().isActive()) {
          datastore.getTxn().rollback();
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

  private static String collectProjectErrorInfo(final long projectId, final String fileName) {
    return "project=" + projectId + ", file=" + fileName;
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
}
