// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StoredData;
import com.google.appinventor.server.util.UriBuilder;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OIDC third party initiated login for LTI 1.3, served at /lti/login. The
 * platform sends the user here to start a launch. The tool mints a state and a
 * nonce, stores them server side, and redirects the browser to the platform
 * authorization endpoint, which then posts the signed launch back to /lti/launch.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiLoginServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String iss = req.getParameter("iss");
    if (iss == null || iss.isEmpty()) {
      // iss, login_hint, and target_link_uri are required login initiation parameters (Security
      // Framework 5.1.1.1). A protocol request that omits iss is rejected rather than defaulted
      // to a configured issuer.
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing iss");
      return;
    }
    StoredData.LtiPlatformData platform = LtiConfig.platform(iss);
    if (platform == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown platform issuer");
      return;
    }
    String loginHint = req.getParameter("login_hint");
    if (loginHint == null || loginHint.isEmpty()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing login_hint");
      return;
    }
    String targetLinkUri = req.getParameter("target_link_uri");
    if (targetLinkUri == null || targetLinkUri.isEmpty()) {
      // Presence only. The signed launch is what actually routes, so target_link_uri is not
      // compared against a registered value here, which could reject a conformant launch.
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing target_link_uri");
      return;
    }
    String messageHint = req.getParameter("lti_message_hint");
    String clientId = resolveClientId(req.getParameter("client_id"), platform.clientId);
    if (clientId == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown client_id for issuer");
      return;
    }
    if (!LtiHttp.browserUrlAllowed(platform.authEndpoint, LtiConfig.allowInsecure())) {
      // The browser is about to be redirected here with state and nonce, so it must be https
      // outside development (LTI Core 3.5).
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Insecure platform authorization endpoint");
      return;
    }
    String[] stateNonce = LtiState.create(iss);
    String url = new UriBuilder(platform.authEndpoint)
        .add("scope", "openid")
        .add("response_type", "id_token")
        .add("response_mode", "form_post")
        .add("prompt", "none")
        .add("client_id", clientId)
        .add("redirect_uri", LtiConfig.launchUrl())
        .add("state", stateNonce[0])
        .add("nonce", stateNonce[1])
        .add("login_hint", loginHint)
        .add("lti_message_hint", messageHint)
        .build();
    resp.sendRedirect(url);
  }

  /**
   * The client_id to place in the authorization request, or null if the request must be
   * rejected. The platform may echo client_id in the login initiation; when present it must
   * equal the one registered for this issuer, so an arbitrary caller supplied value is never
   * reflected into the authorization request. When absent the registered id is used.
   */
  @VisibleForTesting
  static String resolveClientId(String supplied, String registered) {
    if (supplied == null || supplied.isEmpty()) {
      return registered;
    }
    return supplied.equals(registered) ? registered : null;
  }
}
