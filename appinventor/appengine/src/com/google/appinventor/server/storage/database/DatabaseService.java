package com.google.appinventor.server.storage.database;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StoredData;


public abstract class DatabaseService {
  private static final Flag<String> PROVIDER = Flag.createFlag("database.provider", "gae");

  public abstract StoredData.UserData findUserDataByEmail(final String email);

  public abstract StoredData.UserData findUserById(final String userId);

  public static DatabaseService getDatabaseService() {
    final String provider = PROVIDER.get();

    if ("gae".equals(provider) || (provider == null || provider.isEmpty())) {
      return new ProviderDatastoreAppEngine();
    }

    throw new UnsupportedOperationException("Unknown database provider: " + provider);
  }
}
