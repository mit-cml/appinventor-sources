package com.google.appinventor.server.database;

import com.google.appinventor.server.storage.StoredData;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

public final class ProviderDatastoreAppEngine extends DatabaseService {
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
  public StoredData.UserData findUserDataByEmail(final String email) {
    Objectify datastore = ObjectifyService.begin(); // Need an instance not in this transaction
    StoredData.UserData userData = datastore.query(StoredData.UserData.class).filter("email", email).get();
    if (userData == null) { // Still null!
      userData = datastore.query(StoredData.UserData.class).filter("emaillower", email.toLowerCase()).get();
    }
    return userData;
  }

  private Key<StoredData.UserData> userKey(String userId) {
    return new Key<>(StoredData.UserData.class, userId);
  }
}
