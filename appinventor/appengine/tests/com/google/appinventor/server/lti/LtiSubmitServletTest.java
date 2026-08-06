// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.OdeAuthFilter;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

import com.riq.MockHttpServletRequest;
import com.riq.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Drives the submit endpoint over a request rather than through its helpers, so
 * the guards that only exist on the request path are covered. Every case here is
 * a refusal, because accepting a submission posts to the platform over the
 * network and belongs to the end to end run instead.
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
