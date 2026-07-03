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
 * /lti/submit. The Submit to LMS item in the IDE Project menu posts here from a
 * same origin request. It posts to the grade line item remembered from the
 * launch with the state Submitted and PendingManual, so the teacher then grades
 * it in the LMS and the student sees that grade in the LMS gradebook.
 *
 * <p>Only POST performs the submission, and it requires a custom request header
 * that a cross site form cannot set and a cross site script cannot add without a
 * CORS preflight this server does not grant. A page on another site therefore
 * can not cause a submission on the student's behalf. A GET returns a short page
 * that points the student back to the Project menu.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiSubmitServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiSubmitServlet.class.getName());
  private static final String REQUEST_HEADER = "X-AppInventor-LTI";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html; charset=utf-8");
    resp.getWriter().write("<!DOCTYPE html><html><head><meta charset='utf-8'>"
        + "<title>Submit to LMS</title></head>"
        + "<body style='font-family:sans-serif;max-width:640px;margin:2rem auto;padding:0 1rem'>"
        + "<h2>Submit to LMS</h2>"
        + "<p>To submit your work, open the project in App Inventor and choose "
        + "Submit to LMS from the Project menu.</p></body></html>");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain; charset=utf-8");
    if (req.getHeader(REQUEST_HEADER) == null) {
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      resp.getWriter().println("Use Submit to LMS from the App Inventor Project menu.");
      return;
    }
    OdeAuthFilter.UserInfo userInfo = OdeAuthFilter.getUserInfo(req);
    if (userInfo == null || userInfo.getUserId() == null || userInfo.getUserId().isEmpty()) {
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      resp.getWriter().println("Not signed in. Launch the activity from the LMS first.");
      return;
    }
    long projectId;
    try {
      projectId = Long.parseLong(req.getParameter("projectId"));
    } catch (NumberFormatException e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().println("No project to submit.");
      return;
    }
    LtiGradeContext.Context ctx = LtiGradeContext.get(projectId);
    if (ctx == null || !userInfo.getUserId().equals(ctx.userId)) {
      // Not a gradable LTI project for this user, a plain project or one owned by
      // someone else. A non 2xx status is important, because the client shows
      // success only on 2xx.
      resp.setStatus(HttpServletResponse.SC_CONFLICT);
      resp.getWriter().println(
          "No grade line item for this project. Launch the activity from the LMS, "
          + "and make sure grading is enabled on it.");
      return;
    }
    try {
      LtiAgs.postSubmission(ctx.issuer, ctx.lineItemUrl, ctx.ltiUserSub);
      resp.getWriter().println(
          "Submitted to your LMS. Your teacher will grade it, and the grade will then "
          + "appear in the LMS gradebook.");
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI grade passback failed", e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.getWriter().println("Grade passback failed.");
    }
  }
}
