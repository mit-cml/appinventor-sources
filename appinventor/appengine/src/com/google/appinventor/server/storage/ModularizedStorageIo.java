package com.google.appinventor.server.storage;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.cache.CacheService;
import com.google.appinventor.server.storage.database.DatabaseService;
import com.google.appinventor.server.storage.filesystem.FilesystemService;
import com.google.appinventor.shared.rpc.user.User;

public final class ModularizedStorageIo implements StorageIo {
  static final Flag<Boolean> REQUIRE_TOS = Flag.createFlag("require.tos", false);

  private final static String CACHE_PREFIX__USER = "f682688a-1065-4cda-8515-a8bd70200ac9";
  private final static String CACHE_PREFIX__BUILD_STATUS = "40bae275-070f-478b-9a5f-d50361809b99";

  private final CacheService cacheService = CacheService.getCacheService();
  private final DatabaseService databaseService = DatabaseService.getDatabaseService();
  private final FilesystemService filesystemService = FilesystemService.getFilesystemService();

  public ModularizedStorageIo() {}

  public User getUser(final String userId, final String email) {
    String cachekey = CACHE_PREFIX__USER + "|" + userId;

    final User cachedUser = (User) cacheService.get(cachekey);
    if (cachedUser != null && cachedUser.getUserTosAccepted() && ((email == null) || (cachedUser.getUserEmail().equals(email)))) {
      return cachedUser;
    }

    // If not in memcache, or tos not yet accepted, fetch from datastore
    final User user = new User(userId, email, false, false, null);
    final StoredData.UserData userData = databaseService.findOrCreateUser(userId, email);
    user.setUserId(userData.id);
    user.setUserEmail(userData.email);
    user.setUserTosAccepted(userData.tosAccepted || !REQUIRE_TOS.get());
    user.setIsAdmin(userData.isAdmin);
    user.setSessionId(userData.sessionid);
    user.setPassword(userData.password);

    cacheService.put(cachekey, user, 60); // Remember for one minute
    // The choice of one minute here is arbitrary. getUser() is called on every authenticated
    // RPC call to the system (out of OdeAuthFilter), so using memcache will save a significant
    // number of calls to the datastore. If someone is idle for more then a minute, it isn't
    // unreasonable to hit the datastore again. By pruning memcache ourselves, we have a
    // bit more control (maybe) of how things are flushed from memcache. Otherwise we are
    // at the whim of whatever algorithm App Engine employs now or in the future.

    return user;
  }

}
