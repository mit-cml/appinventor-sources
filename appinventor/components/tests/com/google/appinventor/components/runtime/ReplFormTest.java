// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018-2020 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;

import android.Manifest;
import com.google.appinventor.components.runtime.util.IOUtils;
import com.google.appinventor.components.runtime.util.QUtil;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Shadows;

/**
 * Tests for the ReplForm.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class ReplFormTest extends FormTest {

  private static final String TAG = "test";
  private static final int BUFSIZE = 4096;

  /**
   * Copy the test assets into the appropriate locations on disk as if they had been transferred
   * via the companion.
   */
  @Before
  public void setUp() {
    super.setUpAsRepl();
    Shadows.shadowOf(getForm()).grantPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
    Shadows.shadowOf(getForm()).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    copyAssetToReplAssets(getForm(), TARGET_FILE, TARGET_FILE);
    copyAssetToReplAssets(getForm(),
        "com.google.appinventor.components.runtime.test/" + TARGET_FILE,
        "external_comps/com.google.appinventor.components.runtime.test/assets/" + TARGET_FILE);
  }

  /**
   * Test that the form can open naked file paths.
   *
   * <p>In version 2.48, ReplForm did not follow the contract for {@link Form#getAssetPath(String)}.
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
      is = getForm().openAssetInternal(getForm().getAssetPath(TARGET_FILE));
      assertEquals("Hello, world!\n", readStream(is));
    } finally {
      IOUtils.closeQuietly(TAG, is);
    }
  }

  /// Helper functions

  /**
   * Copy an asset from the given source name to the given target name in the
   * Companion's SDK-level-dependent asset directory.
   *
   * @param source the source asset path
   * @param target the target asset path
   */
  public static void copyAssetToReplAssets(Form form, String source, String target) {
    java.io.File targetFile = new java.io.File(QUtil.getReplAssetPath(form, true) + target);
    if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
      throw new IllegalStateException("Could not configure REPL assets in setup");
    }
    try (InputStream in = form.getAssets().open(source);
         OutputStream out = new FileOutputStream(targetFile)) {
      byte[] buffer = new byte[BUFSIZE];
      int bytesRead;
      while ((bytesRead = in.read(buffer, 0, BUFSIZE)) > 0) {
        out.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not configure REPL assets in test setup", e);
    }
  }

}
