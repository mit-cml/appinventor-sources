// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.test.TestExtension;
import com.google.appinventor.components.runtime.util.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the Form component.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class FormTest extends RobolectricTestBase {

  private static final int BUFSIZE = 4096;
  static final String TARGET_FILE = "test.txt";

  /**
   * Tests the ability of the Form to open an asset.
   *
   * @throws IOException if the asset cannot be found.
   */
  @Test
  public void testOpenAsset() throws IOException {
    InputStream is = null;
    try {
      is = getForm().openAsset(TARGET_FILE);
      assertEquals("Hello, world!\n", readStream(is));
    } finally {
      IOUtils.closeQuietly("test", is);
    }
  }

  /**
   * Tests the ability of the Form to open an asset associated with an
   * extension.
   *
   * @throws IOException if the asset cannot be found.
   */
  @Test
  public void testOpenAssetExtension() throws IOException {
    InputStream is = null;
    try {
      is = getForm().openAssetForExtension(new TestExtension(getForm()), TARGET_FILE);
      assertEquals("Sample extension asset\n", readStream(is));
    } finally {
      IOUtils.closeQuietly("test", is);
    }
  }

  /// Helper functions

  /**
   * Read the contents of a stream as a string.
   *
   * @param is the input stream to read
   * @return the contents of the input stream as a string
   * @throws IOException if the file cannot be read
   */
  public static String readStream(InputStream is) throws IOException {
    byte[] data = new byte[BUFSIZE];
    int read;
    StringBuilder sb = new StringBuilder();
    while ((read = is.read(data, 0, BUFSIZE)) > 0) {
      sb.append(new String(data, 0, read, Charset.forName("UTF-8")));
    }
    return sb.toString();
  }
}
