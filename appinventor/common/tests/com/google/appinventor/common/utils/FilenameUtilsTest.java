// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
