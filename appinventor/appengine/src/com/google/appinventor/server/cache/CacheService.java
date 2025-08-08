package com.google.appinventor.server.cache;

import com.google.appinventor.server.flags.Flag;

public abstract class CacheService {
  private static final Flag<String> PROVIDER = Flag.createFlag("cache.provider", "gae");

  public abstract Object get(final String cacheKey);

  public abstract void put(final String cacheKey, final Object content);

  public abstract void put(final String cacheKey, final Object content, final int ttlSeconds);

  public abstract boolean delete(final String cacheKey);

  public static CacheService getCacheService() {
    final String provider = PROVIDER.get();

    if ("redis".equals(provider)) {
      return new RedisProvider();
    } else if ("gae".equals(provider)) {
      return new MemcacheAppEngineProvider();
    }

    throw new UnsupportedOperationException("Unknown cache provider: " + provider);
  }
}
