package com.google.appinventor.server.storage.database;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.database.datastore.ProviderDatastoreAppEngine;
import com.google.appinventor.shared.rpc.user.User;


public abstract class DatabaseService {
  private static final Flag<String> PROVIDER = Flag.createFlag("database.provider", "gae");

  public abstract User findOrCreateUser(final String userId, final String email, final boolean requireTos);

  public abstract void setUserSessionId(final String userId, final String sessionId);

  public abstract User getUserFromEmail(String email);

  public abstract void setTosAccepted(final String userId);

  public abstract void setUserEmail(final String userId, String inputemail);

  public static DatabaseService getDatabaseService() {
    final String provider = PROVIDER.get();

    if ("gae".equals(provider) || (provider == null || provider.isEmpty())) {
      return new ProviderDatastoreAppEngine();
    }

    throw new UnsupportedOperationException("Unknown database provider: " + provider);
  }

  protected static String collectUserErrorInfo(final String userId) {
    return collectUserErrorInfo(userId, CrashReport.NOT_AVAILABLE);
  }

  protected static String collectUserErrorInfo(final String userId, String fileName) {
    return "user=" + userId + ", file=" + fileName;
  }

  protected static String collectProjectErrorInfo(final String userId, final long projectId, final String fileName) {
    return "user=" + userId + ", project=" + projectId + ", file=" + fileName;
  }

  protected static String collectUserProjectErrorInfo(final String userId, final long projectId) {
    return "user=" + userId + ", project=" + projectId;
  }
}
