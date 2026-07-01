// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.OdeAuthFilter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sends a grade back to the LMS for the signed in user, served at /lti/submit.
 * For the spike the student visits this after a launch to post a full score to
 * the line item remembered from the launch. A real integration would trigger
 * this from a Submit action in the IDE.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiSubmitServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiSubmitServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain; charset=utf-8");
    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);
    if (userInfo == null || userInfo.getUserId().isEmpty()) {
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      resp.getWriter().println("Not signed in. Launch the activity from the LMS first.");
      return;
    }
    LtiGradeContext.Context ctx = LtiGradeContext.get(userInfo.getUserId());
    if (ctx == null) {
      resp.getWriter().println(
          "No grade line item for this session. Launch the activity from the LMS, "
          + "and make sure grading is enabled on it.");
      return;
    }
    try {
      LtiAgs.postSubmission(ctx.lineItemUrl, ctx.ltiUserSub);
      resp.getWriter().println(
          "Submitted to your LMS. Your teacher will grade it, and the grade will then "
          + "appear in the LMS gradebook.");
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI grade passback failed", e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.getWriter().println("Grade passback failed: " + e.getMessage());
    }
  }
}
