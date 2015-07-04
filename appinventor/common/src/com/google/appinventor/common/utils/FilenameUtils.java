// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.common.utils;

import com.google.common.base.Preconditions;

/**
 * Helper class for working with filenames.
 *
 */
public class FilenameUtils {

  private FilenameUtils() {
  }

  /**
   * Returns the extension of a filename.
   *
   * @param filename  filename to get extension of
   * @return  extension of filename
   */
  public static String getExtension(String filename) {
    Preconditions.checkNotNull(filename);

    // Separate filename from rest of pathname
    filename = filename.substring(filename.lastIndexOf('/') + 1);

    // Extract extension
    int index = filename.lastIndexOf('.');
    return index == -1 ? "" : filename.substring(index + 1);
  }
}
