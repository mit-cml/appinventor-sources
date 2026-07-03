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
import com.google.appinventor.server.storage.StoredData;
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
 * <p>This is an exploration spike. It auto accepts the terms of service for
 * the provisioned account.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiLaunchServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiLaunchServlet.class.getName());
  private static final String LTI = "https://purl.imsglobal.org/spec/lti/claim/";
  private static final String LTI_DL = "https://purl.imsglobal.org/spec/lti-dl/claim/";
  private static final String AGS = "https://purl.imsglobal.org/spec/lti-ags/claim/endpoint";
  private static final long CLOCK_SKEW_SECONDS = 60;
  private static final int MAX_PROJECT_NAME_LENGTH = 40;

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();
  private final transient YoungAndroidProjectService projectService =
      new YoungAndroidProjectService(storageIo);

  // Only POST is served. LTI delivers the launch with response_mode=form_post,
  // so accepting GET would only risk the signed id_token landing in a URL or a
  // log. A GET therefore gets the servlet default 405.
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String idToken = req.getParameter("id_token");
      String state = req.getParameter("state");
      if (idToken == null || idToken.isEmpty()) {
        fail(resp, "Missing id_token");
        return;
      }
      LtiState.Entry stateEntry = LtiState.consume(state);
      if (stateEntry == null) {
        fail(resp, "Invalid or expired state");
        return;
      }
      StoredData.LtiPlatformData platform = LtiConfig.platform(stateEntry.issuer);
      if (platform == null) {
        fail(resp, "Unknown platform issuer");
        return;
      }

      String jwks = LtiHttp.get(platform.jwksEndpoint);
      JSONObject claims = LtiJwt.verify(idToken, jwks);

      if (!platform.issuer.equals(claims.optString("iss"))) {
        fail(resp, "Issuer mismatch");
        return;
      }
      if (!audienceContains(claims.opt("aud"), platform.clientId)) {
        fail(resp, "Audience mismatch");
        return;
      }
      String nonce = claims.optString("nonce");
      if (!stateEntry.nonce.equals(nonce)) {
        fail(resp, "Nonce mismatch");
        return;
      }
      long now = System.currentTimeMillis() / 1000L;
      if (claims.optLong("exp", 0) < now - CLOCK_SKEW_SECONDS) {
        fail(resp, "Token expired");
        return;
      }
      if (!platform.deploymentId.isEmpty()
          && !platform.deploymentId.equals(claims.optString(LTI + "deployment_id"))) {
        fail(resp, "Unknown deployment id");
        return;
      }
      if (claims.optString("sub").isEmpty()) {
        // Without a subject every launcher would share one provisioned account.
        fail(resp, "Missing subject");
        return;
      }
      // Consume the one time nonce only after every other check has passed, and
      // in the datastore so a captured launch cannot be replayed on another
      // server instance.
      if (!storageIo.useLtiNonce(nonce)) {
        fail(resp, "Replayed launch");
        return;
      }

      String messageType = claims.optString(LTI + "message_type");
      if ("LtiDeepLinkingRequest".equals(messageType)) {
        // The teacher is adding the assignment and picking a template. Only an
        // instructor may do this, in addition to the per project ownership check
        // in the selection step.
        if (!isInstructor(claims)) {
          fail(resp, "Only an instructor can add an App Inventor assignment");
          return;
        }
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
        LtiGradeContext.put(user.getUserId(), platform.issuer, ags.optString("lineitem", ""), sub);
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
      fail(resp, "Launch failed");
    }
  }

  /**
   * Provisions or looks up the App Inventor account for the launch. The account
   * is namespaced by the platform issuer and the stable subject claim, so an
   * LTI launch can only ever reach an LTI provisioned account and never a Google
   * signed in user, whatever email the platform asserts. Linking a launch to an
   * existing App Inventor account, if wanted, would be a separate explicit step.
   */
  private User userForLaunch(JSONObject claims) {
    String issuer = claims.optString("iss", "");
    String sub = claims.optString("sub", "");
    String linkedUserId = storageIo.getLtiUserId(issuer, sub);
    User user;
    if (linkedUserId != null) {
      user = storageIo.getUser(linkedUserId);
    } else {
      // First launch for this platform identity. Provision the namespaced
      // account and record the durable link, so the account id stays stable
      // even if the account key derivation ever changes.
      user = storageIo.getUserFromEmail(ltiAccountKey(issuer, sub));
      storageIo.storeLtiUserLink(issuer, sub, user.getUserId());
    }
    storageIo.setTosAccepted(user.getUserId());
    return user;
  }

  /**
   * A stable account key for a launch, derived from the full platform issuer and
   * subject so two distinct launchers never share an account, and placed in the
   * reserved .lti.invalid space so it can never equal a real login email. The
   * digest avoids the collisions a lossy character replacement would cause on
   * platforms whose subjects contain punctuation.
   */
  @VisibleForTesting
  static String ltiAccountKey(String issuer, String sub) {
    try {
      return "lti-" + LtiJwt.hex(LtiJwt.sha256(issuer + "\n" + sub)).substring(0, 32)
          + "@lti.invalid";
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
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
      String issuer = claims.optString("iss", "");
      String deploymentId = claims.optString(LTI + "deployment_id", "");
      String resourceLinkId = resourceLinkId(claims);
      if (!resourceLinkId.isEmpty()) {
        long linked = LtiResourceLinks.get(userId, issuer, deploymentId, resourceLinkId);
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
        if (!resourceLinkId.isEmpty()) {
          LtiResourceLinks.put(userId, issuer, deploymentId, resourceLinkId, existing);
        }
        LOG.info("LTI fork: " + user.getUserEmail() + " already has project " + projectName);
        return existing;
      }
      long projectId = forkNewProject(user, claims, projectName);
      if (projectId > 0 && !resourceLinkId.isEmpty()) {
        LtiResourceLinks.put(userId, issuer, deploymentId, resourceLinkId, projectId);
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

  /** The resource link id from the launch claims, or empty if unavailable. */
  private static String resourceLinkId(JSONObject claims) {
    JSONObject resourceLink = claims.optJSONObject(LTI + "resource_link");
    return (resourceLink == null) ? "" : resourceLink.optString("id", "");
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
   * project and turning on autoload of the last project, so that after the
   * launch redirect the IDE opens the project for this assignment rather than
   * the projects list. Both are the same user settings the IDE itself maintains,
   * and the IDE opens the current project only when autoload is on. For an LTI
   * provisioned account, created for exactly this purpose, that is the wanted
   * default.
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
   * role vocabulary URIs, so a course or institution level Instructor,
   * Administrator, or Teaching Assistant counts, and plain Learner launches do
   * not.
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
    if (!name.isEmpty() && !Character.isLetter(name.charAt(0))) {
      name = "Project_" + name;
    }
    if (name.length() > MAX_PROJECT_NAME_LENGTH) {
      name = name.substring(0, MAX_PROJECT_NAME_LENGTH).replaceAll("_+$", "");
    }
    if (!name.isEmpty()) {
      return name;
    }
    String id = (resourceLink == null) ? "" : resourceLink.optString("id", "");
    String suffix = id.replaceAll("[^A-Za-z0-9]", "");
    return suffix.isEmpty() ? "AppInventorAssignment" : "Assignment_" + suffix;
  }

  /**
   * The teacher-attached template project id from the launch custom claim, or
   * -1. The value is a short JWT the tool signed for itself when the teacher
   * selected a template they own, so it is verified here against the tool key
   * before it is trusted. A custom parameter set outside the Deep Linking flow,
   * for example by hand editing the activity, carries no valid signature and is
   * therefore ignored.
   */
  private static long templateProjectId(JSONObject claims) {
    JSONObject custom = claims.optJSONObject(LTI + "custom");
    if (custom == null) {
      return -1;
    }
    String ref = custom.optString("template", "");
    if (ref.isEmpty()) {
      return -1;
    }
    try {
      JSONObject refClaims = LtiJwt.verify(ref, LtiKeys.jwksJson());
      if (!refClaims.optString("iss").equals(claims.optString("iss"))) {
        // The reference was signed for a different platform, so it does not apply.
        return -1;
      }
      String value = refClaims.optString("template_project_id", "");
      return value.isEmpty() ? -1 : Long.parseLong(value);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI fork: ignoring an unverifiable template reference", e);
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
        claims.optString(LTI + "deployment_id", ""),
        claims.optString("iss", ""),
        teacher.getUserId());
    html.append("<form method='post' action='/lti/deeplink/select'>");
    html.append("<input type='hidden' name='dl' value='").append(LtiHtml.escape(dlToken))
        .append("'>");
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
      html.append("> ").append(LtiHtml.escape(name)).append("</label></li>");
    }
    html.append("</ul><button type='submit'>Use this as the assignment template</button>"
        + "</form></body></html>");
    resp.getWriter().write(html.toString());
  }

}
