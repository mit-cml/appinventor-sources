// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.common.utils;

import junit.framework.TestCase;

/**
 * Checks functionality of string helper functions.
 *
 * @see StringUtils
 *
 */
public class StringUtilsTest extends TestCase {

  /**
   * Tests quoting a string.
   *
   * @see StringUtils#quote(String)
   */
  public void testQuote() {
    assertEquals("\"\"", StringUtils.quote(""));
    assertEquals("\"Hello\"", StringUtils.quote("Hello"));

    try {
      StringUtils.unquote(null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  /**
   * Tests unquoting a string.
   *
   * @see StringUtils#unquote(String)
   */
  public void testUnquote() {
    assertEquals("Hello", StringUtils.unquote("\"Hello\""));
    assertEquals("", StringUtils.unquote("\"\""));

    try {
      StringUtils.unquote(null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }

    try {
      StringUtils.unquote("");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }

    try {
      StringUtils.unquote("\"");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }

    try {
      StringUtils.unquote("Hello");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }

    try {
      StringUtils.unquote("\"Hello");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }

    try {
      StringUtils.unquote("Hello\"");
      fail();
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  /**
   * Tests escaping a string for use in HTML.
   *
   * @see StringUtils#escape(String)
   */
  public void testEscape() {
    assertEquals(null, StringUtils.escape(null));
    assertEquals("&amp;&lt;&gt;&quot;<br>", StringUtils.escape("&<>\"\n"));
  }

  /**
   * Tests whether a string is contained in an array.
   *
   * @see StringUtils#contains(String[], String)
   */
  public void testContains() {
    assertTrue(StringUtils.contains(new String[] { "a", "" }, ""));
    assertTrue(StringUtils.contains(new String[] { "a", "" }, "a"));
    assertFalse(StringUtils.contains(new String[] { "a", "b" }, ""));
    assertFalse(StringUtils.contains(new String[] { }, ""));

    try {
      StringUtils.contains(null, "");
      fail();
    } catch (NullPointerException expected) {
      // expected
    }

    try {
      StringUtils.contains(new String[] { "" }, null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  /**
   * Tests joining strings.
   *
   * @see StringUtils#join(String, String[])
   */
  public void testJoin() {
    assertEquals("", StringUtils.join("", new String[0]));
    assertEquals("", StringUtils.join("+", new String[0]));

    assertEquals("foo", StringUtils.join("", new String[] { "foo" }));
    assertEquals("foo", StringUtils.join("+", new String[] { "foo" }));

    assertEquals("foofaafee", StringUtils.join("", new String[] { "foo", "faa", "fee" }));
    assertEquals("foo+faa+fee", StringUtils.join("+", new String[] { "foo", "faa", "fee" }));

    try {
      StringUtils.join(null, new String[0]);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }

    try {
      StringUtils.join("", (String[]) null);
      fail();
    } catch (NullPointerException expected) {
      // expected
    }
  }

  public void testUserToPackageName() {
    assertEquals("appinventor.ai_joeuser",
        StringUtils.userToPackageName("joeuser@gmail.com"));
    assertEquals("appinventor.ai_JoeUser",
        StringUtils.userToPackageName("JoeUser@gmail.com"));
    assertEquals("appinventor.ai_deb_achwall",
        StringUtils.userToPackageName("deb.achwall@gmail.com"));
    assertEquals("appinventor.ai_123456",
        StringUtils.userToPackageName("123456@gmail.com"));
    assertEquals("appinventor.ai_kenny",
        StringUtils.userToPackageName("kenny@some-domain.com"));
    assertEquals("appinventor.ai_JeanMichel_Cousteau",
        StringUtils.userToPackageName("Jean-Michel.Cousteau@gmail.com"));
    assertEquals("appinventor.ai_Keha",
        StringUtils.userToPackageName("Ke$ha@gmail.com"));
  }

  public void testReplaceLastOccurrence() {
    // Test situation similar to Save As for project named HelloPurr.
    assertEquals("appinventor.ai_joeuser.HelloPurr_copy.Screen1",
        StringUtils.replaceLastOccurrence("appinventor.ai_joeuser.HelloPurr.Screen1",
        ".HelloPurr.", ".HelloPurr_copy."));
    assertEquals("src/appinventor/ai_joeuser/HelloPurr_copy/Screen1.blk",
        StringUtils.replaceLastOccurrence("src/appinventor/ai_joeuser/HelloPurr/Screen1.blk",
        "/HelloPurr/", "/HelloPurr_copy/"));

    // Test situation similar to Checkpoint for project named HelloPurr.
    assertEquals("appinventor.ai_joeuser.HelloPurr_checkpoint1.Screen1",
        StringUtils.replaceLastOccurrence("appinventor.ai_joeuser.HelloPurr.Screen1",
        ".HelloPurr.", ".HelloPurr_checkpoint1."));
    assertEquals("src/appinventor/ai_joeuser/HelloPurr_checkpoint1/Screen1.blk",
        StringUtils.replaceLastOccurrence("src/appinventor/ai_joeuser/HelloPurr/Screen1.blk",
        "/HelloPurr/", "/HelloPurr_checkpoint1/"));

    // Test situation similar to Save As for project named appinventor.
    assertEquals("appinventor.ai_joeuser.appinventor_copy.Screen1",
        StringUtils.replaceLastOccurrence("appinventor.ai_joeuser.appinventor.Screen1",
        ".appinventor.", ".appinventor_copy."));
    assertEquals("src/appinventor/ai_joeuser/appinventor_copy/Screen1.blk",
        StringUtils.replaceLastOccurrence("src/appinventor/ai_joeuser/appinventor/Screen1.blk",
        "/appinventor/", "/appinventor_copy/"));

    // Test situation similar to Checkpoint for project named appinventor.
    assertEquals("appinventor.ai_joeuser.appinventor_checkpoint1.Screen1",
        StringUtils.replaceLastOccurrence("appinventor.ai_joeuser.appinventor.Screen1",
        ".appinventor.", ".appinventor_checkpoint1."));
    assertEquals("src/appinventor/ai_joeuser/appinventor_checkpoint1/Screen1.blk",
        StringUtils.replaceLastOccurrence("src/appinventor/ai_joeuser/appinventor/Screen1.blk",
        "/appinventor/", "/appinventor_checkpoint1/"));

    // Test situation similar to Save As for project named ai_joeuser.
    assertEquals("appinventor.ai_joeuser.ai_joeuser_copy.Screen1",
        StringUtils.replaceLastOccurrence("appinventor.ai_joeuser.ai_joeuser.Screen1",
        ".ai_joeuser.", ".ai_joeuser_copy."));
    assertEquals("src/appinventor/ai_joeuser/ai_joeuser_copy/Screen1.blk",
        StringUtils.replaceLastOccurrence("src/appinventor/ai_joeuser/ai_joeuser/Screen1.blk",
        "/ai_joeuser/", "/ai_joeuser_copy/"));

    // Test situation similar to Checkpoint for project named ai_joeuser.
    assertEquals("appinventor.ai_joeuser.ai_joeuser_checkpoint1.Screen1",
        StringUtils.replaceLastOccurrence("appinventor.ai_joeuser.ai_joeuser.Screen1",
        ".ai_joeuser.", ".ai_joeuser_checkpoint1."));
    assertEquals("src/appinventor/ai_joeuser/ai_joeuser_checkpoint1/Screen1.blk",
        StringUtils.replaceLastOccurrence("src/appinventor/ai_joeuser/ai_joeuser/Screen1.blk",
        "/ai_joeuser/", "/ai_joeuser_checkpoint1/"));

    // Test situation where string is empty.
    assertEquals("",
        StringUtils.replaceLastOccurrence("", "5", "$"));

    // Test situation where target is empty.
    assertEquals("0123456789",
        StringUtils.replaceLastOccurrence("0123456789", "", "$"));

    // Test situation where replacement is empty.
    assertEquals("012346789",
        StringUtils.replaceLastOccurrence("0123456789", "5", ""));

    // Test situation where target doesn't exist.
    assertEquals("0123456789",
        StringUtils.replaceLastOccurrence("0123456789", "A", "$"));
  }

  public void testValidFilenameChars() {
    assertTrue(StringUtils.VALID_FILENAME_CHARS.matchesAllOf("myfilename.zip"));
    assertTrue(StringUtils.VALID_FILENAME_CHARS.matchesAllOf("MyFilename.ZiP"));
    assertTrue(StringUtils.VALID_FILENAME_CHARS.matchesAllOf("myfilename2.zip"));
    assertTrue(StringUtils.VALID_FILENAME_CHARS.matchesAllOf("my3filena-me.zip"));
    assertTrue(StringUtils.VALID_FILENAME_CHARS.matchesNoneOf("!@#$%^&*()\"';:<>,/?"));
  }

  public void testNormalizeForFilename() {
    assertEquals("MyAppInventorProject1",
        StringUtils.normalizeForFilename("My App Inventor Project 1"));
    assertEquals("testproject4",
      StringUtils.normalizeForFilename("1 2 3 test project 4"));
  }
}
