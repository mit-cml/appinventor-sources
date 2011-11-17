// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.common.utils;

import junit.framework.TestCase;

/**
 * Checks functionality of filename helper functions.
 *
 * @see FilenameUtils
 *
 */
public class FilenameUtilsTest extends TestCase {

  /**
   * Tests file extension extraction.
   *
   * @see FilenameUtils#getExtension(String)
   */
  public void testGetExtension() {
    assertEquals("", FilenameUtils.getExtension(""));
    assertEquals("", FilenameUtils.getExtension("foo"));
    assertEquals("java", FilenameUtils.getExtension("foo.java"));
    assertEquals("cpp", FilenameUtils.getExtension("foo.java.cpp"));
    assertEquals("", FilenameUtils.getExtension("/a.b.c/foo"));
    assertEquals("cpp", FilenameUtils.getExtension("/a.b.c/foo.java.cpp"));

    try {
      FilenameUtils.getExtension(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }
}
