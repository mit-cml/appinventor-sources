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
 * Marks the signed in student's work as submitted in the LMS, served at
 * /lti/submit. The Submit to LMS item in the IDE Project menu posts here. It
 * posts to the grade line item remembered from the launch with the state
 * Submitted and PendingManual, so the teacher then grades it in the LMS and the
 * student sees that grade in the LMS gradebook. A plain browser visit gets a
 * confirmation page instead, so that merely following a link can not trigger a
 * submission.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiSubmitServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiSubmitServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);
    if (userInfo == null || userInfo.getUserId().isEmpty()) {
      notSignedIn(resp);
      return;
    }
    resp.setContentType("text/html; charset=utf-8");
    resp.getWriter().write("<!DOCTYPE html><html><head><meta charset='utf-8'>"
        + "<title>Submit to LMS</title></head>"
        + "<body style='font-family:sans-serif;max-width:640px;margin:2rem auto;padding:0 1rem'>"
        + "<h2>Submit this assignment to your LMS?</h2>"
        + "<p>This tells your LMS that your work is ready for grading.</p>"
        + "<form method='post' action='/lti/submit'><button type='submit' "
        + "style='background:#1a73e8;color:#fff;border:0;padding:.6rem 1.2rem;"
        + "border-radius:4px;font-size:1rem;cursor:pointer'>Submit</button></form>"
        + "</body></html>");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain; charset=utf-8");
    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);
    if (userInfo == null || userInfo.getUserId().isEmpty()) {
      notSignedIn(resp);
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

  private void notSignedIn(HttpServletResponse resp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    resp.setContentType("text/plain; charset=utf-8");
    resp.getWriter().println("Not signed in. Launch the activity from the LMS first.");
  }
}
