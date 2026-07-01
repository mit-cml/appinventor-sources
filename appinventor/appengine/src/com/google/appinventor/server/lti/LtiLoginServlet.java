// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.util.UriBuilder;

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
    String loginHint = req.getParameter("login_hint");
    String messageHint = req.getParameter("lti_message_hint");
    String clientId = req.getParameter("client_id");
    if (clientId == null || clientId.isEmpty()) {
      clientId = LtiConfig.clientId();
    }
    String[] stateNonce = LtiState.create();
    String url = new UriBuilder(LtiConfig.authEndpoint())
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
}
