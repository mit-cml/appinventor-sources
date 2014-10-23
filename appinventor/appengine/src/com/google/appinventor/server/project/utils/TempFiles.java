// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.utils;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * Helper functions for handling temporary files.
 *
 */
public final class TempFiles {

  // Temp file directory
  private final static File tempRoot = Files.createTempDir();

  private TempFiles() {  // COV_NF_LINE
  }  // COV_NF_LINE

  /**
   * Creates a temporary file for the given data.
   *
   * @param data  data to write into the temporary file
   * @return  file descriptor for temporary file
   * @throws IOException
   */
  public static File createTempFile(byte[] data) throws IOException {
    File tmpFile = File.createTempFile("ode", null, tempRoot);
    Files.write(data, tmpFile);
    return tmpFile;
  }

  /**
   * Deletes the given temporary file.
   *
   * @param tempFile  temporary file to delete
   */
  public static void deleteTempFile(File tempFile) {
    tempFile.delete();
  }
}
