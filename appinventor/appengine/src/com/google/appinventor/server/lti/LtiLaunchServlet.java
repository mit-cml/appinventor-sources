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
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * <p>An exploration spike. It auto accepts the terms of service for the
 * provisioned account.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiLaunchServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiLaunchServlet.class.getName());
  private static final String LTI = "https://purl.imsglobal.org/spec/lti/claim/";
  private static final String LTI_DL = "https://purl.imsglobal.org/spec/lti-dl/claim/";
  private static final String AGS = "https://purl.imsglobal.org/spec/lti-ags/claim/endpoint";
  private static final String LIS = "http://purl.imsglobal.org/vocab/lis/v2/";
  private static final long CLOCK_SKEW_SECONDS = 60;
  private static final int MAX_PROJECT_NAME_LENGTH = 40;

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();
  private final YoungAndroidProjectService projectService =
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
      LtiState.Entry stateEntry = LtiState.peek(state);
      if (stateEntry == null) {
        fail(resp, "Invalid or expired state");
        return;
      }
      StoredData.LtiPlatformData platform = LtiConfig.platform(stateEntry.issuer);
      if (platform == null) {
        fail(resp, "Unknown platform issuer");
        return;
      }

      String jwks;
      try {
        jwks = LtiHttp.get(platform.jwksEndpoint);
      } catch (IOException e) {
        LOG.log(Level.WARNING, "LTI launch: could not reach the platform key set", e);
        failServer(resp, HttpServletResponse.SC_BAD_GATEWAY);
        return;
      }
      JSONObject claims;
      try {
        claims = LtiJwt.verify(idToken, jwks);
      } catch (Exception e) {
        fail(resp, "Token verification failed");
        return;
      }
      // The token is proven, so spend the one time state now. A failure before
      // this point leaves the state for the honest platform to retry.
      LtiState.consume(state);

      if (!platform.issuer.equals(claims.optString("iss"))) {
        fail(resp, "Issuer mismatch");
        return;
      }
      Object aud = claims.opt("aud");
      if (!audienceContains(aud, platform.clientId)) {
        fail(resp, "Audience mismatch");
        return;
      }
      if (aud instanceof JSONArray && ((JSONArray) aud).length() > 1
          && !platform.clientId.equals(claims.optString("azp"))) {
        // With more than one audience the authorized party must name this tool.
        fail(resp, "Authorized party mismatch");
        return;
      }
      String nonce = claims.optString("nonce");
      if (!stateEntry.nonce.equals(nonce)) {
        fail(resp, "Nonce mismatch");
        return;
      }
      long now = System.currentTimeMillis() / 1000L;
      if (!tokenTimeValid(claims, now)) {
        fail(resp, "Token timing invalid");
        return;
      }
      if (!"1.3.0".equals(claims.optString(LTI + "version"))) {
        fail(resp, "Unsupported LTI version");
        return;
      }
      String deploymentId = claims.optString(LTI + "deployment_id");
      if (deploymentId.isEmpty()) {
        fail(resp, "Missing deployment id");
        return;
      }
      if (!platform.deploymentId.isEmpty() && !platform.deploymentId.equals(deploymentId)) {
        fail(resp, "Unknown deployment id");
        return;
      }
      if (claims.optString("sub").isEmpty()) {
        // Without a subject every launcher would share one provisioned account.
        fail(resp, "Missing subject");
        return;
      }
      if (claims.optJSONArray(LTI + "roles") == null) {
        fail(resp, "Missing or malformed roles");
        return;
      }
      // Consume the one time nonce only after the token checks pass, as a
      // transactional datastore write so two racing replays cannot both succeed.
      if (!storageIo.useLtiNonce(nonce)) {
        fail(resp, "Replayed launch");
        return;
      }
      storageIo.cleanupLtiNonces();

      String messageType = claims.optString(LTI + "message_type");
      if ("LtiDeepLinkingRequest".equals(messageType)) {
        // The teacher is adding the assignment and picking a template. Only an
        // instructor may do this, in addition to the per project ownership check
        // in the selection step.
        if (!isInstructor(claims)) {
          fail(resp, "Only an instructor can add an App Inventor assignment");
          return;
        }
        renderTemplatePicker(resp, claims, userForLaunch(claims));
        return;
      }
      if (!"LtiResourceLinkRequest".equals(messageType)) {
        fail(resp, "Unsupported message type");
        return;
      }
      if (resourceLinkId(claims).isEmpty()) {
        fail(resp, "Missing resource link id");
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

      long projectId = maybeForkStarterProject(user, claims);
      if (projectId > 0) {
        pointIdeAtProject(user.getUserId(), projectId);
        JSONObject ags = claims.optJSONObject(AGS);
        if (ags != null) {
          LtiGradeContext.put(projectId, user.getUserId(), platform.issuer,
              ags.optString("lineitem", ""), sub);
        }
      }

      LOG.info("LTI launch established a session for " + user.getUserEmail()
          + ", redirecting to the IDE");
      resp.sendRedirect("/");
    } catch (Exception e) {
      LOG.log(Level.WARNING, "LTI launch failed", e);
      failServer(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
      // First launch for this platform identity. The account id is derived from
      // the issuer and subject, so two racing first launches converge on one
      // account rather than each creating one, and the durable link is recorded.
      user = storageIo.getUser(ltiUserId(issuer, sub), ltiAccountKey(issuer, sub));
      storageIo.storeLtiUserLink(issuer, sub, user.getUserId());
    }
    storageIo.setTosAccepted(user.getUserId());
    return user;
  }

  /**
   * A stable App Inventor account id for a launch, derived from the full platform
   * issuer and subject so two distinct launchers never share an account and two
   * racing first launches converge on one. The digest avoids the collisions a
   * lossy character replacement would cause on platforms whose subjects contain
   * punctuation.
   */
  @VisibleForTesting
  static String ltiUserId(String issuer, String sub) {
    try {
      return "lti-" + LtiJwt.hex(LtiJwt.sha256(issuer + "\n" + sub)).substring(0, 32);
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }

  /** The provisioned account email, in the reserved space that no real login uses. */
  @VisibleForTesting
  static String ltiAccountKey(String issuer, String sub) {
    return ltiUserId(issuer, sub) + "@lti.invalid";
  }

  /** Whether the token audience names this tool, as the audience string or one array entry. */
  @VisibleForTesting
  static boolean audienceContains(Object aud, String clientId) {
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
    resp.setContentType("text/html; charset=utf-8");
    resp.getWriter().write(LtiHtml.pageHead("Launch problem")
        + "<h1>This assignment could not open</h1><p>Please go back to your LMS and open the "
        + "activity again. If it keeps happening, ask your teacher for help.</p>"
        + LtiHtml.pageFoot());
  }

  private void failServer(HttpServletResponse resp, int status) throws IOException {
    resp.setStatus(status);
    resp.setContentType("text/html; charset=utf-8");
    resp.getWriter().write(LtiHtml.pageHead("Launch problem")
        + "<h1>This assignment could not open</h1><p>Something went wrong while opening the "
        + "activity. Please try again in a moment, or ask your teacher for help.</p>"
        + LtiHtml.pageFoot());
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
      // Not transactional across the read and the create, so two launches of a
      // new assignment fired at once can each fork once. A spike accepts the rare
      // duplicate rather than reserve the link under a transaction first.
      long projectId = forkNewProject(user, claims, forkProjectName(claims));
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
  private long forkNewProject(User user, JSONObject claims, String baseName) {
    String projectName = uniqueProjectName(user.getUserId(), baseName);
    long templateId = templateProjectId(claims);
    if (templateId > 0) {
      try {
        String ownerId = storageIo.getProjectUserId(templateId);
        if (ownerId != null) {
          long copied =
              projectService.copyProject(ownerId, templateId, projectName, user.getUserId());
          LOG.info("LTI fork: copied template " + templateId + " -> project " + copied + " ("
              + projectName + ") for " + user.getUserEmail());
          return copied;
        }
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

  private static String resourceLinkId(JSONObject claims) {
    JSONObject resourceLink = claims.optJSONObject(LTI + "resource_link");
    return (resourceLink == null) ? "" : resourceLink.optString("id", "");
  }

  /**
   * The base name, or the base with a suffix, chosen so the student does not
   * already own a project with it and so the result stays within the project
   * name length limit. Two assignments whose titles happen to match fork into
   * separate projects, each with its own grade line item, rather than colliding
   * on one.
   */
  private String uniqueProjectName(String userId, String base) {
    Set<String> taken = new HashSet<>(storageIo.getProjectNames(userId));
    for (int suffix = 1; suffix <= 99; suffix++) {
      String candidate = (suffix == 1) ? base : cappedName(base, "_" + suffix);
      if (!taken.contains(candidate)) {
        return candidate;
      }
    }
    return cappedName(base, "_" + System.currentTimeMillis());
  }

  /** The base with the tag appended, trimmed so the whole fits the name limit. */
  private static String cappedName(String base, String tag) {
    int room = MAX_PROJECT_NAME_LENGTH - tag.length();
    return (base.length() > room ? base.substring(0, room) : base) + tag;
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
   * Whether the launch carries a teaching role. Matches a core Instructor or
   * Administrator context, institution, or system role, a sub role of Instructor
   * such as TeachingAssistant, or the deprecated simple names, so a plain Learner
   * and a crafted role that merely ends in a teaching word do not count.
   */
  @VisibleForTesting
  static boolean isInstructor(JSONObject claims) {
    JSONArray roles = claims.optJSONArray(LTI + "roles");
    if (roles == null) {
      return false;
    }
    for (int i = 0; i < roles.length(); i++) {
      if (isInstructorRole(roles.optString(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether one role value grants the instructor flow, comparing the role fragment
   * exactly rather than by substring, so a value such as one ending in
   * InstructorCandidate, or one from another vocabulary, does not slip through.
   */
  private static boolean isInstructorRole(String role) {
    if (role.equals("Instructor") || role.equals("Administrator")) {
      return true;
    }
    int hash = role.indexOf('#');
    if (hash < 0 || !role.startsWith(LIS)) {
      return false;
    }
    String fragment = role.substring(hash + 1);
    return fragment.equals("Instructor") || fragment.equals("Administrator")
        || role.substring(0, hash).endsWith("/Instructor");
  }

  /**
   * Whether the token timing claims are acceptable now. Requires a numeric issued
   * at that is not in the future and a live expiry, both within the clock skew, so
   * a token with no iat or a future iat is refused rather than accepted on exp
   * alone.
   */
  @VisibleForTesting
  static boolean tokenTimeValid(JSONObject claims, long nowSeconds) {
    long iat = claims.optLong("iat", 0);
    long exp = claims.optLong("exp", 0);
    if (iat <= 0 || exp <= 0) {
      return false;
    }
    if (iat > nowSeconds + CLOCK_SKEW_SECONDS) {
      return false;
    }
    return exp >= nowSeconds - CLOCK_SKEW_SECONDS;
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
    if (suffix.isEmpty()) {
      return "AppInventorAssignment";
    }
    String fallback = "Assignment_" + suffix;
    return fallback.length() > MAX_PROJECT_NAME_LENGTH
        ? fallback.substring(0, MAX_PROJECT_NAME_LENGTH) : fallback;
  }

  /**
   * The teacher attached template project id from the launch custom claim, or
   * -1. The value is a short JWT the tool signed for itself when the teacher
   * selected a template they own, so it is verified here against the tool key
   * before it is trusted. A custom parameter set outside the Deep Linking flow,
   * for example by hand editing the activity, carries no valid signature and is
   * therefore ignored. The reference is bound to the platform issuer, not to one
   * assignment, so a party who can read a signed reference from one activity
   * could reuse it in another on the same platform.
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
   * Renders the Deep Linking picker of the teacher's own App Inventor projects, so
   * they can choose one as the assignment template. The platform return context is
   * held server side under a one time token, and the choice is posted to
   * /lti/deeplink/select, which returns the signed Deep Linking response to the LMS.
   */
  private void renderTemplatePicker(HttpServletResponse resp, JSONObject claims, User teacher)
      throws IOException {
    resp.setContentType("text/html; charset=utf-8");
    StringBuilder html = new StringBuilder(LtiHtml.pageHead("Choose a template"))
        .append("<h1 id='pick'>Choose a template for this assignment</h1>");
    List<UserProject> live = new ArrayList<>();
    for (UserProject up : storageIo.getUserProjects(teacher.getUserId(),
        storageIo.getProjects(teacher.getUserId()))) {
      if (!up.isInTrash() && up.getProjectName() != null && !up.getProjectName().isEmpty()) {
        live.add(up);
      }
    }
    if (live.isEmpty()) {
      html.append("<p>You do not have an App Inventor project to use as a template yet. Open App "
          + "Inventor, build the project you want students to start from, then add this assignment "
          + "again.</p>").append(LtiHtml.closeButton()).append(LtiHtml.pageFoot());
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
    html.append("<p>Each student who opens this assignment gets their own copy of the project "
        + "you choose here.</p><form method='post' action='/lti/deeplink/select'>");
    html.append("<input type='hidden' name='dl' value='").append(LtiHtml.escape(dlToken))
        .append("'><ul role='radiogroup' aria-labelledby='pick'>");
    boolean first = true;
    for (UserProject up : live) {
      html.append("<li role='none'><label class='opt'><input type='radio' "
          + "name='template_project_id' value='")
          .append(up.getProjectId()).append(first ? "' checked>" : "'>").append("<span>")
          .append(LtiHtml.escape(up.getProjectName())).append("</span></label></li>");
      first = false;
    }
    html.append("</ul><button class='btn' type='submit'>Use this as the assignment template"
        + "</button></form>").append(LtiHtml.pageFoot());
    resp.getWriter().write(html.toString());
  }

}
