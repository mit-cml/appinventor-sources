// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serves the tool public key as a JWK set at /lti/jwks, so the platform can
 * verify tokens the tool signs (grade passback). Registered with the platform
 * as the tool keyset URL.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiJwksServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiJwksServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      resp.setContentType("application/json; charset=utf-8");
      resp.getWriter().write(LtiKeys.jwksJson());
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI JWKS generation failed", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "JWKS error");
    }
  }
}
