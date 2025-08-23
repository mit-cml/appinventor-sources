package com.google.appinventor.server.storage;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.cache.CacheService;
import com.google.appinventor.server.storage.database.DatabaseService;
import com.google.appinventor.server.storage.filesystem.FilesystemService;
import com.google.appinventor.shared.rpc.user.User;

import java.util.Objects;
import java.util.logging.Logger;


public final class ModularizedStorageIo implements StorageIo {
  private static final Flag<Boolean> REQUIRE_TOS = Flag.createFlag("require.tos", false);

  private static final Logger LOG = Logger.getLogger(ModularizedStorageIo.class.getName());

  private final static String CACHE_PREFIX__USER = "f682688a-1065-4cda-8515-a8bd70200ac9";
  private final static String CACHE_PREFIX__BUILD_STATUS = "40bae275-070f-478b-9a5f-d50361809b99";

  private final CacheService cacheService = CacheService.getCacheService();
  private final DatabaseService databaseService = DatabaseService.getDatabaseService();
  private final FilesystemService filesystemService = FilesystemService.getFilesystemService();

  public ModularizedStorageIo() {
  }

  public User getUser(final String userId, final String email) {
    final String cacheKey = CACHE_PREFIX__USER + "|" + userId;

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
    final String cacheKey = CACHE_PREFIX__USER + "|" + userId;

    cacheService.delete(cacheKey);
    databaseService.setUserSessionId(userId, sessionId);
  }

  @Override
  public void storeBuildStatus(String userId, long projectId, int progress) {
    String cacheKey = CACHE_PREFIX__BUILD_STATUS + "|" + userId + "|" + projectId;
    cacheService.put(cacheKey, progress);
  }

  @Override
  public int getBuildStatus(String userId, long projectId) {
    String cacheKey = CACHE_PREFIX__BUILD_STATUS + "|" + userId + "|" + projectId;
    Integer progress = (Integer) cacheService.get(cacheKey);
    // 50% fallback if not in memcache (or memcache service down)
    return Objects.requireNonNullElse(progress, 50);
  }

}
