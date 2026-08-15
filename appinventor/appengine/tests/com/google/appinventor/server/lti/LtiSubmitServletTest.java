// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.OdeAuthFilter;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import com.riq.MockHttpServletRequest;
import com.riq.MockHttpServletResponse;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Covers the submit endpoint. The refusals are driven over a request, so the guards that
 * only exist on the request path are exercised, and an accepted submission is not, because
 * that posts to the platform over the network and belongs to the end to end run instead.
 * What an attempt leaves on record is driven through the snapshot step on its own, since
 * that is the part which decides whether the platform is told anything at all.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiSubmitServletTest extends LocalDatastoreTestCase {

  private static final String HEADER = "X-AppInventor-LTI";
  private static final String LEARNER = "learner-1";
  private static final String OTHER_LEARNER = "learner-2";
  private static final String ISSUER = "http://localhost:8080";
  private static final String LINE_ITEM =
      "http://localhost:8080/mod/lti/services.php/2/lineitems/1/lineitem";
  private static final long PROJECT_ID = 5066549580791808L;

  private LtiSubmitServlet servlet;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    StorageIoInstanceHolder.getInstance().getUser(LEARNER, "learner1@example.com");
    StorageIoInstanceHolder.getInstance().getUser(OTHER_LEARNER, "learner2@example.com");
    servlet = new LtiSubmitServlet();
  }

  private static OdeAuthFilter.UserInfo session(String userId, boolean readOnly) {
    OdeAuthFilter.UserInfo info = new OdeAuthFilter.UserInfo();
    info.setUserId(userId);
    info.setReadOnly(readOnly);
    return info;
  }

  private static MockedStatic<OdeAuthFilter> signedInAs(OdeAuthFilter.UserInfo info) {
    MockedStatic<OdeAuthFilter> auth = Mockito.mockStatic(OdeAuthFilter.class);
    auth.when(() -> OdeAuthFilter.getUserInfo(Mockito.<HttpServletRequest>any()))
        .thenReturn(info);
    return auth;
  }

  private static MockHttpServletRequest submitRequest(String projectId) {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setHeader(HEADER, "1");
    if (projectId != null) {
      req.setParameter("projectId", projectId);
    }
    return req;
  }

  private static LtiGradeContext.Context contextFor(String userId) {
    return new LtiGradeContext.Context(userId, ISSUER, LINE_ITEM, "sub-" + userId);
  }

  private static long createProject(String ownerId, String name) {
    return createProject(ownerId, name, false);
  }

  /** Optionally with the Yail a build leaves behind, which a copy must not carry across. */
  private static long createProject(String ownerId, String name, boolean withYail) {
    Project project = new Project(name);
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    project.addTextFile(new TextFile("youngandroidproject/project.properties",
        "main=appinventor.ai_test." + name + ".Screen1\nname=" + name + "\n"));
    project.addTextFile(
        new TextFile("src/appinventor/ai_test/" + name + "/Screen1.scm", "#|\n$JSON\n{}\n|#"));
    if (withYail) {
      project.addTextFile(new TextFile("src/appinventor/ai_test/" + name + "/Screen1.yail",
          "(set-and-coerce-property! 'FirebaseDB1 'FirebaseToken \"secret\")"));
    }
    return StorageIoInstanceHolder.getInstance().createProject(ownerId, project, "{}");
  }

  private static boolean hasYail(java.util.List<String> fileNames) {
    for (String fileName : fileNames) {
      if (fileName.endsWith(".yail")) {
        return true;
      }
    }
    return false;
  }

  /**
   * The frozen copy is a copy into another account, so it has to leave the Yail behind. This
   * drives the submit path rather than the tidy on its own, since the tidy only protects
   * anything if the path that makes the copy actually runs it.
   */
  public void testTheFrozenCopyLeavesTheYailBehind() {
    long projectId = createProject(LEARNER, "Exercise_1", true);
    assertTrue("the learner project has Yail to begin with",
        hasYail(StorageIoInstanceHolder.getInstance().getProjectSourceFiles(LEARNER, projectId)));

    assertEquals(LtiSubmitServlet.Snapshot.STORED,
        servlet.snapshotSubmission(projectId, contextFor(LEARNER)));

    LtiSubmission.Submission stored = LtiSubmission.get(projectId);
    assertNotNull(stored);
    assertFalse("the frozen copy must not carry the Yail",
        hasYail(StorageIoInstanceHolder.getInstance()
            .getProjectSourceFiles(stored.snapshotOwnerId, stored.snapshotProjectId)));
    assertTrue("the learner keeps their own",
        hasYail(StorageIoInstanceHolder.getInstance().getProjectSourceFiles(LEARNER, projectId)));
  }

  /**
   * The platform is told about a submission only when a copy of it was stored. A submission
   * with nothing behind it would show as handed in with nothing for the teacher to open.
   */
  public void testTheLmsIsToldOnlyWhenACopyWasStored() throws Exception {
    long projectId = createProject(LEARNER, "Exercise_1");
    LtiGradeContext.put(projectId, LEARNER, ISSUER, LINE_ITEM, "platform-sub-1");
    MockHttpServletResponse resp = new MockHttpServletResponse();

    try (MockedStatic<OdeAuthFilter> auth = signedInAs(session(LEARNER, false));
        MockedStatic<LtiAgs> ags = Mockito.mockStatic(LtiAgs.class)) {
      servlet.doPost(submitRequest(Long.toString(projectId)), resp);
      ags.verify(() -> LtiAgs.postSubmission(ISSUER, LINE_ITEM, "platform-sub-1"));
    }
    assertEquals(200, resp.getStatus());
  }

  /**
   * An attempt with no copy behind it is never announced to the platform, whether or not the
   * failure could be written down. A gradebook saying handed in with nothing to open is worse
   * for the teacher than a learner being asked to try again.
   */
  public void testTheLmsIsNotToldWhenNoCopyWasStored() throws Exception {
    long projectId = createProject(LEARNER, "Exercise_1");
    LtiGradeContext.put(projectId, OTHER_LEARNER, ISSUER, LINE_ITEM, "platform-sub-2");
    MockHttpServletResponse resp = new MockHttpServletResponse();

    try (MockedStatic<OdeAuthFilter> auth = signedInAs(session(OTHER_LEARNER, false));
        MockedStatic<LtiAgs> ags = Mockito.mockStatic(LtiAgs.class)) {
      servlet.doPost(submitRequest(Long.toString(projectId)), resp);
      ags.verifyNoInteractions();
    }
    assertTrue("the status must be outside 2xx",
        resp.getStatus() < 200 || resp.getStatus() >= 300);
  }


  /** A submission that copies cleanly becomes the copy the review opens. */
  public void testASuccessfulSnapshotIsStored() {
    long projectId = createProject(LEARNER, "Exercise_1");

    assertEquals(LtiSubmitServlet.Snapshot.STORED,
        servlet.snapshotSubmission(projectId, contextFor(LEARNER)));
    LtiSubmission.Submission stored = LtiSubmission.get(projectId);
    assertNotNull("the attempt must be on record", stored);
    assertFalse("the copy must be a different project", stored.snapshotProjectId == projectId);
  }

  /**
   * A failed attempt leaves the copy the platform already knows about exactly where it is.
   *
   * <p>The platform was told about that copy and a teacher has to be able to open it. This
   * attempt was never announced, so removing the earlier copy would leave the gradebook saying
   * handed in with nothing behind it, which is the state this whole path exists to avoid.
   */
  public void testAFailedAttemptLeavesTheAnnouncedCopyAlone() {
    long projectId = createProject(LEARNER, "Exercise_1");
    assertEquals(LtiSubmitServlet.Snapshot.STORED,
        servlet.snapshotSubmission(projectId, contextFor(LEARNER)));
    LtiSubmission.Submission announced = LtiSubmission.get(projectId);
    assertNotNull(announced);

    // The context names a learner the project does not belong to, which is how a copy refuses.
    assertEquals(LtiSubmitServlet.Snapshot.FAILED,
        servlet.snapshotSubmission(projectId, contextFor(OTHER_LEARNER)));

    LtiSubmission.Submission after = LtiSubmission.get(projectId);
    assertNotNull("the announced copy must still be there to open", after);
    assertEquals(announced.snapshotProjectId, after.snapshotProjectId);
  }

  /**
   * A copy that is made but does not end up on record is not a stored submission.
   *
   * <p>The store keeps whichever record carries the later time, so a later record with no copy
   * behind it quietly wins over this attempt. Nothing writes such a record any more, but data
   * written before failures stopped being recorded can still hold one, and a review meeting it
   * opens nothing. Telling the platform this attempt arrived would put Submitted in the
   * gradebook against nothing to grade, so the attempt reads back what is on record first.
   */
  public void testACopySupersededByALaterEmptyRecordIsNotStored() {
    long projectId = createProject(LEARNER, "Exercise_1");
    StorageIoInstanceHolder.getInstance().storeLtiSubmission(projectId, LEARNER, 0, "",
        new Date(System.currentTimeMillis() + 60000));
    assertNull("a later attempt is already on record with no copy", LtiSubmission.get(projectId));

    assertEquals(LtiSubmitServlet.Snapshot.FAILED,
        servlet.snapshotSubmission(projectId, contextFor(LEARNER)));

    assertNull("nothing with a copy behind it may be on record", LtiSubmission.get(projectId));
  }








  /**
   * A page on another site can post a form but cannot set a custom header, and it
   * cannot add one without a preflight this server does not grant, so the header
   * is what keeps another site from submitting on the learner's behalf.
   */
  public void testPostWithoutTheCustomHeaderIsRefused() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setParameter("projectId", Long.toString(PROJECT_ID));
    MockHttpServletResponse resp = new MockHttpServletResponse();

    servlet.doPost(req, resp);

    assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatus());
    assertTrue(resp.getContentAsString().contains("Project menu"));
  }

  /** Without a session there is no learner to submit for. */
  public void testPostWithoutASessionIsRefused() throws Exception {
    MockHttpServletResponse resp = new MockHttpServletResponse();

    servlet.doPost(submitRequest(Long.toString(PROJECT_ID)), resp);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, resp.getStatus());
  }

  /** A read-only session, such as one viewing another account, may not act as the learner. */
  public void testReadOnlySessionIsRefused() throws Exception {
    MockHttpServletResponse resp = new MockHttpServletResponse();
    try (MockedStatic<OdeAuthFilter> auth = signedInAs(session(LEARNER, true))) {
      servlet.doPost(submitRequest(Long.toString(PROJECT_ID)), resp);
    }
    assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatus());
    assertTrue(resp.getContentAsString().contains("read-only"));
  }

  /** A request with no project, or a project id that is not a number, is refused. */
  public void testPostWithoutAProjectIsRefused() throws Exception {
    MockHttpServletResponse missing = new MockHttpServletResponse();
    MockHttpServletResponse malformed = new MockHttpServletResponse();
    try (MockedStatic<OdeAuthFilter> auth = signedInAs(session(LEARNER, false))) {
      servlet.doPost(submitRequest(null), missing);
      servlet.doPost(submitRequest("not-a-number"), malformed);
    }
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, missing.getStatus());
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, malformed.getStatus());
  }

  /**
   * A plain App Inventor project has no grade line item, so there is nothing to
   * submit to. The status has to be outside 2xx, because the menu item reports
   * success on 2xx alone.
   */
  public void testProjectWithoutAGradeLineItemIsRefused() throws Exception {
    MockHttpServletResponse resp = new MockHttpServletResponse();
    try (MockedStatic<OdeAuthFilter> auth = signedInAs(session(LEARNER, false))) {
      servlet.doPost(submitRequest(Long.toString(PROJECT_ID)), resp);
    }
    assertEquals(HttpServletResponse.SC_CONFLICT, resp.getStatus());
    assertTrue(resp.getStatus() < 200 || resp.getStatus() >= 300);
  }

  /** One learner may not submit another learner's assignment project. */
  public void testAnotherLearnerProjectIsRefused() throws Exception {
    LtiGradeContext.put(PROJECT_ID, OTHER_LEARNER, ISSUER, LINE_ITEM, "platform-sub-2");
    MockHttpServletResponse resp = new MockHttpServletResponse();
    try (MockedStatic<OdeAuthFilter> auth = signedInAs(session(LEARNER, false))) {
      servlet.doPost(submitRequest(Long.toString(PROJECT_ID)), resp);
    }
    assertEquals(HttpServletResponse.SC_CONFLICT, resp.getStatus());
  }

  /** Opening the endpoint in a browser explains where the real action lives. */
  public void testGetPointsBackToTheProjectMenu() throws Exception {
    MockHttpServletResponse resp = new MockHttpServletResponse();

    servlet.doGet(new MockHttpServletRequest(), resp);

    String page = resp.getContentAsString();
    assertTrue(page.contains("Submit to LMS"));
    assertTrue(page.contains("Project"));
  }
}
