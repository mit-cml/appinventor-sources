// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.appidentity.AppIdentityServiceFailureException;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.apphosting.api.ApiProxy;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StoredData.CorruptionRecord;
import com.google.appinventor.server.storage.StoredData.FeedbackData;
import com.google.appinventor.server.storage.StoredData.FileData;
import com.google.appinventor.server.storage.StoredData.MotdData;
import com.google.appinventor.server.storage.StoredData.NonceData;
import com.google.appinventor.server.storage.StoredData.ProjectData;
import com.google.appinventor.server.storage.StoredData.SplashData;
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
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
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
  private static final long SPLASHDATA_ID = 1;

  // TODO(user): need a way to modify this. Also, what is really a good value?
  private static final int MAX_JOB_RETRIES = 10;

  private final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();

  private final GcsService gcsService;

  private static final String GCS_BUCKET_NAME;

  private static final long TWENTYFOURHOURS = 24*3600*1000; // 24 hours in milliseconds

  private final boolean useGcs = Flag.createFlag("use.gcs", true).get();

  private final boolean conversionEnabled = false; // We are converting GCS <=> Blobstore

  // Use this class to define the work of a job that can be
  // retried. The "datastore" argument to run() is the Objectify
  // object for this job (created with
  // ObjectifyService.beginTransaction() if a transaction is used or
  // ObjectifyService.begin if no transaction is used). Note that all
  // operations on "datastore" should be for objects in the same
  // entity group if a transaction is used.

  // Note: 1/25/2015: Added code to make the use of a transaction
  //                  optional.  In general we only need to use a
  //                  transaction where there work we would need to
  //                  rollback if an operation on the datastore
  //                  failed. We have not necessarily converted all
  //                  cases yet (out of a sense of caution). However
  //                  we have removed transaction in places where
  //                  doing so permits Objectify to use its global
  //                  cache (memcache) in a way that helps
  //                  performance.

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
    ObjectifyService.register(SplashData.class);

    // Learn GCS Bucket from App Configuration or App Engine Default
    String gcsBucket = Flag.createFlag("gcs.bucket", "").get();
    if (gcsBucket.equals("")) { // Attempt to get default bucket
                                // from AppIdentity Service
      AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
      try {
        gcsBucket = appIdentity.getDefaultGcsBucketName();
      } catch (AppIdentityServiceFailureException e) {
        // We get this exception when we are running on an App Engine instance
        // created before App Engine version 1.9.0 and we have neither configured
        // the GCS bucket in appengine-web.xml or used the App Engine console to
        // create the default bucket. The Default Bucket is a better approach for
        // personal instances because they have a default free quota of 5 Gb (as
        // of 5/29/2015 when this code was written).
        gcsBucket = ""; // This will cause a RunTimeException in the RPC code later
                        // which will log a better message
      }
      LOG.log(Level.INFO, "Default GCS Bucket Configured from App Identity: " + gcsBucket);
    }
    GCS_BUCKET_NAME = gcsBucket;
  }

  ObjectifyStorageIo() {
    RetryParams retryParams = new RetryParams.Builder().initialRetryDelayMillis(100)
      .retryMaxAttempts(10)
      .totalRetryPeriodMillis(10000).build();
    LOG.log(Level.INFO, "RetryParams: getInitialRetryDelayMillis() = " + retryParams.getInitialRetryDelayMillis());
    LOG.log(Level.INFO, "RetryParams: getRequestTimeoutMillis() = " + retryParams.getRequestTimeoutMillis());
    LOG.log(Level.INFO, "RetryParams: getRetryDelayBackoffFactor() = " + retryParams.getRetryDelayBackoffFactor());
    LOG.log(Level.INFO, "RetryParams: getRetryMaxAttempts() = " + retryParams.getRetryMaxAttempts());
    LOG.log(Level.INFO, "RetryParams: getRetryMinAttempts() = " + retryParams.getRetryMinAttempts());
    LOG.log(Level.INFO, "RetryParams: getTotalRetryPeriodMillis() = " + retryParams.getTotalRetryPeriodMillis());
    gcsService = GcsServiceFactory.createGcsService(retryParams);
    memcache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
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
      if (tuser.getUserName()==null) {
        setUserName(userId,tuser.getDefaultName());
        tuser.setUserName(tuser.getDefaultName());
      }
      return tuser;
    } else {                    // If not in memcache, or tos
                                // not yet accepted, fetch from datastore
        tuser = new User(userId, email, null, null, 0, false, false, 0, null);
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
          if(userData.emailFrequency == 0){
            // when users of old version access UserData,
            // emailFrequency will be automatically set as 0
            // force it to be DEFAULT_EMAIL_NOTIFICATION_FREQUENCY
            userData.emailFrequency = User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY;
            datastore.put(userData);
          }
          user.setUserEmail(userData.email);
          user.setUserName(userData.name);
          user.setUserLink(userData.link);
          user.setUserEmailFrequency(userData.emailFrequency);
          user.setType(userData.type);
          user.setUserTosAccepted(userData.tosAccepted || !requireTos.get());
          user.setSessionId(userData.sessionid);
        }
      }, false);                // Transaction not needed. If we fail there is nothing to rollback
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
    userData.name = User.getDefaultName(email);
    userData.type = User.USER;
    userData.link = "";
    userData.emailFrequency = User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY;
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
      }, true);
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
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserName(final String userId, final String name) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.name = name;
            datastore.put(userData);
          }
          // we need to change the memcache version of user
          User user = new User(userData.id,userData.email,name, userData.link, userData.emailFrequency, userData.tosAccepted,
              false, userData.type, userData.sessionid);
          String cachekey = User.usercachekey + "|" + userId;
          memcache.put(cachekey, user, Expiration.byDeltaSeconds(60)); // Remember for one minute
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }

  }

  @Override
  public void setUserLink(final String userId, final String link) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.link = link;
            datastore.put(userData);
          }
          // we need to change the memcache version of user
          User user = new User(userData.id,userData.email,userData.name,link,userData.emailFrequency,userData.tosAccepted,
              false, userData.type, userData.sessionid);
          String cachekey = User.usercachekey + "|" + userId;
          memcache.put(cachekey, user, Expiration.byDeltaSeconds(60)); // Remember for one minute
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void setUserEmailFrequency(final String userId, final int emailFrequency) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(userKey(userId));
          if (userData != null) {
            userData.emailFrequency = emailFrequency;
            datastore.put(userData);
          }
          // we need to change the memcache version of user
          User user = new User(userData.id,userData.email,userData.name,userData.link,emailFrequency,userData.tosAccepted,
              false, userData.type, userData.sessionid);
          String cachekey = User.usercachekey + "|" + userId;
          memcache.put(cachekey, user, Expiration.byDeltaSeconds(60)); // Remember for one minute
        }
      }, true);
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
      }, false);
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
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
    return settings.t;
  }

  @Override
  public String getUserName(final String userId) {
    final Result<String> name = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(UserData.class, userId);
          if (userData != null) {
            name.t = userData.name;
          } else {
            name.t = "unknown";
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
    return name.t;
  }

  @Override
  public String getUserLink(final String userId) {
    final Result<String> link = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(UserData.class, userId);
          if (userData != null) {
            link.t = userData.link;
          } else {
            link.t = "unknown";
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
    return link.t;
  }

  @Override
  public int getUserEmailFrequency(final String userId) {
    final Result<Integer> emailFrequency = new Result<Integer>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          UserData userData = datastore.find(UserData.class, userId);
          if (userData != null) {
            emailFrequency.t = userData.emailFrequency;
          } else {
            emailFrequency.t = User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY;
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
    return emailFrequency.t;
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
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public long createProject(final String userId, final Project project,
      final String projectSettings) {
    final Result<Long> projectId = new Result<Long>();
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
          pd.galleryId = UserProject.NOTPUBLISHED;
          pd.attributionId = UserProject.FROMSCRATCH;
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
              addedFiles.add(createRawFile(projectKey, FileData.RoleEnum.SOURCE, userId,
                  file.getFileName(), file.getContent().getBytes(DEFAULT_ENCODING)));
            } catch (IOException e) { // GCS throws this
              throw CrashReport.createAndLogError(LOG, null,
                collectProjectErrorInfo(userId, projectId.t, file.getFileName()), e);
            }
          }
          for (RawFile file : project.getRawSourceFiles()) {
            try {
              addedFiles.add(createRawFile(projectKey, FileData.RoleEnum.SOURCE, userId, file.getFileName(),
                  file.getContent()));
            } catch (IOException e) {
              throw CrashReport.createAndLogError(LOG, null,
                collectProjectErrorInfo(userId, projectId.t, file.getFileName()), e);
            }
          }
          datastore.put(addedFiles);  // batch put
        }

        @Override
        public void onNonFatalError() {
        }

      }, true);

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
      }, true);
    } catch (ObjectifyException e) {
      for (FileData addedFile : addedFiles) {
        if (isTrue(addedFile.isGCS)) {  // Do something
          if (addedFile.gcsName != null) {
            try {
              gcsService.delete(new GcsFilename(GCS_BUCKET_NAME, addedFile.gcsName));
            } catch (IOException ee) {
              LOG.log(Level.WARNING, "Unable to delete " + addedFile.gcsName +
                " from GCS while aborting project creation.", ee);
          }
        }
      }
      // clear addedFiles in case we end up here more than once
      addedFiles.clear();
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId.t), e);
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
    String userId, String fileName, byte[] content) throws ObjectifyException, IOException {
    validateGCS();
    FileData file = new FileData();
    file.fileName = fileName;
    file.projectKey = projectKey;
    file.role = role;
    file.userId = userId;
    if (useGCSforFile(fileName, content.length)) {
      file.isGCS = true;
      file.gcsName = makeGCSfileName(fileName, projectKey.getId());
      GcsOutputChannel outputChannel =
        gcsService.createOrReplace(new GcsFilename(GCS_BUCKET_NAME, file.gcsName), GcsFileOptions.getDefaultInstance());
      outputChannel.write(ByteBuffer.wrap(content));
      outputChannel.close();
    } else {
      file.content = content;
    }
    return file;
  }

  @Override
  public void deleteProject(final String userId, final long projectId) {
    validateGCS();
    // blobs associated with the project
    final List<String> blobKeys = new ArrayList<String>();
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
      }, true);
      // second job deletes the project files and ProjectData in the project's
      // entity group
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<ProjectData> projectKey = projectKey(projectId);
          Query<FileData> fdq = datastore.query(FileData.class).ancestor(projectKey);
          for (FileData fd: fdq) {
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
      // have to delete the blobs outside of the user and project jobs
      for (String blobKeyString: blobKeys) {
        deleteBlobstoreFile(blobKeyString);
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
  public void setProjectGalleryId(final String userId, final long projectId,final long galleryId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData projectData = datastore.find(projectKey(projectId));
          if (projectData != null) {
            projectData.galleryId = galleryId;
            datastore.put(projectData);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }
  }
  @Override
  public void setProjectAttributionId(final String userId, final long projectId,final long attributionId) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData projectData = datastore.find(projectKey(projectId));
          if (projectData != null) {
            projectData.attributionId = attributionId;
            datastore.put(projectData);
          }
        }
      }, true);
    } catch (ObjectifyException e) {
       throw CrashReport.createAndLogError(LOG, null,"error in setProjectAttributionId",  e);
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
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }

    return projects;
  }

  @Override
  public String loadProjectSettings(final String userId, final long projectId) {
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
      }, false);
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
      }, false);
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
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    if (projectData.t == null) {
      return null;
    } else {
      return new UserProject(projectId, projectData.t.name,
          projectData.t.type, projectData.t.dateCreated,
          projectData.t.dateModified, projectData.t.galleryId,
          projectData.t.attributionId);
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
      }, false);
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
            modDate.t = UserProject.NOTPUBLISHED;
          }
        }
      }, false); // Transaction not needed, and we want the caching we get if we don't
                 // use them.
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return modDate.t;
  }

  @Override
  public String getProjectHistory(final String userId, final long projectId) {
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
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return projectHistory.t;
  }

  // JIS XXX

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
            dateCreated.t = UserProject.NOTPUBLISHED;
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return dateCreated.t;
  }

  @Override
  public long getProjectGalleryId(String userId, final long projectId) {
    final Result<Long> galleryId = new Result<Long>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            galleryId.t = pd.galleryId;
          } else {
            galleryId.t = UserProject.NOTPUBLISHED;
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG,
          null,"error in getProjectGalleryId", e);
    }
    return galleryId.t;
  }
  @Override
  public long getProjectAttributionId(final long projectId) {
    final Result<Long> attributionId = new Result<Long>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          ProjectData pd = datastore.find(projectKey(projectId));
          if (pd != null) {
            attributionId.t = pd.attributionId;
          } else {
            attributionId.t = UserProject.FROMSCRATCH;
          }
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          "error in getProjectAttributionId", e);
    }
    return attributionId.t;
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
      }, true);
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
      }, false);
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
      }, true);
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
      }, true);
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
      }, true);
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
      }, true);
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
      }, true);
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
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          addFilesToProject(datastore, projectId, FileData.RoleEnum.SOURCE, changeModDate, userId, fileNames);
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileNames[0]), e);
    }
  }

  @Override
  public void addOutputFilesToProject(final String userId, final long projectId,
    final String... fileNames) {
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          addFilesToProject(datastore, projectId, FileData.RoleEnum.TARGET, false, userId, fileNames);
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileNames[0]), e);
    }
  }

  private void addFilesToProject(Objectify datastore, long projectId, FileData.RoleEnum role,
    boolean changeModDate, String userId, String... fileNames) {
    List<FileData> addedFiles = new ArrayList<FileData>();
    Key<ProjectData> projectKey = projectKey(projectId);
    for (String fileName : fileNames) {
      FileData fd = createProjectFile(datastore, projectKey, role, fileName);
      if (fd != null) {
        fd.userId = userId;
        addedFiles.add(fd);
      }
    }
    datastore.put(addedFiles); // batch put
    if (changeModDate) {
      updateProjectModDate(datastore, projectId, false);
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
      }, true);
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
      }, true);
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
      Key<FileData> key = projectFileKey(projectKey, fileName);
      memcache.delete(key.getString()); // Remove it from memcache (if it is there)
      FileData fd = datastore.find(key);
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
      updateProjectModDate(datastore, projectId, false);
    }
  }

  @Override
  public List<String> getProjectSourceFiles(final String userId, final long projectId) {
    final Result<List<String>> result = new Result<List<String>>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          result.t = getProjectFiles(datastore, projectId, FileData.RoleEnum.SOURCE);
        }
      }, false);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId), e);
    }
    return result.t;
  }

  @Override
  public List<String> getProjectOutputFiles(final String userId, final long projectId) {
   final Result<List<String>> result = new Result<List<String>>();
   try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          result.t = getProjectFiles(datastore, projectId, FileData.RoleEnum.TARGET);
        }
      }, false);
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

  private long updateProjectModDate(Objectify datastore, long projectId, boolean doingConversion) {
    long modDate = System.currentTimeMillis();
    ProjectData pd = datastore.find(projectKey(projectId));
    if (pd != null) {
      // Only update the ProjectData dateModified if it is more then a minute
      // in the future. Do this to avoid unnecessary datastore puts.
      // Also do not update modification time when doing conversion from
      // blobstore to GCS
      if ((modDate > (pd.dateModified + 1000*60)) && !doingConversion) {
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
    return uploadRawFile(projectId, fileName, userId, force, content, false);
  }

  private long uploadRawFile(final long projectId, final String fileName, final String userId,
      final boolean force, final byte[] content, final boolean doingConversion) throws BlocksTruncatedException {
    validateGCS();
    final Result<Long> modTime = new Result<Long>();
    final boolean useGCS = useGCSforFile(fileName, content.length);
    final Result<String> oldBlobstoreKey = new Result<String>();
    final boolean considerBackup = (useGcs?((fileName.contains("src/") && fileName.endsWith(".blk")) // AI1 Blocks Files
        || (fileName.contains("src/") && fileName.endsWith(".bky")) // Blockly files
        || (fileName.contains("src/") && fileName.endsWith(".scm"))) // Form Definitions
      :false);

    try {
      runJobWithRetries(new JobRetryHelper() {
        FileData fd;

        @Override
        public void run(Objectify datastore) throws ObjectifyException {
          Key<FileData> key = projectFileKey(projectKey(projectId), fileName);
          fd = (FileData) memcache.get(key.getString());
          if (fd == null) {
            fd = datastore.find(projectFileKey(projectKey(projectId), fileName));
          } else {
            LOG.log(Level.INFO, "Fetched " + key.getString() + " from memcache.");
          }

          // <Screen>.yail files are missing when user converts AI1 project to AI2
          // instead of blowing up, just create a <Screen>.yail file
          if (fd == null && fileName.endsWith(".yail")){
            fd = createProjectFile(datastore, projectKey(projectId), FileData.RoleEnum.SOURCE, fileName);
            fd.userId = userId;
          }

          Preconditions.checkState(fd != null);

          if (fd.userId != null && !fd.userId.equals("")) {
            if (!fd.userId.equals(userId)) {
              throw CrashReport.createAndLogError(LOG, null,
                collectUserProjectErrorInfo(userId, projectId),
                new UnauthorizedAccessException(userId, projectId, null));
            }
          }

          if ((content.length < 125) && (fileName.endsWith(".bky"))) { // Likely this is an empty blocks workspace
            if (!force) {            // force is true if we *really* want to save it!
              checkForBlocksTruncation(fd); // See if we had previous content and throw and exception if so
            }
          }

          if (fd.isBlob) {
            // mark the old blobstore blob for deletion
           oldBlobstoreKey.t = fd.blobKey;
          }
          if (useGCS) {
            fd.isGCS = true;
            fd.gcsName = makeGCSfileName(fileName, projectId);
            try {
              GcsOutputChannel outputChannel =
                gcsService.createOrReplace(new GcsFilename(GCS_BUCKET_NAME, fd.gcsName), GcsFileOptions.getDefaultInstance());
              outputChannel.write(ByteBuffer.wrap(content));
              outputChannel.close();
            } catch (IOException e) {
              throw CrashReport.createAndLogError(LOG, null,
                collectProjectErrorInfo(userId, projectId, fileName), e);
            }
            // If the content was previously stored in the datastore, clear it out.
            fd.content = null;
            fd.isBlob = false;  // in case we are converting from a blob
            fd.blobstorePath = null;
          } else {
            if (isTrue(fd.isGCS)) {     // Was a GCS file, must have gotten smaller
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
          if (considerBackup && !doingConversion) {
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
          // Old file not marked with ownership, mark it now
          if (fd.userId == null || fd.userId.equals("")) {
            fd.userId = userId;
          }
          datastore.put(fd);
          memcache.put(key.getString(), fd); // Store the updated data in memcache
          modTime.t = updateProjectModDate(datastore, projectId, doingConversion);
        }

        @Override
        public void onNonFatalError() {
          if (fd != null && fd.blobKey != null) {
            oldBlobstoreKey.t = fd.blobKey;
          }
        }
      }, false);        // Use transaction for blobstore, otherwise we don't need one
                               // and without one the caching code comes into play.

      // It would have been convenient to delete the old blobstore file within the run() method
      // above but that caused an exception where the app engine datastore claimed to be doing
      // operations on multiple entity groups within the same transaction.  Apparently the blobstore
      // operations are, at least partially, also datastore operations.
      if (oldBlobstoreKey.t != null) {
        deleteBlobstoreFile(oldBlobstoreKey.t);
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

  protected void deleteBlobstoreFile(String blobKeyString) {
    // It would be nice if there were an AppEngineFile.delete() method but alas there isn't, so we
    // have to get the BlobKey and delete via the BlobstoreService.
    BlobKey blobKey = null;
    try {
      blobKey = new BlobKey(blobKeyString);
      BlobstoreServiceFactory.getBlobstoreService().delete(blobKey);
    } catch (RuntimeException e) {
      // Log blob delete errors but don't make them fatal
      CrashReport.createAndLogError(LOG, null, "Error deleting blob with blobKey " +
        blobKey, e);
    }
  }

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
    validateGCS();
    final Result<Long> modTime = new Result<Long>();
    final Result<String> oldBlobKeyString = new Result<String>();
    final Result<String> oldgcsName = new Result<String>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<FileData> fileKey = projectFileKey(projectKey(projectId), fileName);
          memcache.delete(fileKey.getString());
          FileData fileData = datastore.find(fileKey);
          if (fileData != null) {
            if (fileData.userId != null && !fileData.userId.equals("")) {
              if (!fileData.userId.equals(userId)) {
                throw CrashReport.createAndLogError(LOG, null,
                  collectUserProjectErrorInfo(userId, projectId),
                  new UnauthorizedAccessException(userId, projectId, null));
              }
            }
            oldBlobKeyString.t = fileData.blobKey;
            if (isTrue(fileData.isGCS)) {
              oldgcsName.t = fileData.gcsName;
            }
          }
          datastore.delete(fileKey);
          modTime.t = updateProjectModDate(datastore, projectId, false);
        }
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileName), e);
    }
    if (oldBlobKeyString.t != null) {
      deleteBlobstoreFile(oldBlobKeyString.t);
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
      }, true);
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  @Override
  public byte[] downloadRawFile(final String userId, final long projectId, final String fileName) {
    validateGCS();
    final Result<byte[]> result = new Result<byte[]>();
    final Result<FileData> fd = new Result<FileData>();
    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          Key<FileData> fileKey = projectFileKey(projectKey(projectId), fileName);
          fd.t = (FileData) memcache.get(fileKey.getString());
          if (fd.t == null) {
            fd.t = datastore.find(fileKey);
          }
        }
      }, false); // Transaction not needed
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null,
          collectProjectErrorInfo(userId, projectId, fileName), e);
    }
    // read the blob/GCS File outside of the job
    FileData fileData = fd.t;
    if (fileData != null) {
      if (fileData.userId != null && !fileData.userId.equals("")) {
        if (!fileData.userId.equals(userId)) {
          throw CrashReport.createAndLogError(LOG, null,
            collectUserProjectErrorInfo(userId, projectId),
            new UnauthorizedAccessException(userId, projectId, null));
        }
      }
      if (isTrue(fileData.isGCS)) {     // It's in the Cloud Store
        try {
          int count;
          boolean npfHappened = false;
          boolean recovered = false;
          for (count = 0; count < 5; count++) {
            GcsFilename gcsFileName = new GcsFilename(GCS_BUCKET_NAME, fileData.gcsName);
            int bytesRead = 0;
            int fileSize = 0;
            ByteBuffer resultBuffer;
            try {
              fileSize = (int) gcsService.getMetadata(gcsFileName).getLength();
              resultBuffer = ByteBuffer.allocate(fileSize);
              GcsInputChannel readChannel = gcsService.openReadChannel(gcsFileName, 0);
              try {
                while (bytesRead < fileSize) {
                  bytesRead += readChannel.read(resultBuffer);
                  if (bytesRead < fileSize) {
                    LOG.log(Level.INFO, "readChannel: bytesRead = " + bytesRead + " fileSize = " + fileSize);
                  }
                }
                recovered = true;
                result.t = resultBuffer.array();
                // Should we downgrade to the blobstore (for debugging)?
                // Note: We only run if we have at least 5 seconds of runtime left in the request
                long timeRemaining = ApiProxy.getCurrentEnvironment().getRemainingMillis();
                if (conversionEnabled && !useGcs && (timeRemaining > 5000)) {
                  // Garf, Let's downgrade this file to the blobstore!
                  // This is used for debugging -- so we can retry upgrading by
                  // first downgrading!
                  // Note: uploadRawFile will do the work!
                  LOG.log(Level.INFO, "Downgrading " + fileName + " with " +
                    timeRemaining + " left on the clock.");
                  try {
                    uploadRawFile(projectId, fileName, userId, true /* force */,
                      result.t, true /* no project timestamp update */);
                  } catch (BlocksTruncatedException e) {
                    /* will never happen because force is true */
                  }
                }

                break;          // We got the data, break out of the loop!
              } finally {
                readChannel.close();
              }
            } catch (NullPointerException e) {
              // This happens if the object in GCS is non-existent, which would happen
              // when people uploaded a zero length object. As of this change, we now
              // store zero length objects into GCS, but there are plenty of older objects
              // that are missing in GCS.
              LOG.log(Level.WARNING, "downloadrawfile: NPF recorded for " + fileData.gcsName);
              npfHappened = true;
              resultBuffer = ByteBuffer.allocate(0);
              result.t = resultBuffer.array();
            }
          }

          // report out on how things went above
          if (npfHappened) {    // We lost at least once
            if (recovered) {
              LOG.log(Level.WARNING, "recovered from NPF in downloadrawfile filename = " + fileData.gcsName +
                " count = " + count);
            } else {
              LOG.log(Level.WARNING, "FATAL NPF in downloadrawfile filename = " + fileData.gcsName);
            }
          }

        } catch (IOException e) {
          throw CrashReport.createAndLogError(LOG, null,
              collectProjectErrorInfo(userId, projectId, fileName), e);
        }
      } else if (fileData.isBlob) {
        try {
          if (fileData.blobKey == null) {
            throw new BlobReadException("blobKey is null");
          }
          result.t = getBlobstoreBytes(fileData.blobKey);
          // Time to consider upgrading this file if we are moving to GCS
          // Note: We only run if we have at least 5 seconds of runtime left in the request
          long timeRemaining = ApiProxy.getCurrentEnvironment().getRemainingMillis();
          if (conversionEnabled && useGcs && (timeRemaining > 5000)) {
            // Upgrade the file to use GCS
            // Note: uploadRawFile does the work for us!
            LOG.log(Level.INFO, "Upgrading " + fileName + " with " +
              timeRemaining + " left on the clock.");
            try {
              uploadRawFile(projectId, fileName, userId, true /* force */,
                result.t, true /* no project timestamp update */);
            } catch (BlocksTruncatedException e) {
              /* will never happen because force is true */
            }
          }
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
  private byte[] getBlobstoreBytes(String blobKeyString) throws BlobReadException {
    BlobKey blobKey = new BlobKey(blobKeyString);
    if (blobKey == null) {
      throw new BlobReadException("Could not find BlobKey for " + blobKeyString);
    }
    try {
      InputStream blobInputStream = new BlobstoreInputStream(blobKey);
      return ByteStreams.toByteArray(blobInputStream);
    } catch (IOException e) {
      throw new BlobReadException(e, "Error trying to read blob from " + blobKey);
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
                                                 @Nullable String zipName,
                                                 final boolean includeYail,
                                                 final boolean fatalError) throws IOException {
    validateGCS();
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
              if (fileName.endsWith(".yail") && !includeYail) {
                // Don't include YAIL files when exporting projects
                // includeYail will be set to true when we are exporting the source
                // to send to the buildserver or when the person exporting
                // a project is an Admin (for debugging).
                // Otherwise Yail files are confusing cruft. In the case of
                // the Firebase Component they may contain secrets which we would
                // rather not have leak into an export .aia file or into the Gallery
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
      }, false);

      // Process the file contents outside of the job since we can't read
      // blobs in the job.
      for (FileData fd : fileData) {
        fileName = fd.fileName;
        byte[] data = null;
        if (fd.isBlob) {
          try {
            if (fd.blobKey == null) {
              throw new BlobReadException("blobKey is null");
            }
            data = getBlobstoreBytes(fd.blobKey);
          } catch (BlobReadException e) {
            throw CrashReport.createAndLogError(LOG, null,
                collectProjectErrorInfo(userId, projectId, fileName), e);
          }
        } else if (isTrue(fd.isGCS)) {
          try {
            int count;
            boolean npfHappened = false;
            boolean recovered = false;
            for (count = 0; count < 5; count++) {
              GcsFilename gcsFileName = new GcsFilename(GCS_BUCKET_NAME, fd.gcsName);
              int bytesRead = 0;
              int fileSize = 0;
              ByteBuffer resultBuffer;
              try {
                fileSize = (int) gcsService.getMetadata(gcsFileName).getLength();
                resultBuffer = ByteBuffer.allocate(fileSize);
                GcsInputChannel readChannel = gcsService.openReadChannel(gcsFileName, 0);
                try {
                  while (bytesRead < fileSize) {
                    bytesRead += readChannel.read(resultBuffer);
                    if (bytesRead < fileSize) {
                      LOG.log(Level.INFO, "readChannel: bytesRead = " + bytesRead + " fileSize = " + fileSize);
                    }
                  }
                  recovered = true;
                  data = resultBuffer.array();
                  break;        // We got the data, break out of the loop!
                } finally {
                  readChannel.close();
                }
              } catch (NullPointerException e) {
                // This happens if the object in GCS is non-existent, which would happen
                // when people uploaded a zero length object. As of this change, we now
                // store zero length objects into GCS, but there are plenty of older objects
                // that are missing in GCS.
                LOG.log(Level.WARNING, "exportProjectFile: NPF recorded for " + fd.gcsName);
                npfHappened = true;
                resultBuffer = ByteBuffer.allocate(0);
                data = resultBuffer.array();
              }
            }

            // report out on how things went above
            if (npfHappened) {    // We lost at least once
              if (recovered) {
                LOG.log(Level.WARNING, "recovered from NPF in exportProjectFile filename = " + fd.gcsName +
                  " count = " + count);
              } else {
                LOG.log(Level.WARNING, "FATAL NPF in exportProjectFile filename = " + fd.gcsName);
                if (fatalError) {
                  throw new IOException("FATAL Error reading file from GCS filename = " + fd.gcsName);
                }
              }
            }
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
        }, false);
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
      }, false);
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
      }, true);
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
      }, false);
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
      }, true);
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
      }, true);
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
   * Call job.run() if we get a {@link java.util.ConcurrentModificationException}
   * or {@link com.google.appinventor.server.storage.ObjectifyException}
   * we will retry the job (at most {@code MAX_JOB_RETRIES times}).
   * Any other exception will cause the job to fail immediately.
   * If useTransaction is true, create a transaction and run the job in
   * that transaction. If the job terminates normally, commit the transaction.
   *
   * Note: Originally we ran all jobs in a transaction. However in
   *       many places there is no need for a transaction because
   *       there is nothing to rollback on failure. Using transactions
   *       has a performance implication, it disables Objectify's
   *       ability to use memcache.
   *
   * @param job
   * @param useTransaction -- Set to true to run job in a transaction
   * @throws ObjectifyException
   */
  @VisibleForTesting
  void runJobWithRetries(JobRetryHelper job, boolean useTransaction) throws ObjectifyException {
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
        if (message != null && message.startsWith("Blocks")) { // This one is fatal!
          throw oe;
        }
        // maybe this should be a fatal error? I think only thing
        // that creates this exception is this method.
        job.onNonFatalError();
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
  boolean isGcsFile(long projectId, String fileName) {
    Objectify datastore = ObjectifyService.begin();
    Key<FileData> fileKey = projectFileKey(projectKey(projectId), fileName);
    FileData fd;
    fd = (FileData) memcache.get(fileKey.getString());
    if (fd == null) {
      fd = datastore.find(fileKey);
    }
    if (fd != null) {
      return isTrue(fd.isGCS);
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
    if (fd.isBlob || isTrue(fd.isGCS) || fd.content.length > 120)
      throw new ObjectifyException("BlocksTruncated"); // Hack
    // I'm avoiding having to modify every use of runJobWithRetries to handle a new
    // exception, so we use this dodge.
  }

  // Make sure we throw an exception if the GCS bucket isn't defined. This hopefully
  // will prompt the person deploying App Inventor to check the server logs and see
  // the message below.
  //
  // This only happens when deploying code that uses GCS but doesn't specify a bucket
  // name in appengine-web.xml *AND* the instance was created before App Engine version
  // 1.9.0. Apps created after 1.9.0 automatically have a default bucket created for
  // them. Older Apps can configure a default bucket. The App Engine documentation
  // explains how.

  private void validateGCS() {
    if (useGcs && GCS_BUCKET_NAME.equals("")) {
      try {
        throw new RuntimeException("You need to configure the default GCS Bucket for your App. " +
          "Follow instructions in the App Engine Developer's Documentation");
      } catch (RuntimeException e) {
        throw CrashReport.createAndLogError(LOG, null, null, e);
      }
    }
  }

  // See if this person needs to have their projects upgraded and if so
  // add a task to the task queue to take care of it
  public void checkUpgrade(String userId) {
    if (!conversionEnabled)     // Unless conversion is enabled...
      return;
    Objectify datastore = ObjectifyService.begin();
    UserData userData = datastore.find(userKey(userId));
    if ((userData.upgradedGCS && useGcs) ||
      (!userData.upgradedGCS && !useGcs))
      return;                   // All done.
    Queue queue = QueueFactory.getQueue("blobupgrade");
    queue.add(TaskOptions.Builder.withUrl("/convert").param("user", userId)
      .etaMillis(System.currentTimeMillis() + 60000));
    return;
  }

  public void doUpgrade(String userId) {
    if (!conversionEnabled)     // Unless conversion is enabled...
      return;                   // shouldn't really ever happen but...
    Objectify datastore = ObjectifyService.begin();
    UserData userData = datastore.find(userKey(userId));
    if ((userData.upgradedGCS && useGcs) ||
      (!userData.upgradedGCS && !useGcs))
      return;                   // All done, another task did it!
    List<Long> projectIds = getProjects(userId);
    boolean anyFailed = false;
    for (long projectId : projectIds) {
      for (FileData fd : datastore.query(FileData.class).ancestor(projectKey(projectId))) {
        if (fd.isBlob) {
          if (useGcs) {         // Let's convert by just reading it!
            downloadRawFile(userId, projectId, fd.fileName);
          }
        } else if (isTrue(fd.isGCS)) {
          if (!useGcs) {        // Let's downgrade by just reading it!
            downloadRawFile(userId, projectId, fd.fileName);
          }
        }
      }
    }

    /*
     * If we are running low on time, we may have not moved all files
     * so exit now without marking the user as having been finished
     */
    if (ApiProxy.getCurrentEnvironment().getRemainingMillis() <= 5000)
      return;

    /* If anything failed, also return without marking user */
    if (anyFailed)
      return;

    datastore = ObjectifyService.beginTransaction();
    userData = datastore.find(userKey(userId));
    userData.upgradedGCS = useGcs;
    datastore.put(userData);
    datastore.getTxn().commit();
  }

  public SplashConfig getSplashConfig() {
    final Result<SplashConfig> result = new Result<SplashConfig>();
    try {
      runJobWithRetries(new JobRetryHelper() {
          @Override
          public void run(Objectify datastore) {
            // Fixed key because only one record
            SplashData sd = datastore.find(SplashData.class, SPLASHDATA_ID);
            if (sd == null) {   // If we don't have Splash Data, create it
              SplashData firstSd = new SplashData(); // We do this so cacheing works
              firstSd.id = SPLASHDATA_ID;
              firstSd.version = 0;                   // on future calls
              firstSd.content = "<b>Welcome to MIT App Inventor</b>";
              firstSd.width = 350;
              firstSd.height = 100;
              datastore.put(firstSd);
              result.t = new SplashConfig(0, firstSd.width, firstSd.height, firstSd.content);
            } else {
              result.t = new SplashConfig(sd.version, sd.width, sd.height, sd.content);
            }
          }
        }, false);             // No transaction, Objectify will cache
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
    return result.t;
  }

  private boolean isTrue(Boolean b) {
    if (b != null && b) {
      return true;
    } else {
      return false;
    }
  }

}
