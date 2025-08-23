package com.google.appinventor.server.storage;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.cache.CacheService;
import com.google.appinventor.server.storage.database.DatabaseAccessException;
import com.google.appinventor.server.storage.database.DatabaseService;
import com.google.appinventor.server.storage.filesystem.FilesystemService;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class ModularizedStorageIo implements StorageIo {
  private static final Flag<Boolean> REQUIRE_TOS = Flag.createFlag("require.tos", false);

  private static final Logger LOG = Logger.getLogger(ModularizedStorageIo.class.getName());

  private static final int MAX_FILE_SIZE_BYTES_IN_DB = 50_000;

  private final static String CACHE_KEY_PREFIX__USER = "f682688a-1065-4cda-8515-a8bd70200ac9";
  private final static String CACHE_KEY_PREFIX__BUILD_STATUS = "40bae275-070f-478b-9a5f-d50361809b99";
  private static final String CACHE_KEY_PREFIX__PROJECT_OWNER = "cf452c52-839a-48e2-a3fc-ef77c87e09c2";

  private final CacheService cacheService = CacheService.getCacheService();
  private final DatabaseService databaseService = DatabaseService.getDatabaseService();
  private final FilesystemService filesystemService = FilesystemService.getFilesystemService();

  public ModularizedStorageIo() {
  }

  public User getUser(final String userId, final String email) {
    final String cacheKey = CACHE_KEY_PREFIX__USER + "|" + userId;

    final User cachedUser = (User) cacheService.get(cacheKey);
    if (cachedUser != null && cachedUser.getUserTosAccepted() && ((email == null) || (cachedUser.getUserEmail().equals(email)))) {
      return cachedUser;
    }

    // If not in memcache, or tos not yet accepted, fetch from datastore
    final User user = databaseService.findOrCreateUser(userId, email, REQUIRE_TOS.get());

    cacheService.put(cacheKey, user, 60); // Remember for one minute
    // The choice of one minute here is arbitrary. getUser() is called on every authenticated
    // RPC call to the system (out of OdeAuthFilter), so using memcache will save a significant
    // number of calls to the datastore. If someone is idle for more then a minute, it isn't
    // unreasonable to hit the datastore again. By pruning memcache ourselves, we have a
    // bit more control (maybe) of how things are flushed from memcache. Otherwise we are
    // at the whim of whatever algorithm App Engine employs now or in the future.

    return user;
  }

  // Get User from email address alone. This version will create the user
  // if they don't exist
  @Override
  public User getUserFromEmail(String email) {
    String emaillower = email.toLowerCase();
    LOG.info("getUserFromEmail: email = " + email + " emaillower = " + emaillower);
    return databaseService.getUserFromEmail(emaillower);
  }

  @Override
  public void setTosAccepted(final String userId) {
    databaseService.setTosAccepted(userId);
  }

  @Override
  public void setUserEmail(final String userId, String inputemail) {
    final String email = inputemail.toLowerCase();
    databaseService.setUserEmail(userId, email);
  }

  @Override
  public void setUserSessionId(final String userId, final String sessionId) {
    final String cacheKey = CACHE_KEY_PREFIX__USER + "|" + userId;
    cacheService.delete(cacheKey);
    databaseService.setUserSessionId(userId, sessionId);
  }

  @Override
  public void setUserPassword(final String userId, final String password) {
    final String cacheKey = CACHE_KEY_PREFIX__USER + "|" + userId;
    cacheService.delete(cacheKey);
    databaseService.setUserPassword(userId, password);
  }

  @Override
  public String loadSettings(final String userId) {
    return databaseService.loadUserDataSettings(userId);
  }

  @Override
  public void storeSettings(final String userId, final String settings) {
    databaseService.storeUserDataSettings(userId, settings);
  }

  @Override
  public long createProject(final String userId, final Project project,
                            final String projectSettings) {
    final List<UnifiedFile> unifiedFiles = new ArrayList<>();
    for (TextFile file : project.getSourceFiles()) {
      unifiedFiles.add(new UnifiedFile(file.getFileName(), file.getContent().getBytes()));
    }
    for (RawFile file : project.getRawSourceFiles()) {
      unifiedFiles.add(new UnifiedFile(file.getFileName(), file.getContent()));
    }

    try {
      final Long projectId = databaseService.createProjectData(project, projectSettings);

      for (UnifiedFile file : unifiedFiles) {
        final String filename = file.getFileName();
        final byte[] content = file.getContent();
        if (useFilesystemForFile(filename, content.length)) {
          try {
            final String fsFileName = makeFilesystemFileName(filename, projectId);
            filesystemService.save(FileDataRoleEnum.SOURCE, fsFileName, content);
            file.setFilesystemName(fsFileName);
          } catch (IOException e) { // GCS throws this
            throw CrashReport.createAndLogError(LOG, null,
                ErrorUtils.collectProjectErrorInfo(userId, projectId, file.getFileName()), e);
          }
        }
      }

      databaseService.createProjectFileData(userId, projectId, FileDataRoleEnum.SOURCE, unifiedFiles);
      databaseService.createUserProjectData(userId, projectId, projectSettings);

      return projectId;
    } catch (DatabaseAccessException e) {
      for (UnifiedFile file : unifiedFiles) {
        final String fsFileName = file.getFilesystemName();
        if (file.getFilesystemName() != null) {
          try {
            filesystemService.delete(FileDataRoleEnum.SOURCE, fsFileName);
          } catch (IOException ex) { // GCS throws this
            LOG.severe("Failed to clean up filesystem file " + file.getFileName() +
                " after database failure: " + ex.getMessage());
          }
        }
      }
      throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectUserErrorInfo(userId), e);
    }
  }

  @Override
  public void deleteProject(final String userId, final long projectId) {
    // First job deletes the UserProjectData in the user's entity group
    databaseService.deleteUserProject(userId, projectId);

    // Second job deletes the project files and ProjectData in the project's
    final List<String> gcsPaths = databaseService.deleteProjectData(userId, projectId);

    // Now delete the gcs files
    for (String gcsName : gcsPaths) {
      try {
        filesystemService.delete(FileDataRoleEnum.SOURCE, gcsName);
      } catch (IOException e) {
        // Note: this warning will happen if we attempt to remove an APK file, because we may be looking
        // in the wrong bucket. But that's OK. Things in the apk bucket will go away on their own.
        LOG.log(Level.WARNING, "Unable to delete " + gcsName + " from GCS while deleting project", e);
      }
    }
  }

  @Override
  public void setMoveToTrashFlag(final String userId, final long projectId, final boolean flag) {
    databaseService.setProjectMovedToTrashFlag(userId, projectId, flag);
  }

  @Override
  public List<Long> getProjects(final String userId) {
    return databaseService.getProjectIdsByUser(userId);
  }

  @Override
  public String loadProjectSettings(final String userId, final long projectId) {
    return databaseService.getProjectSettings(userId, projectId);
  }

  @Override
  public void storeProjectSettings(final String userId, final long projectId,
                                   final String settings) {
    databaseService.setProjectSettings(userId, projectId, settings);
  }

  @Override
  public String getProjectType(final String userId, final long projectId) {
    // We only have one project type, no need to ask about it
    return YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE;
  }

  @Override
  public UserProject getUserProject(final String userId, final long projectId) {
    return databaseService.getUserProject(userId, projectId);
  }

  @Override
  public List<UserProject> getUserProjects(final String userId, final List<Long> projectIds) {
    return databaseService.getUserProjects(userId, projectIds);
  }

  @Override
  public String getProjectName(final String userId, final long projectId) {
    return databaseService.getProjectName(userId, projectId);
  }

  @Override
  public long getProjectDateModified(final String userId, final long projectId) {
    return databaseService.getProjectDateModified(userId, projectId);
  }

  @Override
  public long getProjectDateBuilt(final String userId, final long projectId) {
    return databaseService.getProjectDateBuilt(userId, projectId);
  }

  @Override
  public long updateProjectBuiltDate(final String userId, final long projectId, final long builtDate) {
    databaseService.setProjectBuiltDate(userId, projectId, builtDate);
    return builtDate;
  }

  @Override
  public String getProjectHistory(final String userId, final long projectId) {
    return databaseService.getProjectHistory(userId, projectId);
  }

  @Override
  public long getProjectDateCreated(final String userId, final long projectId) {
    return databaseService.getProjectDateCreated(userId, projectId);
  }

  @Override
  public void addFilesToUser(final String userId, final String... fileNames) {
    for (String fileName : fileNames) {
      databaseService.createUserFileData(userId, fileName);
    }
  }

  @Override
  public void storeBuildStatus(String userId, long projectId, int progress) {
    String cacheKey = CACHE_KEY_PREFIX__BUILD_STATUS + "|" + userId + "|" + projectId;
    cacheService.put(cacheKey, progress);
  }

  @Override
  public int getBuildStatus(String userId, long projectId) {
    String cacheKey = CACHE_KEY_PREFIX__BUILD_STATUS + "|" + userId + "|" + projectId;
    Integer progress = (Integer) cacheService.get(cacheKey);
    // 50% fallback if not in memcache (or memcache service down)
    return Objects.requireNonNullElse(progress, 50);
  }

  @Override
  public void assertUserHasProject(final String userId, final long projectId) {
    final String cacheKey = CACHE_KEY_PREFIX__PROJECT_OWNER + "|" + projectId;
    final String ownerUserId = (String) cacheService.get(cacheKey);

    if (ownerUserId != null) {
      if (ownerUserId.equals(userId)) {
        // The user in the cache owns the project, hence we don't need to throw anything
        return;
      }
      // Whoops, it seems like someone is being sneaky :)
      throw new SecurityException("Unauthorized access");
    }

    final boolean ownsProject = databaseService.assertUserIdOwnerOfProject(userId, projectId);
    if (!ownsProject) {
      // User doesn't have the corresponding project.
      throw new SecurityException("Unauthorized access");
    }

    // User has data for project, so everything checks out.
    // We just store it now in the cache for future access, as we know the user requesting
    //   this project owns it.
    cacheService.put(cacheKey, userId);
  }

  private boolean useFilesystemForFile(String fileName, int length) {
    final boolean isAssetsFile = fileName.contains("assets/");
    final boolean isOutputBinaryFile = fileName.endsWith(".apk") || fileName.endsWith(".aab")
        || fileName.endsWith(".ipa");
    if (isAssetsFile || isOutputBinaryFile) {
      return true;            // Use filesystem for assets and output binaries
    }

    boolean mayUse = (fileName.contains("src/") && fileName.endsWith(".blk")) // AI1 Blocks Files
        || (fileName.contains("src/") && fileName.endsWith(".bky")); // Blockly files
    // Only use GCS for larger blocks files
    return mayUse && length > MAX_FILE_SIZE_BYTES_IN_DB;
  }


  private String makeFilesystemFileName(String fileName, long projectId) {
    return (projectId + "/" + fileName);
  }

}
