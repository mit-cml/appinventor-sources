// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import junit.framework.TestCase;

/**
 * Tests for {@link LmsOAuthConfig}'s authorization-URL construction.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LmsOAuthConfigTest extends TestCase {

  public void testBuildAuthorizationUrlHasAllRequiredParams() {
    String url = LmsOAuthConfig.buildAuthorizationUrl(
        "the-client-id", "http://localhost:8888/lms/auth/callback", "STATE-123", "CHALLENGE-xyz");
    assertTrue(url.startsWith("https://accounts.google.com/o/oauth2/v2/auth"));
    assertTrue(url.contains("client_id=the-client-id"));
    assertTrue(url.contains("redirect_uri="));
    assertTrue(url.contains("response_type=code"));
    assertTrue(url.contains("access_type=offline"));  // needed to receive a refresh token
    assertTrue(url.contains("prompt=consent"));        // forces a refresh token each consent
    assertTrue(url.contains("code_challenge=CHALLENGE-xyz"));  // PKCE challenge
    assertTrue(url.contains("code_challenge_method=S256"));    // PKCE method
    assertTrue(url.contains("state=STATE-123"));
  }

  public void testBuildAuthorizationUrlRequestsClassroomAndDriveScopes() {
    String url = LmsOAuthConfig.buildAuthorizationUrl("cid", "http://localhost/cb", "s", "ch");
    assertTrue(url.contains("classroom.courses.readonly"));
    // The Drive upload needs the per-file drive.file scope, requested at sign-in.
    assertTrue(url.contains("drive.file"));
  }

  public void testIsConfiguredFalseWhenSecretsUnset() {
    // With no system properties set, the client id and secret default to empty,
    // so the connect servlet's configuration gate is closed.
    assertFalse(LmsOAuthConfig.isConfigured());
  }
}
