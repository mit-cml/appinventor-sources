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
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.common.annotations.VisibleForTesting;

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
 * id_token (signature against the platform JWKS, issuer, audience, nonce,
 * expiry, and, when configured, the deployment id), then establishes an App
 * Inventor session for the launched user by reusing the existing encrypted
 * AppInventor cookie, gives a learner their own project for the assignment, and
 * redirects into the IDE opened on that project. The grade service line item is
 * remembered for a later passback.
 *
 * <p>This is an exploration spike. It is lenient about role checks, and auto
 * accepts the terms of service for the provisioned user.
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
      if (!LtiConfig.deploymentId().isEmpty()
          && !LtiConfig.deploymentId().equals(claims.optString(LTI + "deployment_id"))) {
        fail(resp, "Unknown deployment id");
        return;
      }

      String messageType = claims.optString(LTI + "message_type");
      if ("LtiDeepLinkingRequest".equals(messageType)) {
        // The teacher is adding the assignment and picking a template.
        renderTemplatePicker(resp, claims);
        return;
      }

      String sub = claims.optString("sub", "");
      User user = userForLaunch(claims);
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

      long projectId = maybeForkStarterProject(user, claims);
      if (projectId > 0) {
        pointIdeAtProject(user.getUserId(), projectId);
      }

      LOG.info("LTI launch established a session for " + user.getUserEmail()
          + ", redirecting to the IDE");
      resp.sendRedirect("/");
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI launch failed", e);
      fail(resp, "Launch failed: " + e.getMessage());
    }
  }

  /**
   * Provisions or looks up the App Inventor user for the launch claims. When the
   * platform withholds the email for privacy, a stable pseudo address derived
   * from the subject claim keeps the same person on the same account across
   * launches.
   */
  private User userForLaunch(JSONObject claims) {
    String email = claims.optString("email", "");
    if (email.isEmpty()) {
      String sub = claims.optString("sub", "");
      email = (sub.isEmpty() ? "unknown" : sub) + "@lti.invalid";
    }
    User user = storageIo.getUserFromEmail(email);
    storageIo.setTosAccepted(user.getUserId());
    return user;
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
   * "fork"), once per assignment, mirroring the server side create path used by
   * RestServlet. The assignment to project link is stored durably per user, so
   * a relaunch finds the same project even after the activity is renamed or the
   * server restarts. Returns the project id for this assignment (newly created
   * or already existing), or -1 when no project applies (instructor, or
   * failure). Any failure here is logged and never blocks the launch.
   */
  private long maybeForkStarterProject(User user, JSONObject claims) {
    try {
      if (isInstructor(claims)) {
        LOG.info("LTI fork: launcher is an instructor, no student project created");
        return -1;
      }
      String userId = user.getUserId();
      String linkKey = resourceLinkKey(claims);
      if (!linkKey.isEmpty()) {
        long linked = LtiResourceLinks.get(userId, linkKey);
        if (linked > 0 && storageIo.getProjects(userId).contains(linked)) {
          LOG.info("LTI fork: " + user.getUserEmail() + " already has project " + linked
              + " for this assignment");
          return linked;
        }
      }
      String projectName = forkProjectName(claims);
      long existing = findProjectByName(userId, projectName);
      if (existing > 0) {
        // Created before the durable link existed (or by name only). Adopt it.
        if (!linkKey.isEmpty()) {
          LtiResourceLinks.put(userId, linkKey, existing);
        }
        LOG.info("LTI fork: " + user.getUserEmail() + " already has project " + projectName);
        return existing;
      }
      long projectId = forkNewProject(user, claims, projectName);
      if (projectId > 0 && !linkKey.isEmpty()) {
        LtiResourceLinks.put(userId, linkKey, projectId);
      }
      return projectId;
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI fork: could not create the student project (continuing)", e);
      return -1;
    }
  }

  /** Copies the teacher template, or creates a blank project when there is none. */
  private long forkNewProject(User user, JSONObject claims, String projectName) {
    long templateId = templateProjectId(claims);
    if (templateId > 0) {
      try {
        String ownerId = storageIo.getProjectUserId(templateId);
        long copied =
            projectService.copyProject(ownerId, templateId, projectName, user.getUserId());
        LOG.info("LTI fork: copied template " + templateId + " -> project " + copied + " ("
            + projectName + ") for " + user.getUserEmail());
        return copied;
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
    return projectId;
  }

  /** The durable assignment key from the launch claims, or empty if unavailable. */
  private static String resourceLinkKey(JSONObject claims) {
    JSONObject resourceLink = claims.optJSONObject(LTI + "resource_link");
    String id = (resourceLink == null) ? "" : resourceLink.optString("id", "");
    if (id.isEmpty()) {
      return "";
    }
    return LtiResourceLinks.key(
        claims.optString("iss", ""), claims.optString(LTI + "deployment_id", ""), id);
  }

  /** Returns the id of the user's project with the given name, or -1. */
  private long findProjectByName(String userId, String projectName) {
    for (long pid : storageIo.getProjects(userId)) {
      if (projectName.equals(storageIo.getProjectName(userId, pid))) {
        return pid;
      }
    }
    return -1;
  }

  /**
   * Points the IDE at the given project by storing it as the user's current
   * project, so that after the launch redirect the IDE opens the project that
   * belongs to this assignment instead of whatever the user had open last.
   * Reuses the same user setting that the IDE itself maintains.
   */
  private void pointIdeAtProject(String userId, long projectId) {
    try {
      String raw = storageIo.loadSettings(userId);
      JSONObject settings =
          (raw == null || raw.trim().isEmpty()) ? new JSONObject() : new JSONObject(raw);
      JSONObject general = settings.optJSONObject(SettingsConstants.USER_GENERAL_SETTINGS);
      if (general == null) {
        general = new JSONObject();
        settings.put(SettingsConstants.USER_GENERAL_SETTINGS, general);
      }
      general.put(SettingsConstants.GENERAL_SETTINGS_CURRENT_PROJECT_ID, String.valueOf(projectId));
      general.put(SettingsConstants.USER_AUTOLOAD_PROJECT, "true");
      storageIo.storeSettings(userId, settings.toString());
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI launch: could not point the IDE at project " + projectId, e);
    }
  }

  /**
   * Whether the launch carries a teaching role. Matches the fragment of the IMS
   * role vocabulary URIs, so a course or an institution level Instructor or
   * Administrator counts, and plain Learner launches do not.
   */
  private static boolean isInstructor(JSONObject claims) {
    JSONArray roles = claims.optJSONArray(LTI + "roles");
    if (roles == null) {
      return false;
    }
    for (int i = 0; i < roles.length(); i++) {
      String role = roles.optString(i);
      if (role.contains("#Instructor") || role.contains("#Administrator")
          || role.contains("#TeachingAssistant")) {
        return true;
      }
    }
    return false;
  }

  /**
   * A valid App Inventor project name for this assignment. Prefers the activity
   * title from the launch (for example "Exercise 2" becomes Exercise_2) so
   * students can tell which project belongs to which assignment, and falls back
   * to the opaque resource link id. The name is display only. Relaunch
   * idempotency comes from the stored resource link, so renaming the activity
   * does not fork a second project.
   */
  @VisibleForTesting
  static String forkProjectName(JSONObject claims) {
    JSONObject resourceLink = claims.optJSONObject(LTI + "resource_link");
    String title = (resourceLink == null) ? "" : resourceLink.optString("title", "");
    String name = title.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
    if (name.length() > 40) {
      name = name.substring(0, 40).replaceAll("_+$", "");
    }
    if (!name.isEmpty() && !Character.isLetter(name.charAt(0))) {
      name = "Project_" + name;
    }
    if (!name.isEmpty()) {
      return name;
    }
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
   * they can choose one as the assignment template. The platform return context is
   * held server side under a one time token, and the choice is posted to
   * /lti/deeplink/select, which returns the signed Deep Linking response to the LMS.
   */
  private void renderTemplatePicker(HttpServletResponse resp, JSONObject claims)
      throws IOException {
    resp.setContentType("text/html; charset=utf-8");
    User teacher = userForLaunch(claims);
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
    JSONObject dls = claims.optJSONObject(LTI_DL + "deep_linking_settings");
    String dlToken = LtiState.createDeepLink(
        (dls == null) ? "" : dls.optString("deep_link_return_url", ""),
        (dls == null) ? "" : dls.optString("data", ""),
        claims.optString(LTI + "deployment_id", ""));
    html.append("<form method='post' action='/lti/deeplink/select'>");
    html.append("<input type='hidden' name='dl' value='").append(escape(dlToken)).append("'>");
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
