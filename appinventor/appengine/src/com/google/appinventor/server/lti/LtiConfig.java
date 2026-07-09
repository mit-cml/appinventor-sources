// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData;

/**
 * Configuration for the experimental LTI 1.3 tool that lets a Learning
 * Management System (for example Moodle) launch a student into App Inventor and
 * receive a grade back.
 *
 * <p>The platform endpoints default to a local Moodle on port 8080. The client
 * id and deployment id are assigned by the platform when an administrator
 * registers this tool, so they are read from flags and left empty by default.
 * The tool key pair is generated on first use and kept in the datastore.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public final class LtiConfig {

  // The platform (Moodle) side.
  private static final Flag<String> ISSUER =
      Flag.createFlag("lti.platform.issuer", "http://localhost:8080");
  private static final Flag<String> AUTH_ENDPOINT =
      Flag.createFlag("lti.platform.auth", "http://localhost:8080/mod/lti/auth.php");
  private static final Flag<String> TOKEN_ENDPOINT =
      Flag.createFlag("lti.platform.token", "http://localhost:8080/mod/lti/token.php");
  private static final Flag<String> JWKS_ENDPOINT =
      Flag.createFlag("lti.platform.jwks", "http://localhost:8080/mod/lti/certs.php");

  // The tool registration values, assigned by the platform on registration.
  private static final Flag<String> CLIENT_ID =
      Flag.createFlag("lti.tool.clientid", "");
  private static final Flag<String> DEPLOYMENT_ID =
      Flag.createFlag("lti.tool.deploymentid", "");

  // This server's own base URL.
  private static final Flag<String> TOOL_BASE_URL =
      Flag.createFlag("lti.tool.baseurl", "http://localhost:8888");

  // Whether the dynamic registration endpoint (/lti/register) is open. Off by
  // default, an administrator turns it on to register a platform, then off, so
  // an untrusted party cannot register itself as a platform.
  private static final Flag<Boolean> REGISTRATION_ENABLED =
      Flag.createFlag("lti.registration.enabled", false);

  // Whether loopback hosts and plain http are allowed on outbound LTI fetches.
  // True for local dev, false in production.
  private static final Flag<Boolean> ALLOW_INSECURE =
      Flag.createFlag("lti.allow.insecure", true);

  private LtiConfig() {}

  public static String issuer() {
    return ISSUER.get();
  }

  /**
   * The registered platform for an issuer, or null if none is enabled for it.
   * The single platform configured by flags is seeded into the datastore on the
   * first lookup, so an existing flag based setup keeps working and the
   * datastore then holds the source of truth. Additional platforms are added
   * straight to the datastore.
   */
  public static StoredData.LtiPlatformData platform(String issuer) {
    StorageIo storageIo = StorageIoInstanceHolder.getInstance();
    StoredData.LtiPlatformData found = storageIo.getLtiPlatform(issuer);
    if (found != null) {
      return found;
    }
    // Seed the flag configured platform only when no row for it exists yet. A row
    // that is present but disabled was left disabled on purpose, a dynamic
    // registration always lands disabled, so it is not seeded again or enabled again.
    if (issuer != null && issuer.equals(ISSUER.get()) && !CLIENT_ID.get().isEmpty()
        && !platformExists(issuer)) {
      StoredData.LtiPlatformData seeded = new StoredData.LtiPlatformData();
      seeded.issuer = ISSUER.get();
      seeded.clientId = CLIENT_ID.get();
      seeded.authEndpoint = AUTH_ENDPOINT.get();
      seeded.tokenEndpoint = TOKEN_ENDPOINT.get();
      seeded.jwksEndpoint = JWKS_ENDPOINT.get();
      seeded.deploymentId = DEPLOYMENT_ID.get();
      seeded.enabled = true;
      storageIo.storeLtiPlatform(seeded.issuer, seeded.clientId, seeded.authEndpoint,
          seeded.tokenEndpoint, seeded.jwksEndpoint, seeded.deploymentId, seeded.enabled);
      return seeded;
    }
    return null;
  }

  static boolean platformExists(String issuer) {
    for (StoredData.LtiPlatformData p : StorageIoInstanceHolder.getInstance().getLtiPlatforms()) {
      if (issuer.equals(p.issuer)) {
        return true;
      }
    }
    return false;
  }

  public static String toolBaseUrl() {
    return TOOL_BASE_URL.get();
  }

  /** The tool launch (redirect) URL registered with the platform. */
  public static String launchUrl() {
    return toolBaseUrl() + "/lti/launch";
  }

  /** The tool OIDC login initiation URL registered with the platform. */
  public static String loginUrl() {
    return toolBaseUrl() + "/lti/login";
  }

  /** The tool JWK set URL registered with the platform. */
  public static String jwksUrl() {
    return toolBaseUrl() + "/lti/jwks";
  }

  /** Whether the dynamic registration endpoint is currently open. */
  public static boolean registrationEnabled() {
    return REGISTRATION_ENABLED.get();
  }

  /** Whether loopback hosts and plain http are allowed on outbound LTI fetches. */
  public static boolean allowInsecure() {
    return ALLOW_INSECURE.get();
  }
}
