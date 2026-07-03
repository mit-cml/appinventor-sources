// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

/**
 * Tests the durable assignment to project links that make relaunches
 * idempotent across activity renames and server restarts.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiResourceLinksTest extends LocalDatastoreTestCase {

  private static final String USER_ID = "1";
  private static final String KEY_A =
      LtiResourceLinks.key("http://localhost:8080", "1", "42");
  private static final String KEY_B =
      LtiResourceLinks.key("http://localhost:8080", "1", "43");

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    StorageIoInstanceHolder.getInstance().getUser(USER_ID, "student@example.com");
  }

  /** An unknown assignment has no linked project. */
  public void testUnknownKeyReturnsMinusOne() {
    assertEquals(-1, LtiResourceLinks.get(USER_ID, KEY_A));
  }

  /** A stored link comes back, and other assignments stay independent. */
  public void testPutThenGetRoundTrip() {
    LtiResourceLinks.put(USER_ID, KEY_A, 5066549580791808L);
    assertEquals(5066549580791808L, LtiResourceLinks.get(USER_ID, KEY_A));
    assertEquals(-1, LtiResourceLinks.get(USER_ID, KEY_B));
  }

  /** Two assignments map to two projects side by side. */
  public void testTwoAssignmentsCoexist() {
    LtiResourceLinks.put(USER_ID, KEY_A, 11L);
    LtiResourceLinks.put(USER_ID, KEY_B, 22L);
    assertEquals(11L, LtiResourceLinks.get(USER_ID, KEY_A));
    assertEquals(22L, LtiResourceLinks.get(USER_ID, KEY_B));
  }

  /** Relinking an assignment replaces the earlier project id. */
  public void testPutReplacesEarlierLink() {
    LtiResourceLinks.put(USER_ID, KEY_A, 11L);
    LtiResourceLinks.put(USER_ID, KEY_A, 33L);
    assertEquals(33L, LtiResourceLinks.get(USER_ID, KEY_A));
  }

  /** The key separates platform, deployment, and resource link. */
  public void testKeyShape() {
    assertEquals("iss|dep|rl", LtiResourceLinks.key("iss", "dep", "rl"));
  }
}
