// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

/**
 * Tests the durable assignment to project links that make relaunches idempotent
 * across activity renames, server restarts, and server instances.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiResourceLinksTest extends LocalDatastoreTestCase {

  private static final String USER_ID = "1";
  private static final String ISSUER = "http://localhost:8080";
  private static final String DEPLOYMENT = "1";
  private static final String LINK_A = "42";
  private static final String LINK_B = "43";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    StorageIoInstanceHolder.getInstance().getUser(USER_ID, "student@example.com");
  }

  /** An unknown assignment has no linked project. */
  public void testUnknownAssignmentReturnsZero() {
    assertEquals(0, LtiResourceLinks.get(USER_ID, ISSUER, DEPLOYMENT, LINK_A));
  }

  /** A stored link comes back, and other assignments stay independent. */
  public void testPutThenGetRoundTrip() {
    LtiResourceLinks.put(USER_ID, ISSUER, DEPLOYMENT, LINK_A, 5066549580791808L);
    assertEquals(5066549580791808L, LtiResourceLinks.get(USER_ID, ISSUER, DEPLOYMENT, LINK_A));
    assertEquals(0, LtiResourceLinks.get(USER_ID, ISSUER, DEPLOYMENT, LINK_B));
  }

  /** Two assignments map to two projects side by side. */
  public void testTwoAssignmentsCoexist() {
    LtiResourceLinks.put(USER_ID, ISSUER, DEPLOYMENT, LINK_A, 11L);
    LtiResourceLinks.put(USER_ID, ISSUER, DEPLOYMENT, LINK_B, 22L);
    assertEquals(11L, LtiResourceLinks.get(USER_ID, ISSUER, DEPLOYMENT, LINK_A));
    assertEquals(22L, LtiResourceLinks.get(USER_ID, ISSUER, DEPLOYMENT, LINK_B));
  }

  /** Relinking an assignment replaces the earlier project id. */
  public void testPutReplacesEarlierLink() {
    LtiResourceLinks.put(USER_ID, ISSUER, DEPLOYMENT, LINK_A, 11L);
    LtiResourceLinks.put(USER_ID, ISSUER, DEPLOYMENT, LINK_A, 33L);
    assertEquals(33L, LtiResourceLinks.get(USER_ID, ISSUER, DEPLOYMENT, LINK_A));
  }

  /** Another user's identical assignment is a separate link. */
  public void testDifferentUserIsSeparate() {
    StorageIoInstanceHolder.getInstance().getUser("2", "other@example.com");
    LtiResourceLinks.put(USER_ID, ISSUER, DEPLOYMENT, LINK_A, 11L);
    assertEquals(0, LtiResourceLinks.get("2", ISSUER, DEPLOYMENT, LINK_A));
  }
}
