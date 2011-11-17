// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.server.project.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;

/**
 * Tests for {@link TempFiles}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class TempFilesTest extends TestCase {
  public void testCreateAndDeleteTempFile() throws Exception {
    byte[] data = "Hello world.".getBytes(Charsets.UTF_8);
    File tmpFile = TempFiles.createTempFile(data);
    assertTrue(tmpFile.exists());
    byte[] read = Files.toByteArray(tmpFile);
    assertTrue(Arrays.equals(data, read));

    TempFiles.deleteTempFile(tmpFile);
    assertFalse(tmpFile.exists());
  }
}
