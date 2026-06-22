// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Minimal client for the Google OAuth 2.0 token endpoint, used by the Google
 * Classroom integration. Uses {@link HttpURLConnection} directly, matching the
 * prototype servlet and avoiding a new client-library dependency.
 *
 * <p>The client id, client secret, and authorization code are passed in as
 * arguments rather than read from configuration, so this client depends on
 * neither flags nor storage.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public final class GoogleOAuthClient {

  private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

  private GoogleOAuthClient() {}

  /**
   * Exchanges an authorization code for the token response JSON, which includes
   * the refresh token on the first consent. Used by the OAuth callback.
   *
   * @param clientId the OAuth client id
   * @param clientSecret the OAuth client secret
   * @param code the authorization code from Google's redirect
   * @param redirectUri the redirect uri registered for the client
   * @param codeVerifier the PKCE code verifier whose challenge was sent at authorization
   * @return the raw token response JSON
   * @throws IOException if the request fails
   */
  public static String exchangeAuthorizationCode(
      String clientId, String clientSecret, String code, String redirectUri, String codeVerifier)
      throws IOException {
    String body = "grant_type=authorization_code"
        + "&client_id=" + LmsHttp.urlEncode(clientId)
        + "&client_secret=" + LmsHttp.urlEncode(clientSecret)
        + "&code=" + LmsHttp.urlEncode(code)
        + "&redirect_uri=" + LmsHttp.urlEncode(redirectUri)
        + "&code_verifier=" + LmsHttp.urlEncode(codeVerifier);
    return postForm(body, "code exchange");
  }

  private static String postForm(String body, String label) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(TOKEN_ENDPOINT).openConnection();
    LmsHttp.applyTimeouts(conn);
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    conn.setRequestProperty("Accept", "application/json");
    try (OutputStream os = conn.getOutputStream()) {
      os.write(body.getBytes(StandardCharsets.UTF_8));
    }
    return LmsHttp.readBody(conn, label);
  }
}
