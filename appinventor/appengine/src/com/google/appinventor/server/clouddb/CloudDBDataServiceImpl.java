// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.clouddb;

import com.google.appinventor.server.OdeRemoteServiceServlet;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.shared.rpc.clouddb.CloudDBDataService;
import com.google.appinventor.shared.rpc.clouddb.DataEntry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Server-side implementation of {@link CloudDBDataService}.
 *
 * <p>Uses the token passed from the client (the component's Token property, which was
 * issued by {@code TokenAuthServiceImpl}) to authenticate with Redis, then reads or
 * writes keys under the given projectId prefix. All Jedis connections are closed in
 * finally blocks. Jedis exception types are wrapped as plain {@link Exception} because
 * Jedis types are not in GWT's RPC serialization whitelist.
 */
public class CloudDBDataServiceImpl extends OdeRemoteServiceServlet
    implements CloudDBDataService {

  private static final Logger LOG = Logger.getLogger(CloudDBDataServiceImpl.class.getName());
  private static final int CONNECTION_TIMEOUT_MS = 10000;

  private final String defaultServer = Flag.createFlag("clouddb.server", "").get();

  @Override
  public List<DataEntry> getEntries(String projectId, String token, String redisServer,
      int redisPort, boolean useSSL) throws Exception {

    LOG.info("getEntries: projectId=" + projectId + " server=" + redisServer
        + " port=" + redisPort + " useSSL=" + useSSL
        + " tokenEmpty=" + (token == null || token.isEmpty()));

    if (token == null || token.isEmpty()) {
      throw new Exception("No CloudDB token available. "
          + "Ensure the component's Token property has been populated.");
    }

    String host = resolveHost(redisServer);
    Jedis jedis = null;
    try {
      jedis = buildJedis(host, redisPort, useSSL, token);

      String prefix = projectId + ":";
      Set<String> keys = jedis.keys(prefix + "*");

      List<DataEntry> entries = new ArrayList<DataEntry>();
      for (String key : keys) {
        String tag = key.substring(prefix.length());
        String value = jedis.get(key);
        entries.add(new DataEntry(tag, value != null ? value : ""));
      }
      return entries;

    } catch (Exception e) {
      LOG.warning("CloudDB read failed for projectId=" + projectId
          + " host=" + host + ": " + e.getMessage());
      throw new Exception("Could not connect to CloudDB: " + e.getMessage());
    } finally {
      closeQuietly(jedis);
    }
  }

  @Override
  public void setEntry(String projectId, String token, String redisServer,
      int redisPort, boolean useSSL, String tag, String value) throws Exception {

    if (token == null || token.isEmpty()) {
      throw new Exception("No CloudDB token available.");
    }
    if (tag == null || tag.trim().isEmpty()) {
      throw new Exception("Tag must not be empty.");
    }

    String host = resolveHost(redisServer);
    Jedis jedis = null;
    try {
      jedis = buildJedis(host, redisPort, useSSL, token);
      jedis.set(projectId + ":" + tag, value != null ? value : "");
    } catch (Exception e) {
      LOG.warning("CloudDB setEntry failed for projectId=" + projectId
          + " tag=" + tag + ": " + e.getMessage());
      throw new Exception("Could not write to CloudDB: " + e.getMessage());
    } finally {
      closeQuietly(jedis);
    }
  }

  @Override
  public void deleteEntry(String projectId, String token, String redisServer,
      int redisPort, boolean useSSL, String tag) throws Exception {

    if (token == null || token.isEmpty()) {
      throw new Exception("No CloudDB token available.");
    }

    String host = resolveHost(redisServer);
    Jedis jedis = null;
    try {
      jedis = buildJedis(host, redisPort, useSSL, token);
      jedis.del(projectId + ":" + tag);
    } catch (Exception e) {
      LOG.warning("CloudDB deleteEntry failed for projectId=" + projectId
          + " tag=" + tag + ": " + e.getMessage());
      throw new Exception("Could not delete from CloudDB: " + e.getMessage());
    } finally {
      closeQuietly(jedis);
    }
  }

  private String resolveHost(String redisServer) throws Exception {
    String host = "DEFAULT".equals(redisServer) ? defaultServer : redisServer;
    if (host == null || host.isEmpty()) {
      throw new Exception("No CloudDB server configured (clouddb.server flag is empty).");
    }
    return host;
  }

  private Jedis buildJedis(String host, int port, boolean useSSL, String token) {
    JedisShardInfo shardInfo;
    if (useSSL) {
      shardInfo = new JedisShardInfo(host, port, CONNECTION_TIMEOUT_MS,
          true /* ssl */, null, null, null);
    } else {
      shardInfo = new JedisShardInfo(host, port, CONNECTION_TIMEOUT_MS);
    }
    shardInfo.setPassword(token);
    return new Jedis(shardInfo);
  }

  private void closeQuietly(Jedis jedis) {
    if (jedis != null) {
      try {
        jedis.close();
      } catch (Exception e) {
        LOG.warning("Error closing Jedis connection: " + e.getMessage());
      }
    }
  }
}
