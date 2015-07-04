// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.storage;

import junit.framework.TestCase;

/**
 * Tests for StorageUtil.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class StorageUtilTest extends TestCase {
  public void testBasename() throws Exception {
    assertEquals("", StorageUtil.basename(""));
    assertEquals("bar", StorageUtil.basename("bar"));
    assertEquals("bar.scm", StorageUtil.basename("bar.scm"));
    assertEquals("bar", StorageUtil.basename("/bar"));
    assertEquals("bar.scm", StorageUtil.basename("/bar.scm"));
    assertEquals("bar", StorageUtil.basename("/foo/bar"));
    assertEquals("bar.scm", StorageUtil.basename("/foo/bar.scm"));
  }

  public void testTrimOffExtension() throws Exception {
    assertEquals("", StorageUtil.trimOffExtension(""));
    assertEquals("bar", StorageUtil.trimOffExtension("bar"));
    assertEquals("bar", StorageUtil.trimOffExtension("bar."));
    assertEquals("bar", StorageUtil.trimOffExtension("bar.scm"));
    assertEquals("/bar", StorageUtil.trimOffExtension("/bar"));
    assertEquals("/bar", StorageUtil.trimOffExtension("/bar."));
    assertEquals("/bar", StorageUtil.trimOffExtension("/bar.scm"));
    assertEquals("/foo/bar", StorageUtil.trimOffExtension("/foo/bar"));
    assertEquals("/foo/bar", StorageUtil.trimOffExtension("/foo/bar."));
    assertEquals("/foo/bar", StorageUtil.trimOffExtension("/foo/bar.scm"));
    assertEquals("/foo/bar.bar", StorageUtil.trimOffExtension("/foo/bar.bar.scm"));
  }

  public void testGetPackageName() throws Exception {
    assertEquals("", StorageUtil.getPackageName(""));
    assertEquals("", StorageUtil.getPackageName("Foo"));
    assertEquals("java.lang", StorageUtil.getPackageName("java.lang.String"));
  }

  public void testGetContentTypeForFilePath() throws Exception {
    assertEquals("image/gif", StorageUtil.getContentTypeForFilePath("kitty.gif"));
    assertEquals("image/jpeg", StorageUtil.getContentTypeForFilePath("kitty.jpg"));
    assertEquals("image/jpeg", StorageUtil.getContentTypeForFilePath("kitty.jpeg"));
    assertEquals("image/png", StorageUtil.getContentTypeForFilePath("kitty.png"));
    assertEquals("application/vnd.android.package-archive; charset=utf-8",
        StorageUtil.getContentTypeForFilePath("HelloPurr.apk"));
    assertEquals("application/zip; charset=utf-8",
        StorageUtil.getContentTypeForFilePath("HelloPurr.aia"));
    assertEquals("application/octet-stream",
        StorageUtil.getContentTypeForFilePath("android.keystore"));
    assertEquals("text/plain; charset=utf-8", StorageUtil.getContentTypeForFilePath("kitty.txt"));
  }

  public void testIsImageFile() throws Exception {
    assertTrue(StorageUtil.isImageFile("kitty.gif"));
    assertTrue(StorageUtil.isImageFile("kitty.jpg"));
    assertTrue(StorageUtil.isImageFile("kitty.jpeg"));
    assertTrue(StorageUtil.isImageFile("kitty.png"));
    assertFalse(StorageUtil.isImageFile("HelloPurr.apk"));
    assertFalse(StorageUtil.isImageFile("HelloPurr.aia"));
    assertFalse(StorageUtil.isImageFile("android.keystore"));
    assertFalse(StorageUtil.isImageFile("kitty.txt"));
  }

  public void testIsTextFile() throws Exception {
    assertFalse(StorageUtil.isTextFile("kitty.gif"));
    assertFalse(StorageUtil.isTextFile("kitty.jpg"));
    assertFalse(StorageUtil.isTextFile("kitty.jpeg"));
    assertFalse(StorageUtil.isTextFile("kitty.png"));
    assertFalse(StorageUtil.isTextFile("HelloPurr.apk"));
    assertFalse(StorageUtil.isTextFile("HelloPurr.aia"));
    assertFalse(StorageUtil.isTextFile("android.keystore"));
    assertTrue(StorageUtil.isTextFile("kitty.txt"));
  }
}
