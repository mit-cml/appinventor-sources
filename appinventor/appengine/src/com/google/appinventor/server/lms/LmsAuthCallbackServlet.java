// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.encryption.EncryptionException;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Completes the Google Classroom OAuth 2.0 flow.
 *
 * <p>Google redirects here after the user consents. The request is cross-origin
 * and carries no App Inventor session, so this servlet is intentionally NOT
 * behind {@code odeAuthFilter}. It recovers the user id from the encrypted
 * {@link LmsOAuthState}, exchanges the authorization code for tokens via
 * {@link GoogleOAuthClient}, stores the refresh token encrypted via
 * {@link LmsCredentialStore}, and redirects the browser back into App Inventor.
 * No token is ever written to the response.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LmsAuthCallbackServlet extends HttpServlet {

  private static final Logger LOG =
      Logger.getLogger(LmsAuthCallbackServlet.class.getName());

  /** Where to send the browser after the flow completes. */
  private static final String RETURN_URL = "/";

  private final LmsCredentialStore credentialStore = new LmsCredentialStore();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (req.getParameter("error") != null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google sign in did not complete.");
      return;
    }
    String code = req.getParameter("code");
    String state = req.getParameter("state");
    if (code == null || state == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
      return;
    }
    LmsOAuthState.Payload payload = LmsOAuthState.verify(state);
    if (payload == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Invalid or expired sign in. Please try again.");
      return;
    }
    String userId = payload.userId();
    try {
      String tokenJson = GoogleOAuthClient.exchangeAuthorizationCode(
          LmsOAuthConfig.clientId(), LmsOAuthConfig.clientSecret(), code,
          LmsOAuthConfig.redirectUri(), payload.codeVerifier());
      String refreshToken = LmsHttp.jsonField(tokenJson, "refresh_token");
      if (refreshToken != null) {
        credentialStore.saveGoogleRefreshToken(userId, refreshToken);
      }
    } catch (IOException e) {
      // A redeemed or expired authorization code (for example when the user hits
      // Back or refreshes the callback) or a transient failure reaching Google: the
      // user should retry, so this is a bad request, not a server crash. The request
      // is not handed to the logger because its query string carries the single-use
      // code and state.
      LOG.log(Level.WARNING, "Google token exchange failed for user " + userId, e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Google sign in could not be completed. Please try again.");
      return;
    } catch (EncryptionException e) {
      // Encrypting the refresh token failed: a genuine server-side fault.
      throw CrashReport.createAndLogError(LOG, null, "user=" + userId, e);
    }
    // Google returns a refresh token only when one is newly granted; prompt=consent
    // should force that. If nothing is stored (and none was stored before), the
    // connection did not complete, so do not report success.
    if (!credentialStore.hasGoogleCredential(userId)) {
      LOG.warning("No Google refresh token stored for user " + userId);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Google did not return a long-term credential. If this account is already "
          + "connected, remove App Inventor's access in your Google Account and try again.");
      return;
    }
    resp.sendRedirect(RETURN_URL);
  }
}
