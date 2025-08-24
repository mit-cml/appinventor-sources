package com.google.appinventor.server.storage.database;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.FileDataRoleEnum;
import com.google.appinventor.server.storage.UnifiedFile;
import com.google.appinventor.server.storage.database.datastore.ProviderDatastoreAppEngine;
import com.google.appinventor.server.storage.database.dynamodb.ProviderDynamoDB;
import com.google.appinventor.shared.rpc.AdminInterfaceException;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.shared.rpc.user.User;

import java.io.Serializable;
import java.util.List;


public abstract class DatabaseService {
  private static final Flag<String> PROVIDER = Flag.createFlag("database.provider", "gae");

  protected static final Long NONCE_EXPIRATION_TIME_MS = 3 * 3600 * 1000L;  // 3 hours
  protected static final Long PWDATA_EXPIRATION_TIME_MS = 24 * 3600 * 1000L;  // 24 hours

  public abstract User findOrCreateUser(final String userId, final String email, final boolean requireTos);

  public abstract void setUserSessionId(final String userId, final String sessionId);

  public abstract User getUserFromEmail(String email, boolean create);

  public abstract void setTosAccepted(final String userId);

  public abstract void setUserEmail(final String userId, final String emailLower);

  public abstract void setUserPassword(final String userId, final String password);

  public abstract String loadUserDataSettings(final String userId);

  public abstract void storeUserDataSettings(final String userId, final String settings);

  public abstract List<AdminUser> searchUsers(final String partialEmail);

  public abstract void storeUser(final AdminUser user) throws AdminInterfaceException;

  public abstract Long createProjectData(final Project project, final String projectSettings) throws DatabaseAccessException;

  public abstract void createUserProjectData(final String userId, final Long projectId, final String projectSettings) throws DatabaseAccessException;

  public abstract void createProjectFileData(final String userId, final Long projectId, final FileDataRoleEnum role, final List<UnifiedFile> files) throws DatabaseAccessException;

  public abstract void deleteUserProject(final String userId, final Long projectId);

  public abstract List<String> deleteProjectData(final String userId, final Long projectId);

  public abstract void setProjectMovedToTrashFlag(final String userId, final long projectId, final boolean flag);

  public abstract List<Long> getProjectIdsByUser(final String userId);

  public abstract String getProjectSettings(final String userId, final long projectId);

  public abstract void setProjectSettings(final String userId, final long projectId, final String projectSettings);

  public abstract UserProject getUserProject(final String userId, final long projectId);

  public abstract List<UserProject> getUserProjects(final String userId, final List<Long> projectIds);

  public abstract String getProjectName(final String userId, final long projectId);

  public abstract Long getProjectDateModified(final String userId, final long projectId);

  public abstract Long getProjectDateBuilt(final String userId, final long projectId);

  public abstract void setProjectBuiltDate(final String userId, final long projectId, final long builtDate);

  public abstract String getProjectHistory(final String userId, final long projectId);

  public abstract Long getProjectDateCreated(final String userId, final long projectId);

  public abstract void createUserFileData(final String userId, final String fileName);

  public abstract List<String> getUserFileNames(final String userId);

  public abstract void uploadUserFile(final String userId, final String fileName, final byte[] content);

  public abstract byte[] getUserFile(final String userId, final String fileName);

  public abstract void deleteUserFile(final String userId, final String fileName);

  public abstract void addFileToProject(final String userId, final Long projectId, final FileDataRoleEnum role,
                                        final boolean changeModDate, final String fileName);

  public abstract void removeFileFromProject(final String userId, final Long projectId, final FileDataRoleEnum role,
                                                  final boolean changeModDate, final String fileName);

  public abstract List<String> getProjectFiles(final String userId, final long projectId, FileDataRoleEnum role);

  public static final class UploadProjectFileResult {
    public Long lastModifiedDate;
    public FileDataRoleEnum fileRole = null;
    public boolean needsFilesystemDelete = false;
    public boolean shouldDoFilesystemBackup = false;
  }

  public abstract UploadProjectFileResult uploadProjectFile(final String userId, final long projectId, final String fileName,
                                                            final boolean force, final byte[] content, final Long backupThreshold, final String filesystemName) throws BlocksTruncatedException;

  public static final class DeleteProjectFileResult {
    public Long lastModifiedDate;
    public FileDataRoleEnum fileRole = null;
    public String filesystemToDelete = null;
  }

  public abstract DeleteProjectFileResult deleteProjectFile(final String userId, final long projectId, final String fileName);

  // We add Serializable here so it can be cached
  public static final class GetProjectFileResult implements Serializable {
    public byte[] content = new byte[0];
    public FileDataRoleEnum fileRole = null;
    public String filesystemToRetrieve = null;
  }

  public abstract GetProjectFileResult getProjectFile(final String userId, final long projectId, final String fileName);

  public abstract void storeCorruptionRecord(final String userId, final long projectId, final String fileId, final String message);

  public abstract boolean isEmailAddressInAllowlist(final String emailAddress);

  public abstract void storeFeedbackData(final String notes, final String foundIn, final String faultData,
                                         final String comments, final String datestamp, final String email, final String projectId);

  public abstract void storeNonce(final String nonceValue, final String userId, final long projectId);

  public abstract Nonce getNonceByValue(String nonceValue);

  public abstract void cleanupNonces();

  public abstract String createPWData(final String email);

  public abstract String getPWData(final String uid);

  public abstract void cleanupPWDatas();

  public abstract String getBackpack(final String backPackId);

  public abstract void storeBackpack(String backPackId, String content);

  public abstract boolean assertUserIdOwnerOfProject(final String userId, final long projectId);

  public abstract String getAllowedIosExtensions(final Long allowedIosExtensionsId);

  public abstract SplashConfig getSplashConfig(final Long splashConfigId);

  public abstract boolean isProjectInTrash(final Long projectId);

  public abstract void deleteUser(final String userId);

  public abstract String getAllowedTutorialUrls(final Long tutorialsAllowedUrlsId);

  public static DatabaseService getDatabaseService() {
    final String provider = PROVIDER.get();

    if ("ddb".equals(provider) || "dynamodb".equals(provider)) {
      return new ProviderDynamoDB();
    } else if ("gae".equals(provider) || (provider == null || provider.isEmpty())) {
      return new ProviderDatastoreAppEngine();
    }

    throw new UnsupportedOperationException("Unknown database provider: " + provider);
  }
}
