// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.clouddb;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.OdeRemoteServiceServlet;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.util.SslUtils;
import com.google.appinventor.shared.rpc.clouddb.CloudDBDataService;
import com.google.appinventor.shared.rpc.clouddb.DataEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocketFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * Server-side implementation of {@link CloudDBDataService}.
 *
 * <p>Uses the token passed from the client (the component's Token property, which was issued by
 * {@code TokenAuthServiceImpl}) to authenticate with Redis, then reads or writes keys under the
 * given projectId prefix. All Jedis connections are closed in finally blocks. Jedis exception types
 * are wrapped as plain {@link Exception} because Jedis types are not in GWT's RPC serialization
 * whitelist.
 */
public class CloudDBDataServiceImpl extends OdeRemoteServiceServlet implements CloudDBDataService {

  private static final Logger LOG = Logger.getLogger(CloudDBDataServiceImpl.class.getName());
  private static final int CONNECTION_TIMEOUT_MS = 10000;
  private static final int MAX_VIZ_ENTRIES = 2000;

  private final String defaultServer = Flag.createFlag("clouddb.server", "").get();

  private static final String MIT_CA =
      "-----BEGIN CERTIFICATE-----\n"
          + "MIIFXjCCBEagAwIBAgIJAMLfrRWIaHLbMA0GCSqGSIb3DQEBCwUAMIHPMQswCQYD\n"
          + "VQQGEwJVUzELMAkGA1UECBMCTUExEjAQBgNVBAcTCUNhbWJyaWRnZTEuMCwGA1UE\n"
          + "ChMlTWFzc2FjaHVzZXR0cyBJbnN0aXR1dGUgb2YgVGVjaG5vbG9neTEZMBcGA1UE\n"
          + "CxMQTUlUIEFwcCBJbnZlbnRvcjEmMCQGA1UEAxMdQ2xvdWREQiBDZXJ0aWZpY2F0\n"
          + "ZSBBdXRob3JpdHkxEDAOBgNVBCkTB0Vhc3lSU0ExGjAYBgkqhkiG9w0BCQEWC2pp\n"
          + "c0BtaXQuZWR1MB4XDTE3MTIyMjIyMzkyOVoXDTI3MTIyMDIyMzkyOVowgc8xCzAJ\n"
          + "BgNVBAYTAlVTMQswCQYDVQQIEwJNQTESMBAGA1UEBxMJQ2FtYnJpZGdlMS4wLAYD\n"
          + "VQQKEyVNYXNzYWNodXNldHRzIEluc3RpdHV0ZSBvZiBUZWNobm9sb2d5MRkwFwYD\n"
          + "VQQLExBNSVQgQXBwIEludmVudG9yMSYwJAYDVQQDEx1DbG91ZERCIENlcnRpZmlj\n"
          + "YXRlIEF1dGhvcml0eTEQMA4GA1UEKRMHRWFzeVJTQTEaMBgGCSqGSIb3DQEJARYL\n"
          + "amlzQG1pdC5lZHUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDHzI3D\n"
          + "FobNDv2HTWlDdedmbxZIJYSqWlzdRJC3oVJgCubdAs46WJRqUxDRWft9UpYGMKkw\n"
          + "mYN8mdPby2m5OJagdVIZgnguB71zIQkC8yMzd94FC3gldX5m7R014D/0fkpzvsSt\n"
          + "6fsNectJT0k7gPELOH6t4u6AUbvIsEX0nNyRWsmA/ucXCsDBwXyBJxfOKIQ9tDI4\n"
          + "/WfcKk9JDpeMF7RP0CIOtlAPotKIaPoY1W3eMIi/0riOt5vTFsB8pxhxAVy0cfGX\n"
          + "iHukdrAkAJixTgkyS7wzk22xOeXVnRIzAMGK5xHMDw/HRQGTrUGfIXHENV3u+3Ae\n"
          + "L5/ZoQwyZTixmQNzAgMBAAGjggE5MIIBNTAdBgNVHQ4EFgQUZfMKQXqtC5UJGFrZ\n"
          + "gZE1nmlx+t8wggEEBgNVHSMEgfwwgfmAFGXzCkF6rQuVCRha2YGRNZ5pcfrfoYHV\n"
          + "pIHSMIHPMQswCQYDVQQGEwJVUzELMAkGA1UECBMCTUExEjAQBgNVBAcTCUNhbWJy\n"
          + "aWRnZTEuMCwGA1UEChMlTWFzc2FjaHVzZXR0cyBJbnN0aXR1dGUgb2YgVGVjaG5v\n"
          + "bG9neTEZMBcGA1UECxMQTUlUIEFwcCBJbnZlbnRvcjEmMCQGA1UEAxMdQ2xvdWRE\n"
          + "QiBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxEDAOBgNVBCkTB0Vhc3lSU0ExGjAYBgkq\n"
          + "hkiG9w0BCQEWC2ppc0BtaXQuZWR1ggkAwt+tFYhoctswDAYDVR0TBAUwAwEB/zAN\n"
          + "BgkqhkiG9w0BAQsFAAOCAQEAIkKr3eIvwZO6a1Jsh3qXwveVnrqwxYvLw2IhTwNT\n"
          + "/P6C5jbRnzUuDuzg5sEIpbBo/Bp3qIp7G5cdVOkIrqO7uCp6Kyc7d9lPsEe/cbF4\n"
          + "aNwNmdWroRN1y0tuMU6+z7frd5pOeAZP9E/DM/0Uaz4yVzwnlvZUttaLymyMhH54\n"
          + "isGQKbAqHDFtKZvb6DxsHzrO2YgeaBAtjeVhPWiv8BhzbOo9+hhZvYHYtoM2W+Ze\n"
          + "DHuvv0v+qouphftDKVBp16N8Pk5WgabTXzV6VcNee92iwbWYDEv06+S3AF/q2TBe\n"
          + "xxXtAa5ywbp6IRF37QuQChcYnOx7zIylYI1PIENfQFC2BA==\n"
          + "-----END CERTIFICATE-----\n";

  @Override
  public List<DataEntry> getEntries(String projectId, String token, String redisServer,
      int redisPort, boolean useSSL) throws Exception {

    String tokenPrefix = (token == null || token.isEmpty()) ? "<empty>"
        : token.substring(0, Math.min(8, token.length())) + "...";
    LOG.info("getEntries: projectId=" + projectId + " server=" + redisServer
        + " port=" + redisPort + " useSSL=" + useSSL
        + " tokenPrefix=" + tokenPrefix);

    if (token == null || token.isEmpty()) {
      throw new Exception("No CloudDB token available. "
          + "Ensure the component's Token property has been populated.");
    }

    String host = resolveHost(redisServer);
    Jedis jedis = null;
    try {
      jedis = buildJedis(host, redisPort, useSSL, token);

      // MIT's CloudDB server does not support SCAN or PING — use KEYS.
      // KEYS returns all matching keys in one response; we cap at
      // MAX_VIZ_ENTRIES and pipeline all GETs to avoid N round-trips.
      String prefix = projectId + ":";
      List<String> keys = new ArrayList<String>(jedis.keys(prefix + "*"));

      if (keys.size() > MAX_VIZ_ENTRIES) {
        LOG.info("getEntries: result capped at " + MAX_VIZ_ENTRIES
            + " for projectId=" + projectId + " (found " + keys.size() + " total)");
        keys = new ArrayList<String>(keys.subList(0, MAX_VIZ_ENTRIES));
      }

      if (keys.isEmpty()) {
        return new ArrayList<DataEntry>();
      }

      Pipeline pipe = jedis.pipelined();
      for (String key : keys) {
        pipe.get(key);
      }
      List<Object> rawValues = pipe.syncAndReturnAll();

      List<DataEntry> entries = new ArrayList<DataEntry>(keys.size());
      for (int i = 0; i < keys.size(); i++) {
        String tag = keys.get(i).substring(prefix.length());
        Object v = rawValues.get(i);
        entries.add(new DataEntry(tag, v != null ? v.toString() : ""));
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
    SSLSocketFactory sslSocketFactory = null;
    try {
      sslSocketFactory = SslUtils.createSocketFactoryFromCertString(MIT_CA);
    } catch (Exception e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
    Jedis jedis =
        new Jedis(host, port, CONNECTION_TIMEOUT_MS, 2000, useSSL, sslSocketFactory, null, null);
    jedis.connect();
    jedis.auth(token);
    return jedis;
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
