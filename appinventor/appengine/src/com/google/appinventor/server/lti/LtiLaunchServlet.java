// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.OdeAuthFilter;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.user.User;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Receives the signed LTI 1.3 launch at /lti/launch. Validates the platform
 * id_token (signature against the platform JWKS, issuer, audience, nonce, and
 * expiry), then establishes an App Inventor session for the launched user by
 * reusing the existing encrypted AppInventor cookie, and redirects into the IDE.
 * The grade service line item is remembered for a later passback.
 *
 * <p>This is an exploration spike. It is lenient about role and deployment id
 * checks, and auto accepts the terms of service for the provisioned user.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiLaunchServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiLaunchServlet.class.getName());
  private static final String LTI = "https://purl.imsglobal.org/spec/lti/claim/";
  private static final String LTI_DL = "https://purl.imsglobal.org/spec/lti-dl/claim/";
  private static final String AGS = "https://purl.imsglobal.org/spec/lti-ags/claim/endpoint";

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();
  private final transient YoungAndroidProjectService projectService =
      new YoungAndroidProjectService(storageIo);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    handle(req, resp);
  }

  private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String idToken = req.getParameter("id_token");
      String state = req.getParameter("state");
      if (idToken == null || idToken.isEmpty()) {
        fail(resp, "Missing id_token");
        return;
      }
      String expectedNonce = LtiState.consumeNonce(state);
      if (expectedNonce == null) {
        fail(resp, "Invalid or expired state");
        return;
      }

      String jwks = LtiHttp.get(LtiConfig.jwksEndpoint());
      JSONObject claims = LtiJwt.verify(idToken, jwks);

      if (!LtiConfig.issuer().equals(claims.optString("iss"))) {
        fail(resp, "Issuer mismatch");
        return;
      }
      if (!audienceContains(claims.opt("aud"), LtiConfig.clientId())) {
        fail(resp, "Audience mismatch");
        return;
      }
      if (!expectedNonce.equals(claims.optString("nonce"))) {
        fail(resp, "Nonce mismatch");
        return;
      }
      long now = System.currentTimeMillis() / 1000L;
      if (claims.optLong("exp", 0) < now - 60) {
        fail(resp, "Token expired");
        return;
      }

      String messageType = claims.optString(LTI + "message_type");
      if ("LtiDeepLinkingRequest".equals(messageType)) {
        // The teacher is adding the assignment and picking a template.
        renderTemplatePicker(resp, claims);
        return;
      }

      String email = claims.optString("email", "");
      String sub = claims.optString("sub", "");
      if (email.isEmpty()) {
        // Moodle may withhold the email for privacy. Fall back to a stable
        // pseudo address derived from the subject so the user is consistent.
        email = (sub.isEmpty() ? "unknown" : sub) + "@lti.moodle.local";
      }

      User user = storageIo.getUserFromEmail(email);
      storageIo.setTosAccepted(user.getUserId());
      OdeAuthFilter.UserInfo userInfo = new OdeAuthFilter.UserInfo();
      userInfo.setUserId(user.getUserId());
      String cookie = userInfo.buildCookie(false);
      if (cookie != null) {
        Cookie cook = new Cookie("AppInventor", cookie);
        cook.setPath("/");
        resp.addCookie(cook);
      }

      JSONObject ags = claims.optJSONObject(AGS);
      if (ags != null) {
        LtiGradeContext.put(user.getUserId(), ags.optString("lineitem", ""), sub);
      }

      maybeForkStarterProject(user, claims);

      LOG.info("LTI launch established a session for " + email + ", redirecting to the IDE");
      resp.sendRedirect("/");
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI launch failed", e);
      fail(resp, "Launch failed: " + e.getMessage());
    }
  }

  private static boolean audienceContains(Object aud, String clientId) {
    if (clientId == null || clientId.isEmpty()) {
      return false;
    }
    if (aud instanceof String) {
      return aud.equals(clientId);
    }
    if (aud instanceof JSONArray) {
      JSONArray a = (JSONArray) aud;
      for (int i = 0; i < a.length(); i++) {
        if (clientId.equals(a.optString(i))) {
          return true;
        }
      }
    }
    return false;
  }

  private void fail(HttpServletResponse resp, String message) throws IOException {
    LOG.warning("LTI launch rejected: " + message);
    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    resp.setContentType("text/plain; charset=utf-8");
    resp.getWriter().println("LTI launch error: " + message);
  }

  /**
   * Gives a learner their own App Inventor project to work in (the student
   * "fork"), once per resource link, mirroring the server side create path used
   * by RestServlet. Any failure here is logged and never blocks the launch.
   */
  private void maybeForkStarterProject(User user, JSONObject claims) {
    try {
      if (isInstructor(claims)) {
        LOG.info("LTI fork: launcher is an instructor, no student project created");
        return;
      }
      String projectName = forkProjectName(claims);
      if (storageIo.getProjectNames(user.getUserId()).contains(projectName)) {
        LOG.info("LTI fork: " + user.getUserEmail() + " already has project " + projectName);
        return;
      }
      long templateId = templateProjectId(claims);
      if (templateId > 0) {
        try {
          String ownerId = storageIo.getProjectUserId(templateId);
          long copied = projectService.copyProject(ownerId, templateId, projectName, user.getUserId());
          LOG.info("LTI fork: copied template " + templateId + " -> project " + copied + " ("
              + projectName + ") for " + user.getUserEmail());
          return;
        } catch (Exception te) {
          LOG.log(Level.WARNING, "LTI fork: template " + templateId
              + " copy failed, falling back to a blank project", te);
        }
      }
      String packageName = StringUtils.getProjectPackage(user.getUserEmail(), projectName);
      NewYoungAndroidProjectParameters params = new NewYoungAndroidProjectParameters(packageName);
      long projectId = projectService.newProject(user.getUserId(), projectName, params);
      LOG.info("LTI fork: created blank project " + projectId + " (" + projectName + ") for "
          + user.getUserEmail());
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI fork: could not create the student project (continuing)", e);
    }
  }

  private static boolean isInstructor(JSONObject claims) {
    JSONArray roles = claims.optJSONArray(LTI + "roles");
    if (roles == null) {
      return false;
    }
    for (int i = 0; i < roles.length(); i++) {
      String role = roles.optString(i);
      if (role.contains("Instructor") || role.contains("Administrator")
          || role.contains("TeachingAssistant")) {
        return true;
      }
    }
    return false;
  }

  /** A valid, stable project name per resource link, so re-launch is idempotent. */
  private static String forkProjectName(JSONObject claims) {
    JSONObject resourceLink = claims.optJSONObject(LTI + "resource_link");
    String id = (resourceLink == null) ? "" : resourceLink.optString("id", "");
    String suffix = id.replaceAll("[^A-Za-z0-9]", "");
    return suffix.isEmpty() ? "AppInventorAssignment" : "Assignment_" + suffix;
  }

  /** The teacher-attached template project id from the launch custom claim, or -1. */
  private static long templateProjectId(JSONObject claims) {
    JSONObject custom = claims.optJSONObject(LTI + "custom");
    if (custom == null) {
      return -1;
    }
    String value = custom.optString("template_project_id", "");
    try {
      return value.isEmpty() ? -1 : Long.parseLong(value);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /**
   * Deep Linking: shows the teacher a picker of their own App Inventor projects so
   * they can choose one as the assignment template. The choice is posted to
   * /lti/deeplink/select, which returns the signed Deep Linking response to the LMS.
   */
  private void renderTemplatePicker(HttpServletResponse resp, JSONObject claims) throws IOException {
    String email = claims.optString("email", "");
    String sub = claims.optString("sub", "");
    if (email.isEmpty()) {
      email = (sub.isEmpty() ? "unknown" : sub) + "@lti.moodle.local";
    }
    User teacher = storageIo.getUserFromEmail(email);
    storageIo.setTosAccepted(teacher.getUserId());

    JSONObject dls = claims.optJSONObject(LTI_DL + "deep_linking_settings");
    String returnUrl = (dls == null) ? "" : dls.optString("deep_link_return_url", "");
    String data = (dls == null) ? "" : dls.optString("data", "");
    String deploymentId = claims.optString(LTI + "deployment_id", "");

    resp.setContentType("text/html; charset=utf-8");
    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>Choose a template</title>"
        + "<style>body{font-family:sans-serif;max-width:640px;margin:2rem auto;padding:0 1rem}"
        + "h2{color:#1a73e8}li{margin:.5rem 0;list-style:none}"
        + "button{background:#1a73e8;color:#fff;border:0;padding:.6rem 1.2rem;border-radius:4px;"
        + "font-size:1rem;cursor:pointer;margin-top:1rem}</style></head><body>");
    html.append("<h2>App Inventor — choose a template for this assignment</h2>");
    List<Long> projects = storageIo.getProjects(teacher.getUserId());
    if (projects.isEmpty()) {
      html.append("<p>You have no App Inventor projects yet. Open App Inventor, build a template "
          + "project first, then add this assignment again.</p></body></html>");
      resp.getWriter().write(html.toString());
      return;
    }
    html.append("<form method='post' action='/lti/deeplink/select'>");
    html.append("<input type='hidden' name='return_url' value='").append(escape(returnUrl)).append("'>");
    html.append("<input type='hidden' name='data' value='").append(escape(data)).append("'>");
    html.append("<input type='hidden' name='deployment_id' value='").append(escape(deploymentId)).append("'>");
    html.append("<ul>");
    boolean first = true;
    for (long pid : projects) {
      String name = storageIo.getProjectName(teacher.getUserId(), pid);
      html.append("<li><label><input type='radio' name='template_project_id' value='").append(pid)
          .append("'");
      if (first) {
        html.append(" checked");
        first = false;
      }
      html.append("> ").append(escape(name)).append("</label></li>");
    }
    html.append("</ul><button type='submit'>Use this as the assignment template</button>"
        + "</form></body></html>");
    resp.getWriter().write(html.toString());
  }

  private static String escape(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;");
  }
}
