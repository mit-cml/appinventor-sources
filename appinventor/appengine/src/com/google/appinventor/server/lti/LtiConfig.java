// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.flags.Flag;

/**
 * Configuration for the experimental LTI 1.3 tool that lets a Learning
 * Management System (for example Moodle) launch a student into App Inventor and
 * receive a grade back. This is an exploration spike, not production code.
 *
 * <p>The platform endpoints default to a local Moodle on port 8080. The client
 * id and deployment id are assigned by the platform when an administrator
 * registers this tool, so they are read from flags and left empty by default.
 * The tool key pair is generated once and stored under WEB-INF.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public final class LtiConfig {

  // The platform (Moodle) side. Default to a local Moodle on 8080.
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

  // The tool key pair (PKCS8 and X.509 DER) and this server's own base URL.
  private static final Flag<String> PRIVATE_KEY_FILE =
      Flag.createFlag("lti.tool.privatekey", "WEB-INF/lti_private_key.der");
  private static final Flag<String> PUBLIC_KEY_FILE =
      Flag.createFlag("lti.tool.publickey", "WEB-INF/lti_public_key.der");
  private static final Flag<String> TOOL_BASE_URL =
      Flag.createFlag("lti.tool.baseurl", "http://localhost:8888");

  /** Key id advertised in the tool JWKS and in tokens the tool signs. */
  public static final String KID = "appinventor-lti-1";

  private LtiConfig() {}

  public static String issuer() {
    return ISSUER.get();
  }

  public static String authEndpoint() {
    return AUTH_ENDPOINT.get();
  }

  public static String tokenEndpoint() {
    return TOKEN_ENDPOINT.get();
  }

  public static String jwksEndpoint() {
    return JWKS_ENDPOINT.get();
  }

  public static String clientId() {
    return CLIENT_ID.get();
  }

  public static String deploymentId() {
    return DEPLOYMENT_ID.get();
  }

  public static String privateKeyFile() {
    return PRIVATE_KEY_FILE.get();
  }

  public static String publicKeyFile() {
    return PUBLIC_KEY_FILE.get();
  }

  public static String toolBaseUrl() {
    return TOOL_BASE_URL.get();
  }

  /** The tool launch (redirect) URL registered with the platform. */
  public static String launchUrl() {
    return toolBaseUrl() + "/lti/launch";
  }
}
