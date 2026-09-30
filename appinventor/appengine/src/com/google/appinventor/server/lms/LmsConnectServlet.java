// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.OdeServlet;
import com.google.appinventor.server.encryption.EncryptionException;

import java.io.IOException;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Starts the Google Classroom OAuth 2.0 flow for the signed-in user.
 *
 * <p>This servlet runs behind {@code odeAuthFilter}, so the user id is taken from
 * the authenticated session via {@link #userInfoProvider} (never from a request
 * parameter). The id is sealed into the OAuth {@code state} by
 * {@link LmsOAuthState} so that the unauthenticated {@link LmsAuthCallbackServlet}
 * can recover it after Google's cross-origin redirect. The browser is then
 * redirected to Google's consent screen.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LmsConnectServlet extends OdeServlet {

  private static final Logger LOG = Logger.getLogger(LmsConnectServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (!LmsOAuthConfig.isConfigured()) {
      LOG.warning("Rejecting /lms/connect: Google Classroom OAuth is not configured.");
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Google Classroom integration is not configured.");
      return;
    }
    // odeAuthFilter guarantees an authenticated user before this servlet runs.
    String userId = userInfoProvider.getUserId();
    try {
      String codeVerifier = Pkce.newCodeVerifier();
      String state = LmsOAuthState.create(userId, codeVerifier);
      resp.sendRedirect(
          LmsOAuthConfig.buildAuthorizationUrl(state, Pkce.codeChallenge(codeVerifier)));
    } catch (EncryptionException e) {
      throw CrashReport.createAndLogError(LOG, req, "user=" + userId, e);
    }
  }
}
