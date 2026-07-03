// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

/**
 * Tests that the grade passback context survives in storage keyed by project, so
 * a student may submit long after the launch and each assignment posts to its
 * own line item.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiGradeContextTest extends LocalDatastoreTestCase {

  private static final long PROJECT_ID = 5066549580791808L;
  private static final String USER_ID = "1";
  private static final String ISSUER = "http://localhost:8080";
  private static final String LINE_ITEM =
      "http://localhost:8080/mod/lti/services.php/2/lineitems/1/lineitem";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    StorageIoInstanceHolder.getInstance().getUser(USER_ID, "student@example.com");
  }

  /** Without a stored context there is nothing to submit to. */
  public void testEmptyInitially() {
    assertNull(LtiGradeContext.get(PROJECT_ID));
  }

  /** A stored context comes back with its owner, issuer, line item, and platform user. */
  public void testPutThenGetRoundTrip() {
    LtiGradeContext.put(PROJECT_ID, USER_ID, ISSUER, LINE_ITEM, "platform-sub-9");
    LtiGradeContext.Context ctx = LtiGradeContext.get(PROJECT_ID);
    assertNotNull(ctx);
    assertEquals(USER_ID, ctx.userId);
    assertEquals(ISSUER, ctx.issuer);
    assertEquals(LINE_ITEM, ctx.lineItemUrl);
    assertEquals("platform-sub-9", ctx.ltiUserSub);
  }

  /** Two assignments keep separate contexts, so a submission targets the right one. */
  public void testProjectsAreSeparate() {
    LtiGradeContext.put(PROJECT_ID, USER_ID, ISSUER, LINE_ITEM + "/a", "sub-a");
    LtiGradeContext.put(PROJECT_ID + 1, USER_ID, ISSUER, LINE_ITEM + "/b", "sub-b");
    assertEquals(LINE_ITEM + "/a", LtiGradeContext.get(PROJECT_ID).lineItemUrl);
    assertEquals(LINE_ITEM + "/b", LtiGradeContext.get(PROJECT_ID + 1).lineItemUrl);
  }

  /** A launch without a grade service must not clobber an earlier target. */
  public void testEmptyLineItemIsIgnored() {
    LtiGradeContext.put(PROJECT_ID, USER_ID, ISSUER, LINE_ITEM, "sub-1");
    LtiGradeContext.put(PROJECT_ID, USER_ID, ISSUER, "", "sub-2");
    LtiGradeContext.put(PROJECT_ID, USER_ID, ISSUER, null, "sub-3");
    assertEquals(LINE_ITEM, LtiGradeContext.get(PROJECT_ID).lineItemUrl);
  }
}
