// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import junit.framework.TestCase;

import org.json.JSONArray;
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
  private static final String ROLES = "https://purl.imsglobal.org/spec/lti/claim/roles";

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

  /** A course or institution instructor, administrator, or teaching assistant may add work. */
  public void testInstructorRolesAreRecognized() {
    assertTrue(LtiLaunchServlet.isInstructor(rolesClaim(
        "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor")));
    assertTrue(LtiLaunchServlet.isInstructor(rolesClaim(
        "http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator")));
    assertTrue(LtiLaunchServlet.isInstructor(rolesClaim(
        "http://purl.imsglobal.org/vocab/lis/v2/membership/Instructor#TeachingAssistant")));
    assertTrue(LtiLaunchServlet.isInstructor(rolesClaim(
        "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator")));
    assertTrue(LtiLaunchServlet.isInstructor(rolesClaim(
        "http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator")));
    assertTrue(LtiLaunchServlet.isInstructor(rolesClaim("Instructor")));
  }

  /** A learner, a missing roles claim, and an empty roles array are not an instructor. */
  public void testLearnerAndMissingRolesAreNotInstructor() {
    assertFalse(LtiLaunchServlet.isInstructor(rolesClaim(
        "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner")));
    assertFalse(LtiLaunchServlet.isInstructor(new JSONObject()));
    assertFalse(LtiLaunchServlet.isInstructor(rolesClaim()));
  }

  /** A crafted role, a foreign vocabulary, and a non array roles claim are refused. */
  public void testCraftedInstructorRolesAreRejected() {
    assertFalse(LtiLaunchServlet.isInstructor(rolesClaim(
        "http://purl.imsglobal.org/vocab/lis/v2/membership#InstructorCandidate")));
    assertFalse(LtiLaunchServlet.isInstructor(rolesClaim("https://evil.example/roles#Instructor")));
    // A fragment that is a learner, under a path that merely ends in Instructor, is not a teacher.
    assertFalse(LtiLaunchServlet.isInstructor(rolesClaim(
        "http://purl.imsglobal.org/vocab/lis/v2/foo/Instructor#Learner")));
    // A principal Instructor fragment under an unknown vocabulary does not count either.
    assertFalse(LtiLaunchServlet.isInstructor(rolesClaim(
        "http://purl.imsglobal.org/vocab/lis/v2/foo#Instructor")));
    assertFalse(LtiLaunchServlet.isInstructor(
        new JSONObject().put(ROLES, "Instructor")));
  }

  /** The audience check accepts a matching string or array entry and refuses anything else. */
  public void testAudienceContains() {
    assertTrue(LtiLaunchServlet.audienceContains("client-a", "client-a"));
    assertTrue(LtiLaunchServlet.audienceContains(
        new JSONArray().put("other").put("client-a"), "client-a"));
    assertFalse(LtiLaunchServlet.audienceContains("other", "client-a"));
    assertFalse(LtiLaunchServlet.audienceContains(new JSONArray().put("other"), "client-a"));
    assertFalse(LtiLaunchServlet.audienceContains("client-a", ""));
    assertFalse(LtiLaunchServlet.audienceContains(null, "client-a"));
  }

  /** The timing check requires a numeric iat that is not in the future and a live exp. */
  public void testTokenTimeValid() {
    long now = 1_000_000L;
    assertTrue(LtiLaunchServlet.tokenTimeValid(timing(now, now + 3600), now));
    assertFalse("missing iat", LtiLaunchServlet.tokenTimeValid(timing(0, now + 3600), now));
    assertFalse("missing exp", LtiLaunchServlet.tokenTimeValid(timing(now, 0), now));
    assertFalse("future iat", LtiLaunchServlet.tokenTimeValid(timing(now + 1000, now + 3600), now));
    assertFalse("expired", LtiLaunchServlet.tokenTimeValid(timing(now - 3600, now - 1000), now));
    assertTrue("within skew", LtiLaunchServlet.tokenTimeValid(timing(now + 30, now - 30), now));
    // The clock skew is 60 seconds, so check the exact edges of that window.
    assertTrue("iat at edge", LtiLaunchServlet.tokenTimeValid(timing(now + 60, now + 3600), now));
    assertFalse("iat too new", LtiLaunchServlet.tokenTimeValid(timing(now + 61, now + 3600), now));
    assertTrue("exp at edge", LtiLaunchServlet.tokenTimeValid(timing(now, now - 60), now));
    assertFalse("exp too old", LtiLaunchServlet.tokenTimeValid(timing(now, now - 61), now));
  }

  private static JSONObject timing(long iat, long exp) {
    JSONObject c = new JSONObject();
    if (iat != 0) {
      c.put("iat", iat);
    }
    if (exp != 0) {
      c.put("exp", exp);
    }
    return c;
  }

  private static JSONObject rolesClaim(String... roles) {
    JSONArray array = new JSONArray();
    for (String role : roles) {
      array.put(role);
    }
    return new JSONObject().put(ROLES, array);
  }
}
