// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

/**
 * Tests that the grade passback context survives in storage, since a student
 * may submit long after the launch, possibly after a server restart.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiGradeContextTest extends LocalDatastoreTestCase {

  private static final String USER_ID = "1";
  private static final String LINE_ITEM =
      "http://localhost:8080/mod/lti/services.php/2/lineitems/1/lineitem";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    StorageIoInstanceHolder.getInstance().getUser(USER_ID, "student@example.com");
  }

  /** Without a stored context there is nothing to submit to. */
  public void testEmptyInitially() {
    assertNull(LtiGradeContext.get(USER_ID));
  }

  /** A stored context comes back with the same line item and platform user. */
  public void testPutThenGetRoundTrip() {
    LtiGradeContext.put(USER_ID, LINE_ITEM, "platform-sub-9");
    LtiGradeContext.Context ctx = LtiGradeContext.get(USER_ID);
    assertNotNull(ctx);
    assertEquals(LINE_ITEM, ctx.lineItemUrl);
    assertEquals("platform-sub-9", ctx.ltiUserSub);
  }

  /** A later launch replaces the earlier passback target. */
  public void testPutReplacesEarlierContext() {
    LtiGradeContext.put(USER_ID, LINE_ITEM, "sub-1");
    LtiGradeContext.put(USER_ID, LINE_ITEM + "?type=2", "sub-2");
    LtiGradeContext.Context ctx = LtiGradeContext.get(USER_ID);
    assertEquals(LINE_ITEM + "?type=2", ctx.lineItemUrl);
    assertEquals("sub-2", ctx.ltiUserSub);
  }

  /** A launch without a grade service must not clobber an earlier target. */
  public void testEmptyLineItemIsIgnored() {
    LtiGradeContext.put(USER_ID, LINE_ITEM, "sub-1");
    LtiGradeContext.put(USER_ID, "", "sub-2");
    LtiGradeContext.put(USER_ID, null, "sub-3");
    LtiGradeContext.Context ctx = LtiGradeContext.get(USER_ID);
    assertEquals(LINE_ITEM, ctx.lineItemUrl);
  }
}
