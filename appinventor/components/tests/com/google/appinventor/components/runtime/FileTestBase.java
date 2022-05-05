// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.os.Environment;

import com.google.appinventor.components.runtime.shadows.ShadowActivityCompat;
import com.google.appinventor.components.runtime.util.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@Config(shadows = {ShadowActivityCompat.class})
public abstract class FileTestBase extends RobolectricTestBase {  /// Helper functions
  protected static final String TAG = FileTest.class.getSimpleName();

  /**
   * Helper function to grant read/write permissions to the app.
   */
  public void grantFilePermissions() {
    Shadows.shadowOf(getForm()).grantPermissions(READ_EXTERNAL_STORAGE);
    Shadows.shadowOf(getForm()).grantPermissions(WRITE_EXTERNAL_STORAGE);
  }

  /**
   * Helper function to deny read/write permissions to the app.
   */
  public void denyFilePermissions() {
    Shadows.shadowOf(getForm()).denyPermissions(READ_EXTERNAL_STORAGE);
    Shadows.shadowOf(getForm()).denyPermissions(WRITE_EXTERNAL_STORAGE);
  }

  /**
   * Write a temporary file to the file of the given {@code name} with the given
   * {@code content}.
   *
   * @param name the name of the file to write
   * @param content the content of the file
   * @param external true if the file should be written to external storage,
   *                 otherwise false.
   * @return the absolute path of the file
   */
  @SuppressWarnings("deprecation")
  public String writeTempFile(String name, String content, boolean external) {
    String target;
    if (external) {
      target = Environment.getExternalStorageDirectory().getAbsolutePath();
    } else if (getForm().isRepl()) {
      target = Environment.getExternalStorageDirectory().getAbsolutePath()
          + "/AppInventor/data";
    } else {
      target = getForm().getFilesDir().getAbsolutePath();
    }
    target += "/" + name;
    FileOutputStream out = null;
    try {
      java.io.File targetFile = new java.io.File(target);
      targetFile.deleteOnExit();
      if (!targetFile.getParentFile().exists()) {
        if (!targetFile.getParentFile().mkdirs()) {
          throw new IOException();
        }
      }
      out = new FileOutputStream(target);
      out.write(content.getBytes(StandardCharsets.UTF_8));
      return targetFile.getAbsolutePath();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to prepare test", e);
    } finally {
      IOUtils.closeQuietly(TAG, out);
    }
  }
}
