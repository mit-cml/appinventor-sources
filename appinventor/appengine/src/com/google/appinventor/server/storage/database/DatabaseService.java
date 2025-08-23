package com.google.appinventor.server.storage.database;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.FileDataRoleEnum;
import com.google.appinventor.server.storage.UnifiedFile;
import com.google.appinventor.server.storage.database.datastore.ProviderDatastoreAppEngine;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.User;

import java.util.List;


public abstract class DatabaseService {
  private static final Flag<String> PROVIDER = Flag.createFlag("database.provider", "gae");

  public abstract User findOrCreateUser(final String userId, final String email, final boolean requireTos);

  public abstract void setUserSessionId(final String userId, final String sessionId);

  public abstract User getUserFromEmail(String email);

  public abstract void setTosAccepted(final String userId);

  public abstract void setUserEmail(final String userId, final String emailLower);

  public abstract void setUserPassword(final String userId, final String password);

  public abstract String loadUserDataSettings(final String userId);

  public abstract void storeUserDataSettings(final String userId, final String settings);

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

  public abstract boolean assertUserIdOwnerOfProject(final String userId, final long projectId);

  public static DatabaseService getDatabaseService() {
    final String provider = PROVIDER.get();

    if ("gae".equals(provider) || (provider == null || provider.isEmpty())) {
      return new ProviderDatastoreAppEngine();
    }

    throw new UnsupportedOperationException("Unknown database provider: " + provider);
  }
}
