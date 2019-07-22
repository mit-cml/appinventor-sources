// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.Manifest;
import android.os.Environment;
import com.google.appinventor.components.runtime.shadows.ShadowActivityCompat;
import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Tests for the File component.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@Config(shadows = {ShadowActivityCompat.class})
public class FileTest extends FileTestBase {
  private static final String DATA = "test data";
  protected static final String TARGET_FILE = "test.txt";
  protected File file;

  @Before
  public void setUp() {
    super.setUp();
    file = new File(getForm());
  }

  /**
   * Tests that the File component can read files when given the special file
   * path "//". In a compiled app, this reads from the app's compiled assets.
   * For the REPL, this reads from a special directory AppInventor/assets/
   * on the external storage.
   */
  @Test
  public void testFileDoubleSlash() {
    grantFilePermissions();
    file.ReadFrom("//" + TARGET_FILE);
    ShadowAsynchUtil.runAllPendingRunnables();
    runAllEvents();
    ShadowEventDispatcher.assertEventFired(file, "GotText", "Hello, world!\n");
  }

  /**
   * Tests that the File component can read files when given a relative file
   * name. In a compiled app, this reads from the app's private data directory.
   * For the REPL, this reads from a special directory AppInventor/data/
   * on the external storage.
   */
  @Test
  public void testFileRelative() {
    grantFilePermissions();
    writeTempFile(TARGET_FILE, DATA, false);
    testReadFile(TARGET_FILE, DATA);
  }

  /**
   * Tests that the File component can read files when given an absolute file
   * name. In both compiled apps and the REPL, this reads from the given file
   * from the root of the external storage.
   */
  @Test
  public void testFileAbsolute() {
    grantFilePermissions();
    writeTempFile(TARGET_FILE, DATA, true);
    testReadFile("/" + TARGET_FILE, DATA);
  }

  /**
   * Tests that the file component will report a PermissionDenied event when
   * the user denies a request for READ_EXTERNAL_STORAGE.
   */
  @Test
  public void testFilePermissionDenied() {
    denyFilePermissions();
    file.ReadFrom("/" + TARGET_FILE);
    ShadowActivityCompat.denyLastRequestedPermissions();
    runAllEvents();
    ShadowEventDispatcher.assertPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE);
  }

  /// Helper functions

  private void testReadFile(String filename, String expectedData) {
    file.ReadFrom(filename);
    ShadowAsynchUtil.runAllPendingRunnables();
    runAllEvents();
    ShadowEventDispatcher.assertEventFired(file, "GotText", expectedData);
  }

}
