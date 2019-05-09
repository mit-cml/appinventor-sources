// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.Manifest;
import com.google.appinventor.components.runtime.util.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the ReplForm.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class ReplFormTest extends FormTest {

  private static final String TAG = "test";
  private static final int BUFSIZE = 4096;

  @Before
  public void setUp() {
    super.setUpAsRepl();
    Shadows.shadowOf(getForm()).grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
    Shadows.shadowOf(getForm()).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    copyAssetToReplAssets(TARGET_FILE, TARGET_FILE);
    copyAssetToReplAssets("com.google.appinventor.components.runtime.test/" + TARGET_FILE,
        "external_comps/com.google.appinventor.components.runtime.test/assets/" + TARGET_FILE);
  }

  /**
   * Test that the form can open naked file paths.
   *
   * In version 2.48, ReplForm did not follow the contract for {@link Form#getAssetPath(String)}.
   * It returned a string containing a file path, not a file URI. This would result in developers
   * getting an error "URI is not absolute" when live testing. We changed the ReplForm
   * implementation to behave correctly by returning a string starting with file:///, but we also
   * test here that we can handle non-URI versions in case someone ends up overriding the method in
   * a subclass (somehow...).
   */
  @Test
  public void testOpenAssetInternal() throws IOException {
    InputStream is = null;
    try {
      is = getForm().openAssetInternal(ReplForm.REPL_ASSET_DIR + TARGET_FILE);
      assertEquals("Hello, world!\n", readStream(is));
    } finally {
      IOUtils.closeQuietly(TAG, is);
    }
  }

  /// Helper functions

  /**
   * Copy an asset from the given source name to the given target name in the
   * /sdcard/AppInventor/assets directory.
   *
   * @param source the source asset path
   * @param target the target asset path
   */
  public static void copyAssetToReplAssets(String source, String target) {
    InputStream in = null;
    OutputStream out = null;
    try {
      in = ShadowApplication.getInstance().getApplicationContext().getAssets().open(source);
      java.io.File targetFile = new java.io.File(ReplForm.REPL_ASSET_DIR + target);
      if (!targetFile.getParentFile().exists()) {
        if (!targetFile.getParentFile().mkdirs()) {
          throw new IllegalStateException("Could not configure REPL assets in setup");
        }
      }
      out = new FileOutputStream(targetFile);
      byte[] buffer = new byte[BUFSIZE];
      int bytesRead;
      while ((bytesRead = in.read(buffer, 0, BUFSIZE)) > 0) {
        out.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not configure REPL assets in test setup", e);
    } finally {
      IOUtils.closeQuietly(TAG, in);
      IOUtils.closeQuietly(TAG, out);
    }
  }

}
