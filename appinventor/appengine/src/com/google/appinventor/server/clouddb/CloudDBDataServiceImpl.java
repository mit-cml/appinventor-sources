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
 * issued by {@code TokenAuthServiceImpl}) to authenticate with Redis, then fetches all
 * keys under the given projectId prefix and returns them as {@link DataEntry} objects.
 * The Jedis connection is always closed in a finally block.
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

    String host = "DEFAULT".equals(redisServer) ? defaultServer : redisServer;
    if (host == null || host.isEmpty()) {
      throw new Exception("No CloudDB server configured (clouddb.server flag is empty).");
    }

    Jedis jedis = null;
    try {
      JedisShardInfo shardInfo;
      if (useSSL) {
        // Pass null for sslSocketFactory / sslParameters / hostnameVerifier to use the
        // JVM's default SSL context, which trusts standard CA roots on the server.
        shardInfo = new JedisShardInfo(host, redisPort, CONNECTION_TIMEOUT_MS,
            true /* ssl */, null, null, null);
      } else {
        shardInfo = new JedisShardInfo(host, redisPort, CONNECTION_TIMEOUT_MS);
      }
      shardInfo.setPassword(token);
      jedis = new Jedis(shardInfo);

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
      // Jedis exception types (JedisConnectionException, JedisDataException, etc.) are
      // not in GWT's RPC serialization whitelist. Wrap them in a plain Exception so the
      // message reaches the client without a secondary SerializationException.
      LOG.warning("CloudDB read failed for projectId=" + projectId
          + " host=" + host + ": " + e.getMessage());
      throw new Exception("Could not connect to CloudDB: " + e.getMessage());
    } finally {
      if (jedis != null) {
        try {
          jedis.close();
        } catch (Exception e) {
          LOG.warning("Error closing Jedis connection: " + e.getMessage());
        }
      }
    }
  }

}
