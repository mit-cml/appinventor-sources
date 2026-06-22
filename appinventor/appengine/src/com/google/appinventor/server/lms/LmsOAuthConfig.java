// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.util.UriBuilder;
import com.google.common.annotations.VisibleForTesting;

/**
 * Configuration and authorization-URL construction for the Google Classroom
 * OAuth 2.0 flow, shared by {@link LmsConnectServlet} (start) and
 * {@link LmsAuthCallbackServlet} (callback). Values come from the
 * {@code <system-properties>} section of appengine-web.xml via {@link Flag}; no
 * secret is hard coded.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public final class LmsOAuthConfig {

  private static final Flag<String> CLIENT_ID_FLAG =
      Flag.createFlag("lms.google.classroom.client_id", "");
  private static final Flag<String> CLIENT_SECRET_FLAG =
      Flag.createFlag("lms.google.classroom.client_secret", "");
  private static final Flag<String> REDIRECT_URI_FLAG =
      Flag.createFlag("lms.google.classroom.redirect_uri", "");

  /** Google OAuth 2.0 authorization endpoint. */
  private static final String AUTHORIZATION_ENDPOINT =
      "https://accounts.google.com/o/oauth2/v2/auth";

  /**
   * OAuth scope requested at sign-in: read-only access to the user's Classroom
   * courses. The {@code drive.file} scope for exporting a project to the user's
   * Drive is added by the later Drive-upload work, so this sign-in PR requests
   * only the Classroom scope and keeps the stored token least privilege.
   */
  private static final String SCOPE =
      "https://www.googleapis.com/auth/classroom.courses.readonly";

  private LmsOAuthConfig() {}

  /** Returns whether the client id, secret, and redirect URI are all configured. */
  public static boolean isConfigured() {
    return !CLIENT_ID_FLAG.get().isEmpty()
        && !CLIENT_SECRET_FLAG.get().isEmpty()
        && !REDIRECT_URI_FLAG.get().isEmpty();
  }

  static String clientId() {
    return CLIENT_ID_FLAG.get();
  }

  static String clientSecret() {
    return CLIENT_SECRET_FLAG.get();
  }

  static String redirectUri() {
    return REDIRECT_URI_FLAG.get();
  }

  /**
   * Builds the Google authorization URL carrying {@code state} and the PKCE
   * {@code codeChallenge}.
   */
  static String buildAuthorizationUrl(String state, String codeChallenge) {
    return buildAuthorizationUrl(CLIENT_ID_FLAG.get(), REDIRECT_URI_FLAG.get(), state,
        codeChallenge);
  }

  @VisibleForTesting
  static String buildAuthorizationUrl(String clientId, String redirectUri, String state,
      String codeChallenge) {
    return new UriBuilder(AUTHORIZATION_ENDPOINT)
        .add("client_id", clientId)
        .add("redirect_uri", redirectUri)
        .add("response_type", "code")
        .add("scope", SCOPE)
        .add("access_type", "offline")
        .add("prompt", "consent")
        .add("code_challenge", codeChallenge)
        .add("code_challenge_method", "S256")
        .add("state", state)
        .build();
  }
}
