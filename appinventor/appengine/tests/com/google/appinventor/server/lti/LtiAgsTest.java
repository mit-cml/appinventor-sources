// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import junit.framework.TestCase;

/**
 * Tests that grade passback keeps the line item query string, so a score reaches
 * the right endpoint on a platform whose line item URL carries one, as Canvas and
 * Moodle line item URLs do.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiAgsTest extends TestCase {

  /** A line item with no query gets /scores appended to its path. */
  public void testScoresUrlWithoutQuery() {
    assertEquals("https://lms.example.org/lineitems/1/lineitem/scores",
        LtiAgs.scoresUrl("https://lms.example.org/lineitems/1/lineitem"));
  }

  /** A line item with a query keeps the query after /scores rather than inside it. */
  public void testScoresUrlPreservesQuery() {
    assertEquals("https://lms.example.org/lineitems/1/lineitem/scores?type_id=1",
        LtiAgs.scoresUrl("https://lms.example.org/lineitems/1/lineitem?type_id=1"));
  }
}
