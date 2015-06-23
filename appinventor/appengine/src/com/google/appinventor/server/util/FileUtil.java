// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import java.io.File;
import java.io.IOException;

/**
 * Helper methods for file access.
 *
 */
public final class FileUtil {
  private FileUtil() {
  }

  /**
   * Creates all requested but not existing directories.
   *
   * @param dir  directories to create
   * @throws IOException  if the creation of any one directory failed
   */
  public static void mkdirs(File dir) throws IOException {
    if (!dir.mkdirs() && (!dir.exists() || !dir.isDirectory())) {
      throw new IOException("creating directories: " + dir.getAbsolutePath());
    }
  }
}
