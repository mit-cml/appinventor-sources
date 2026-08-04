// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.OdeAuthFilter;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

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
 * cannot cause a submission on the student's behalf. A GET returns a short page
 * that points the student back to the Project menu.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiSubmitServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LtiSubmitServlet.class.getName());
  private static final String REQUEST_HEADER = "X-AppInventor-LTI";

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();
  private final transient YoungAndroidProjectService projectService =
      new YoungAndroidProjectService(storageIo);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html; charset=utf-8");
    resp.getWriter().write(LtiHtml.pageHead("Submit to LMS")
        + "<h1>Submit to LMS</h1>"
        + "<p>To submit your work, go back to App Inventor and choose "
        + "<strong>Submit to LMS</strong> from the <strong>Project</strong> menu.</p>"
        + LtiHtml.pageFoot());
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
    if (userInfo.getReadOnly()) {
      // A read-only session (for example an admin viewing another account) must not act
      // as the student and push a grade line item to the LMS on their behalf.
      resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
      resp.getWriter().println("This session is read-only and cannot submit to the LMS.");
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
      try {
        snapshotSubmission(projectId, ctx);
      } catch (Exception e) {
        // The submission still reaches the LMS, so the learner is not left thinking their
        // work was never handed in. The review then has nothing to open for this attempt,
        // because the learner's own project is deliberately never offered as a substitute,
        // and snapshotSubmission marked the record unavailable before rethrowing, so an
        // older copy cannot pass for this one.
        LOG.log(Level.WARNING,
            "LTI submission snapshot failed for source project " + projectId, e);
      }
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

  /** Copies the submitted project into an account no interactive login can reach. */
  private void snapshotSubmission(long sourceProjectId, LtiGradeContext.Context ctx)
      throws Exception {
    Date submittedAt = new Date();
    try {
      String sourceOwnerId = storageIo.getProjectUserId(sourceProjectId);
      if (!ctx.userId.equals(sourceOwnerId)) {
        throw new SecurityException("LTI snapshot source owner mismatch");
      }
      String snapshotOwnerId = snapshotOwnerId(sourceOwnerId, sourceProjectId);
      User snapshotOwner =
          storageIo.getUser(snapshotOwnerId, snapshotOwnerId + "@lti.invalid");
      if (!snapshotOwnerId.equals(snapshotOwner.getUserId())) {
        throw new SecurityException("LTI snapshot owner account mismatch");
      }
      long snapshotProjectId = projectService.copyProject(sourceOwnerId, sourceProjectId,
          snapshotProjectName(sourceProjectId, submittedAt.getTime()), snapshotOwnerId);
      if (snapshotProjectId <= 0 || snapshotProjectId == sourceProjectId) {
        throw new IllegalStateException("LTI snapshot copy returned an invalid project");
      }
      if (!snapshotOwnerId.equals(storageIo.getProjectUserId(snapshotProjectId))) {
        throw new SecurityException("LTI snapshot project owner mismatch");
      }
      removeLaunchMarker(snapshotOwnerId, snapshotProjectId);
      LtiSubmission.put(sourceProjectId, sourceOwnerId, snapshotProjectId,
          snapshotOwnerId, submittedAt);
      LOG.info("LTI submission snapshot copied source project " + sourceProjectId
          + " to project " + snapshotProjectId);
    } catch (Exception e) {
      try {
        // A failed resubmission must not leave an older artifact looking current.
        LtiSubmission.markUnavailable(sourceProjectId, ctx.userId, submittedAt);
      } catch (Exception recordFailure) {
        e.addSuppressed(recordFailure);
      }
      throw e;
    }
  }

  /**
   * The snapshot is never a submit-capable learner project, even though project
   * copying carries settings across.
   */
  @VisibleForTesting
  void removeLaunchMarker(String snapshotOwnerId, long snapshotProjectId) {
    try {
      String raw = storageIo.loadProjectSettings(snapshotOwnerId, snapshotProjectId);
      if (raw == null || raw.trim().isEmpty()) {
        return;
      }
      JSONObject settings = new JSONObject(raw);
      JSONObject youngAndroid =
          settings.optJSONObject(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS);
      if (youngAndroid != null
          && youngAndroid.has(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED)) {
        youngAndroid.remove(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED);
        storageIo.storeProjectSettings(snapshotOwnerId, snapshotProjectId,
            settings.toString());
      }
    } catch (Exception e) {
      // The read-only session and submit endpoint still enforce immutability;
      // a settings cleanup failure must not discard an otherwise valid copy.
      LOG.log(Level.WARNING,
          "Could not remove the LTI launch marker from snapshot " + snapshotProjectId, e);
    }
  }

  /** A stable shadow account for all snapshots of one learner project. */
  private static String snapshotOwnerId(String sourceOwnerId, long sourceProjectId) {
    try {
      String material = sourceOwnerId.length() + ":" + sourceOwnerId + sourceProjectId;
      return "lti-snapshot-" + LtiJwt.hex(LtiJwt.sha256(material)).substring(0, 32);
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }

  /** A legal, collision-resistant project name kept below the forty-character limit. */
  private static String snapshotProjectName(long sourceProjectId, long submittedAt) {
    String unique = UUID.randomUUID().toString().substring(0, 6);
    return "Snapshot_" + Long.toString(sourceProjectId, 36) + "_"
        + Long.toString(submittedAt, 36) + "_" + unique;
  }
}
