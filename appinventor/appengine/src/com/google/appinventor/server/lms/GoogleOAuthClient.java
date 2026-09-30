// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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

  /**
   * Exchanges a stored refresh token for a fresh token response JSON, which
   * includes a short-lived access token. Used when a Google API call needs an
   * access token but only the long-lived refresh token is stored.
   *
   * @param clientId the OAuth client id
   * @param clientSecret the OAuth client secret
   * @param refreshToken the stored Google OAuth refresh token
   * @return the raw token response JSON, including the {@code access_token} field
   * @throws IOException if the request fails, including an {@code invalid_grant}
   *     error when the refresh token has been revoked or has expired
   */
  public static String refreshAccessToken(
      String clientId, String clientSecret, String refreshToken) throws IOException {
    String body = "grant_type=refresh_token"
        + "&client_id=" + LmsHttp.urlEncode(clientId)
        + "&client_secret=" + LmsHttp.urlEncode(clientSecret)
        + "&refresh_token=" + LmsHttp.urlEncode(refreshToken);
    return postForm(body, "token refresh");
  }

  private static String postForm(String body, String label) throws IOException {
    HttpURLConnection conn = LmsHttp.open(TOKEN_ENDPOINT);
    try {
      LmsHttp.applyTimeouts(conn);
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Accept", "application/json");
      // Do not reuse a pooled keep-alive connection, which can surface as an
      // "Unexpected end of file from server" once the socket has gone stale.
      conn.setRequestProperty("Connection", "close");
      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes(StandardCharsets.UTF_8));
      }
      return LmsHttp.readBody(conn, label);
    } finally {
      conn.disconnect();
    }
  }
}
