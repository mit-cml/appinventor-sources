package com.google.appinventor.server.cache;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.util.logging.Level;

public final class ProviderMemcacheAppEngine extends CacheService {
  private final MemcacheService memcache;

  public ProviderMemcacheAppEngine() {
    this.memcache = MemcacheServiceFactory.getMemcacheService();
    memcache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
  }

  @Override
  public Object get(final String cacheKey) {
    return memcache.get(cacheKey);
  }

  @Override
  public void put(final String cacheKey, final Object content) {
    memcache.put(cacheKey, content);
  }

  @Override
  public void put(final String cacheKey, final Object content, final int ttlSeconds) {
    memcache.put(cacheKey, content, Expiration.byDeltaSeconds(ttlSeconds));
  }

  @Override
  public boolean delete(final String cacheKey) {
    return memcache.delete(cacheKey);
  }
}
