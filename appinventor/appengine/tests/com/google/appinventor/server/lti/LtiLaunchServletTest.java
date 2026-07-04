// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import junit.framework.TestCase;

import org.json.JSONObject;

/**
 * Tests the launch servlet's pure helpers, in particular how the student's
 * project is named from the launch claims. The name must be a valid App
 * Inventor project name and stable per resource link, so a relaunch finds the
 * same project again.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiLaunchServletTest extends TestCase {

  private static final String RESOURCE_LINK =
      "https://purl.imsglobal.org/spec/lti/claim/resource_link";

  private static JSONObject claimsWith(String title, String id) {
    JSONObject link = new JSONObject();
    if (title != null) {
      link.put("title", title);
    }
    if (id != null) {
      link.put("id", id);
    }
    return new JSONObject().put(RESOURCE_LINK, link);
  }

  /** A plain activity title becomes a readable project name. */
  public void testTitleBecomesProjectName() {
    assertEquals("Exercise_2", LtiLaunchServlet.forkProjectName(claimsWith("Exercise 2", "8")));
  }

  /** Punctuation and repeated separators collapse to single underscores. */
  public void testPunctuationIsCollapsed() {
    assertEquals("Build_a_Quiz_App_v2",
        LtiLaunchServlet.forkProjectName(claimsWith("Build a Quiz App! (v2)", "9")));
  }

  /** A name may not start with a digit, so a prefix is added. */
  public void testLeadingDigitGetsPrefix() {
    assertEquals("Project_3D_maze",
        LtiLaunchServlet.forkProjectName(claimsWith("3D maze", "4")));
  }

  /** A very long title is truncated without a trailing underscore. */
  public void testLongTitleIsTruncated() {
    String name = LtiLaunchServlet.forkProjectName(claimsWith(
        "An Extremely Long Assignment Title That Goes On And On Well Past The Limit", "5"));
    assertTrue("was: " + name, name.length() <= 40);
    assertFalse("was: " + name, name.endsWith("_"));
  }

  /** Without a usable title the name falls back to the resource link id. */
  public void testEmptyTitleFallsBackToResourceLinkId() {
    assertEquals("Assignment_7", LtiLaunchServlet.forkProjectName(claimsWith("", "7")));
    assertEquals("Assignment_7", LtiLaunchServlet.forkProjectName(claimsWith("!!!", "7")));
  }

  /** A long resource link id in the fallback is still capped to a valid length. */
  public void testLongResourceLinkIdFallbackIsCapped() {
    String name = LtiLaunchServlet.forkProjectName(
        claimsWith("", "0123456789012345678901234567890123456789"));
    assertTrue("was: " + name, name.startsWith("Assignment_"));
    assertTrue("was: " + name, name.length() <= 40);
  }

  /** Without any resource link claim a fixed default is used. */
  public void testMissingResourceLinkUsesDefault() {
    assertEquals("AppInventorAssignment",
        LtiLaunchServlet.forkProjectName(new JSONObject()));
  }

  /** An LTI account lands in the reserved space and never looks like a real email. */
  public void testAccountKeyIsNamespaced() {
    String key = LtiLaunchServlet.ltiAccountKey("http://moodle.example.org", "42");
    assertTrue("was: " + key, key.startsWith("lti-"));
    assertTrue("was: " + key, key.endsWith("@lti.invalid"));
  }

  /** The same subject on two platforms maps to two different accounts. */
  public void testAccountKeySeparatesPlatforms() {
    assertFalse(LtiLaunchServlet.ltiAccountKey("http://a.example.org", "1")
        .equals(LtiLaunchServlet.ltiAccountKey("http://b.example.org", "1")));
  }

  /** Subjects that differ only in punctuation get different accounts. */
  public void testAccountKeyDistinguishesPunctuation() {
    assertFalse(LtiLaunchServlet.ltiAccountKey("http://x.org", "a-b")
        .equals(LtiLaunchServlet.ltiAccountKey("http://x.org", "a.b")));
  }

  /** A missing subject still produces a stable, valid key. */
  public void testAccountKeyHandlesMissingSubject() {
    String key = LtiLaunchServlet.ltiAccountKey("http://moodle.example.org", "");
    assertTrue("was: " + key, key.startsWith("lti-"));
    assertTrue("was: " + key, key.endsWith("@lti.invalid"));
  }

  /** One launcher always resolves to one account, so racing first launches converge. */
  public void testAccountKeyIsStableForOneLauncher() {
    assertEquals(LtiLaunchServlet.ltiAccountKey("http://moodle.example.org", "42"),
        LtiLaunchServlet.ltiAccountKey("http://moodle.example.org", "42"));
  }
}
