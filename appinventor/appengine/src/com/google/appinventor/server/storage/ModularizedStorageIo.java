package com.google.appinventor.server.storage;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.GalleryExtensionException;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.project.youngandroid.YoungAndroidSettingsBuilder;
import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.server.storage.cache.CacheService;
import com.google.appinventor.server.storage.database.DatabaseAccessException;
import com.google.appinventor.server.storage.database.DatabaseService;
import com.google.appinventor.server.storage.filesystem.FilesystemService;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.AdminInterfaceException;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.appinventor.components.common.YaVersion.YOUNG_ANDROID_VERSION;
import static com.google.appinventor.shared.storage.StorageUtil.APPSTORE_CREDENTIALS_FILENAME;


public final class ModularizedStorageIo implements StorageIo {
  private static final Flag<Boolean> REQUIRE_TOS = Flag.createFlag("require.tos", false);

  private static final Logger LOG = Logger.getLogger(ModularizedStorageIo.class.getName());

  // used for getting the allowed tutorial urls
  private static final JSONParser JSON_PARSER = new ServerJsonParser();

  private static final int MAX_FILE_SIZE_BYTES_IN_DB = 50_000;  // 50 Kb
  private static final long BACKUP_THRESHOLD_SECONDS = 24 * 3600;  // 24 hours in seconds
  private static final int MAX_FILE_SIZE_BYTES_IN_CACHE = 1_000_000;  // 1 Mb

  private final static String CACHE_KEY_PREFIX__USER = "USER";
  private final static String CACHE_KEY_PREFIX__BUILD_STATUS = "BUILD_STATUS";
  private static final String CACHE_KEY_PREFIX__PROJECT_OWNER = "PROJECT_OWNER";
  private static final String CACHE_KEY_PREFIX__PROJECT_FILE = "PROJECT_FILE";

  private static final String TEMP_FILE_PREFIX = "__TEMP__";

  private static final long ALLOWED_TUTORIAL_URL_ID = 1;
  private static final long SPLASH_DATA_ID = 1;
  private static final long ALLOWED_IOS_EXTENSIONS_ID = 1;

  private final CacheService cacheService = CacheService.getCacheService();
  private final DatabaseService databaseService = DatabaseService.getDatabaseService();
  private final FilesystemService filesystemService = FilesystemService.getFilesystemService();

  public ModularizedStorageIo() {
  }

  @Override
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
    LOG.info("getUserFromEmail: email = " + email + " email = " + email);
    return databaseService.getUserFromEmail(email, true);
  }

  // Find a user by email address. This version does *not* create a new user
  // if the user does not exist
  @Override
  public String findUserByEmail(String email) throws NoSuchElementException {
    User user = databaseService.getUserFromEmail(email, false);
    if (user == null) {
      throw new NoSuchElementException("Couldn't find a user with email " + email);
    }

    return user.getUserId();
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
  public List<String> getUserFiles(final String userId) {
    return databaseService.getUserFileNames(userId);
  }

  @Override
  public void uploadRawUserFile(final String userId, final String fileName, final byte[] content) {
    databaseService.uploadUserFile(userId, fileName, content);
  }

  @Override
  public byte[] downloadRawUserFile(final String userId, final String fileName) {
    return databaseService.getUserFile(userId, fileName);
  }

  @Override
  public void deleteUserFile(final String userId, final String fileName) {
    databaseService.deleteUserFile(userId, fileName);
  }

  @Override
  public int getMaxJobSizeBytes() {
    // TODO(user): what should this mean?
    return 5 * 1024 * 1024;
  }

  @Override
  public void addSourceFilesToProject(final String userId, final long projectId,
                                      final boolean changeModDate, final String... fileNames) {
    for (String fileName : fileNames) {
      databaseService.addFileToProject(userId, projectId, FileDataRoleEnum.SOURCE, changeModDate, fileName);
    }
  }

  @Override
  public void addOutputFilesToProject(final String userId, final long projectId,
                                      final String... fileNames) {
    for (String fileName : fileNames) {
      databaseService.addFileToProject(userId, projectId, FileDataRoleEnum.TARGET, false, fileName);
    }
  }

  @Override
  public void removeSourceFilesFromProject(final String userId, final long projectId,
                                           final boolean changeModDate, final String... fileNames) {
    boolean isFirst = true;
    for (String fileName : fileNames) {
      // Only try to update the modification date for the first file
      databaseService.removeFileFromProject(userId, projectId, FileDataRoleEnum.SOURCE, changeModDate && isFirst, fileName);
      final String cacheKey = CACHE_KEY_PREFIX__PROJECT_FILE + "|" + projectId + "|" + fileName;
      cacheService.delete(cacheKey); // Remove it from memcache (if it is there)

      if (isFirst) {
        isFirst = false;
      }
    }
  }

  @Override
  public void removeOutputFilesFromProject(final String userId, final long projectId,
                                           final String... fileNames) {
    for (String fileName : fileNames) {
      databaseService.removeFileFromProject(userId, projectId, FileDataRoleEnum.SOURCE, false, fileName);
      final String cacheKey = CACHE_KEY_PREFIX__PROJECT_FILE + "|" + projectId + "|" + fileName;
      cacheService.delete(cacheKey); // Remove it from memcache (if it is there)
    }
  }

  @Override
  public List<String> getProjectSourceFiles(final String userId, final long projectId) {
    return databaseService.getProjectFiles(userId, projectId, FileDataRoleEnum.SOURCE);
  }

  @Override
  public List<String> getProjectOutputFiles(final String userId, final long projectId) {
    return databaseService.getProjectFiles(userId, projectId, FileDataRoleEnum.TARGET);
  }

  @Override
  public long uploadRawFile(final long projectId, final String fileName, final String userId,
                            final boolean force, final byte[] content) throws BlocksTruncatedException {
    final boolean useFilesystem = useFilesystemForFile(fileName, content.length);
    final String filesystemName = makeFilesystemFileName(fileName, projectId);

    final boolean considerBackup = (fileName.contains("src/") &&
        (fileName.endsWith(".bky") || fileName.endsWith(".scm")));

    DatabaseService.UploadProjectFileResult result = databaseService.uploadProjectFile(userId, projectId, fileName,
        force, content,
        considerBackup ? BACKUP_THRESHOLD_SECONDS : null,
        useFilesystem ? filesystemName : null);

    if (useFilesystem) {
      try {
        filesystemService.save(result.fileRole, filesystemName, content);
      } catch (IOException e) {
        throw CrashReport.createAndLogError(LOG, null, ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
      }
    }

    if (result.needsFilesystemDelete) {
      try {
        filesystemService.delete(result.fileRole, makeFilesystemFileName(fileName, projectId));
      } catch (IOException e) {
        LOG.warning("Failed to delete old filesystem file: " + ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName));
      }
    }

    if (result.shouldDoFilesystemBackup) {
      final String filesystemBackupName = makeFilesystemFileName(fileName + "." + formattedTime() + ".backup", projectId);
      try {
        filesystemService.save(result.fileRole, filesystemBackupName, content);
      } catch (IOException e) {
        LOG.warning("Failed to backup filesystem file: " + ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName));
      }
    }

    // Make sure to update the cache, if appropriate.
    // The cache for files takes serialized GetProjectFileResult objects. As such, we need to construct
    //   a similar object based on the content bytes and UploadProjectFileResult.
    DatabaseService.GetProjectFileResult cacheResult = new DatabaseService.GetProjectFileResult();
    cacheResult.content = content;
    cacheResult.fileRole = result.fileRole;
    if (useCacheForFile(cacheResult.fileRole, fileName, cacheResult.content.length)) {
      // Only cache source files that are not assets
      final String cacheKey = CACHE_KEY_PREFIX__PROJECT_FILE + "|" + projectId + "|" + fileName;
      cacheService.put(cacheKey, cacheResult, 60 * 60);  // Cache for 1 hour
    }

    return result.lastModifiedDate;
  }

  private static String formattedTime() {
    // Return time in ISO_8660 format
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    return formatter.format(new Date());
  }

  @Override
  public long deleteFile(final String userId, final long projectId, final String fileName) {
    final DatabaseService.DeleteProjectFileResult result = databaseService.deleteProjectFile(userId, projectId, fileName);

    final String cacheKey = CACHE_KEY_PREFIX__PROJECT_FILE + "|" + projectId + "|" + fileName;
    cacheService.delete(cacheKey); // Remove it from memcache (if it is there)

    if (result.filesystemToDelete != null) {
      try {
        filesystemService.delete(result.fileRole, result.filesystemToDelete);
      } catch (IOException e) {
        // This may get logged if we attempt to delete an APK file. But we can ignore
        // this case because APK files will be deleted on their own
        LOG.log(Level.WARNING, "Unable to delete " + result.filesystemToDelete + " from GCS.", e);
      }
    }

    return (result.lastModifiedDate == null) ? 0 : result.lastModifiedDate;
  }

  @Override
  public byte[] downloadRawFile(final String userId, final long projectId, final String fileName) {
    final String cacheKey = CACHE_KEY_PREFIX__PROJECT_FILE + "|" + projectId + "|" + fileName;

    final DatabaseService.GetProjectFileResult cachedBytes = (DatabaseService.GetProjectFileResult) cacheService.get(cacheKey);
    if (cachedBytes != null) {
      return cachedBytes.content;
    }

    final DatabaseService.GetProjectFileResult result = databaseService.getProjectFile(userId, projectId, fileName);

    if (result.filesystemToRetrieve != null) {
      try {
        result.content = filesystemService.read(result.fileRole, result.filesystemToRetrieve);
      } catch (IOException e) {
        throw CrashReport.createAndLogError(LOG, null,
            ErrorUtils.collectProjectErrorInfo(userId, projectId, fileName), e);
      }
    }

    if (useCacheForFile(result.fileRole, fileName, result.content.length)) {
      // Only cache source files that are not assets
      cacheService.put(cacheKey, result, 60 * 60);  // Cache for 1 hour
    }

    return result.content;
  }

  @Override
  public void recordCorruption(String userId, long projectId, String fileId, String message) {
    databaseService.storeCorruptionRecord(userId, projectId, fileId, message);
  }

  /**
   * Exports project files as a zip archive
   *
   * @param userId                 a user Id (the request is made on behalf of this user)
   * @param projectId              project ID
   * @param includeProjectHistory  whether or not to include the project history
   * @param includeAndroidKeystore whether or not to include the Android keystore
   * @param zipName                the name of the zip file, if a specific one is desired
   * @param includeYail            include any yail files in the project
   * @param includeScreenShots     include any screen shots stored with the project
   * @param fatalError             Signal a fatal error if a file is not found
   * @param forGallery             flag to indicate we are exporting for the gallery
   * @return project with the content as requested by params.
   */
  @Override
  public ProjectSourceZip exportProjectSourceZip(final String userId, final long projectId,
                                                 final boolean includeProjectHistory,
                                                 final boolean includeAndroidKeystore,
                                                 @Nullable String zipName,
                                                 final boolean includeYail,
                                                 final boolean includeScreenShots,
                                                 final boolean forGallery,
                                                 final boolean fatalError,
                                                 final boolean forAppStore,
                                                 final boolean locallyCachedApp) throws IOException {
    final boolean forBuildserver = includeAndroidKeystore && includeYail;
    int fileCount = 0;
    final String projectName = getProjectName(userId, projectId);

    final Map<String, Integer> screens = new HashMap<>();

    ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
    final ZipOutputStream out = new ZipOutputStream(zipFile);
    out.setComment("Built with MIT App Inventor");

    final List<String> fileNames = new ArrayList<>();
    for (String fileName : databaseService.getProjectFiles(userId, projectId, FileDataRoleEnum.SOURCE)) {
      fileNames.add(fileName);
      if (fileName.startsWith("src/") && (fileName.endsWith(".scm") || fileName.endsWith(".bky") || fileName.endsWith(".yail"))) {
        String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf("."));
        int count = screens.containsKey(fileNameNoExt) ? screens.get(fileNameNoExt) + 1 : 1;
        screens.put(fileNameNoExt, count);
      }
    }

    Iterator<String> it = fileNames.iterator();
    while (it.hasNext()) {
      String fileName = it.next();
      if (fileName.startsWith("assets/external_comps") && forGallery) {
        throw new GalleryExtensionException();
      }

      if (fileName.equals(FileExporter.REMIX_INFORMATION_FILE_PATH) ||
          (fileName.startsWith("screenshots") && !includeScreenShots) ||
          (fileName.startsWith("src/") && fileName.endsWith(".yail") && !includeYail) ||
          (fileName.startsWith("src/") && fileName.endsWith(".bky") && locallyCachedApp) ||
          (fileName.startsWith("src/") && fileName.endsWith(".scm") && locallyCachedApp)) {
        // Skip legacy remix history files that were previous stored with the project
        // only include screenshots if asked ...
        // Don't include YAIL files when exporting projects
        // includeYail will be set to true when we are exporting the source
        // to send to the buildserver or when the person exporting
        // a project is an Admin (for debugging).
        // Otherwise Yail files are confusing cruft. In the case of
        // the Firebase Component they may contain secrets which we would
        // rather not have leak into an export .aia file or into the Gallery
        // We don't include the .scm and .bky files when exporting the source
        // to be cached locally by a device to avoid leaking potentially sensitive
        // information such as keys.
        it.remove();
      } else if (forBuildserver && fileName.startsWith("src/") &&
          (fileName.endsWith(".scm") || fileName.endsWith(".bky") || fileName.endsWith(".yail"))) {
        String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf("."));
        if (screens.get(fileNameNoExt) < 3) {
          LOG.log(Level.INFO, "Not adding file to build ", fileName);
          it.remove();
          if (fileName.endsWith(".yail")) {
            deleteFile(userId, projectId, fileName);
          }
        }
      }
    }

    for (String fileName : fileNames) {
      byte[] data = downloadRawFile(userId, projectId, fileName);
      if (fileName.endsWith(".properties") && locallyCachedApp) {
        String projectProperties = new String(data, StandardCharsets.UTF_8);
        Properties oldProperties = new Properties();
        try {
          oldProperties.load(new StringReader(projectProperties));
        } catch (IOException e) {
          e.printStackTrace();
        }
        YoungAndroidSettingsBuilder oldPropertiesBuilder = new YoungAndroidSettingsBuilder(oldProperties);
        String updatedProperties = oldPropertiesBuilder.setAIVersioning(Integer.toString(YOUNG_ANDROID_VERSION)).toProperties();
        data = updatedProperties.getBytes(StandardCharsets.UTF_8);
      }

      if (data == null) {     // This happens if file creation is interrupted
        data = new byte[0];
      }

      out.putNextEntry(new ZipEntry(fileName));
      out.write(data, 0, data.length);
      out.closeEntry();
      fileCount++;
    }

    if (fileCount == 0) {
      // can't close out since will get a ZipException due to the lack of files
      throw new IllegalArgumentException("No files to download");
    }

    final String projectHistory = includeProjectHistory ? getProjectHistory(userId, projectId) : null;
    if (projectHistory != null) {
      byte[] data = projectHistory.getBytes(StorageUtil.DEFAULT_CHARSET);
      out.putNextEntry(new ZipEntry(FileExporter.REMIX_INFORMATION_FILE_PATH));
      out.write(data, 0, data.length);
      out.closeEntry();
      fileCount++;
    }

    if (includeAndroidKeystore) {
      for (String ufd : databaseService.getUserFileNames(userId)) {
        if (ufd.equals(StorageUtil.ANDROID_KEYSTORE_FILENAME)) {
          final byte[] content = databaseService.getUserFile(userId, ufd);
          if (content.length > 0) {
            out.putNextEntry(new ZipEntry(StorageUtil.ANDROID_KEYSTORE_FILENAME));
            out.write(content, 0, content.length);
            out.closeEntry();
            fileCount++;
          }
        } else if (forAppStore && ufd.equals(APPSTORE_CREDENTIALS_FILENAME)) {
          final byte[] content = databaseService.getUserFile(userId, ufd);
          if (content.length > 0) {
            out.putNextEntry(new ZipEntry(APPSTORE_CREDENTIALS_FILENAME));
            out.write(content, 0, content.length);
            out.closeEntry();
            fileCount++;
          }
        }
      }
    }

    out.close();

    if (zipName == null) {
      zipName = projectName + ".aia";
    }
    ProjectSourceZip projectSourceZip = new ProjectSourceZip(zipName, zipFile.toByteArray(), fileCount);
    projectSourceZip.setMetadata(projectName);
    return projectSourceZip;
  }

  @Override
  public boolean checkWhiteList(String email) {
    return databaseService.isEmailAddressInAllowlist(email.toLowerCase());
  }

  @Override
  public void storeFeedback(final String notes, final String foundIn, final String faultData,
                            final String comments, final String datestamp, final String email, final String projectId) {
    databaseService.storeFeedbackData(notes, foundIn, faultData, comments, datestamp, email, projectId);
  }

  @Override
  public void storeNonce(final String nonceValue, final String userId, final long projectId) {
    databaseService.storeNonce(nonceValue, userId, projectId);
  }

  @Override
  public Nonce getNoncebyValue(String nonceValue) {
    return databaseService.getNonceByValue(nonceValue);
  }

  @Override
  public void cleanupNonces() {
    try {
      databaseService.cleanupNonces();
    } catch (UnsupportedOperationException e) {
      // We catch and log silently in case manual cleanup of Nonces is not supported
      //  (for example, if they get cleaned up automatically by the database using a TTL attribute)
      LOG.warning("Cannot manually clean up Nonces");
    }
  }

  @Override
  public String createPWData(final String email) {
    return databaseService.createPWData(email);
  }

  @Override
  public String findPWData(final String uid) {
    return databaseService.getPWData(uid);
  }

  @Override
  public void cleanuppwdata() {
    try {
      databaseService.cleanupPWDatas();
    } catch (UnsupportedOperationException e) {
      // We catch and log silently in case manual cleanup of PWDatas is not supported
      //  (for example, if they get cleaned up automatically by the database using a TTL attribute)
      LOG.warning("Cannot manually clean up PWDatas");
    }
  }

  @Override
  public String uploadTempFile(byte[] content) throws IOException {
    String uuid = UUID.randomUUID().toString();
    String fileName = TEMP_FILE_PREFIX + "/" + uuid;
    filesystemService.save(FileDataRoleEnum.TEMPORARY, fileName, content);
    return fileName;
  }

  @Override
  public InputStream openTempFile(String fileName) throws IOException {
    if (!fileName.startsWith(TEMP_FILE_PREFIX)) {
      throw new RuntimeException("deleteTempFile (" + fileName + ") Invalid File Name");
    }

    final byte[] result = filesystemService.read(FileDataRoleEnum.TEMPORARY, fileName);
    return new ByteArrayInputStream(result);
  }

  @Override
  public void deleteTempFile(String fileName) throws IOException {
    if (!fileName.startsWith(TEMP_FILE_PREFIX)) {
      throw new RuntimeException("deleteTempFile (" + fileName + ") Invalid File Name");
    }
    filesystemService.delete(FileDataRoleEnum.TEMPORARY, fileName);
  }

  @Override
  public SplashConfig getSplashConfig() {
    return databaseService.getSplashConfig(SPLASH_DATA_ID);
  }

  @Override
  public List<AdminUser> searchUsers(final String partialEmail) {
    return databaseService.searchUsers(partialEmail);
  }

  @Override
  public void storeUser(final AdminUser user) throws AdminInterfaceException {
    databaseService.storeUser(user);
  }

  @Override
  public String downloadBackpack(final String backPackId) {
    return databaseService.getBackpack(backPackId);
  }

  @Override
  public void uploadBackpack(String backPackId, String content) {
    databaseService.storeBackpack(backPackId, content);
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

  @Override
  public List<String> getTutorialsUrlAllowed() {
    final String rawResult = databaseService.getAllowedTutorialUrls(ALLOWED_TUTORIAL_URL_ID);

    JSONArray parsedUrls = (JSONArray) JSON_PARSER.parse(rawResult);
    List<JSONValue> jsonList = parsedUrls.getElements();
    List<String> returnValue = new ArrayList<>();
    for (JSONValue v : jsonList) {
      returnValue.add(v.asString().getString());
    }

    return returnValue;
  }

  @Override
  public boolean deleteAccount(final String userId) {
    final List<Long> projectIds = getProjects(userId);
    // We iterate over the projects in two loops The first loop is
    // just to determine that all remaining projects are in the trash.
    // The second loop actually removes such projects.  We do it this
    // way so that no projects are removed if any projects
    // exist. Otherwise some trashed projects may get removed before
    // we discover a live project.
    for (long projectId : projectIds) {
      if (!databaseService.isProjectInTrash(projectId)) {
        return false;           // Have a live project
      }
    }

    // Got here, no live projects, remove the remainders
    for (long projectId : projectIds) {
      deleteProject(userId, projectId);
    }

    // Now flush the user data object both from the datastore and the
    // cache.
    databaseService.deleteUser(userId);

    final String cacheKey = CACHE_KEY_PREFIX__USER + "|" + userId;
    // And remove it from memcache
    cacheService.delete(cacheKey);

    return true;
  }

  @Override
  public String getIosExtensionsConfig() {
    return databaseService.getAllowedIosExtensions(ALLOWED_IOS_EXTENSIONS_ID);
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

  private boolean useCacheForFile(FileDataRoleEnum role, String fileName, int length) {
    if (role != FileDataRoleEnum.SOURCE) {
      return false;  // Only cache source files
    }

    if (fileName.contains("assets/")) {
      return false;  // Never cache assets files
    }

    return length > MAX_FILE_SIZE_BYTES_IN_CACHE;
  }

  private String makeFilesystemFileName(String fileName, long projectId) {
    return (projectId + "/" + fileName);
  }

}
