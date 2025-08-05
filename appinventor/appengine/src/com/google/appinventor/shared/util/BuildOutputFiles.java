// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.util;

import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

public final class BuildOutputFiles {
  private BuildOutputFiles() {
  }

  /**
   * Returns whether the given file is an output file or not.
   * @param fileName the file name to check
   * @return true if it's an output file
   */
  public static boolean isOutputFile(final String fileName) {
    return fileName.endsWith(".apk") || fileName.endsWith(".aab");
  }

  /**
   * Returns the target name
   * @return Android
   */
  public static String getTargetName() {
    // This method is created for when iOS builds are available, to be able to change it to
    //   Apple or iOS or something similar, based on an input param.
    return YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
  }

}
