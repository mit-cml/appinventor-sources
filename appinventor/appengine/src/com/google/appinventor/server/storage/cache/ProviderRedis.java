package com.google.appinventor.server.storage.cache;

import com.google.appinventor.server.flags.Flag;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public final class ProviderRedis extends CacheService {
  private static final Flag<String> REDIS_HOST = Flag.createFlag("cache.redis.host", "localhost:6379");

  private static final RedisCodec<String, Object> REDIS_CODEC = new RedisCodec<>() {
    private final ByteArrayCodec byteArrayCodec = new ByteArrayCodec();

    @Override
    public String decodeKey(ByteBuffer byteBuffer) {
      return Charset.defaultCharset().decode(byteBuffer).toString();
    }

    @Override
    public Object decodeValue(ByteBuffer bytes) {
      try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(byteArrayCodec.decodeValue(bytes)))) {
        return is.readObject();
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }

    @Override
    public ByteBuffer encodeKey(String s) {
      return Charset.defaultCharset().encode(s);
    }

    @Override
    public ByteBuffer encodeValue(Object o) {
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream os = new ObjectOutputStream(bos)) {
        os.writeObject(o);
        return byteArrayCodec.encodeValue(bos.toByteArray());
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }
  };

  private final RedisCommands<String, Object> sync;
  private final RedisAsyncCommands<String, Object> async;

  public ProviderRedis() {
    // TODO: This works fine for single instance Redis, but doesn't for cluster based ones
    final RedisClient client = RedisClient.create("redis://" + REDIS_HOST.get());
    final StatefulRedisConnection<String, Object> connection = client.connect(REDIS_CODEC);

    this.sync = connection.sync();
    this.async = connection.async();
  }

  @Override
  public Object get(String cacheKey) {
    return sync.get(cacheKey);
  }

  @Override
  public void put(String cacheKey, Object content) {
    sync.set(cacheKey, content);
  }

  @Override
  public void put(String cacheKey, Object content, int ttlSeconds) {
    sync.setex(cacheKey, ttlSeconds, content);
  }

  @Override
  public boolean delete(String cacheKey) {
    // No need to wait here...
    async.del(cacheKey);
    return true;
  }
}
