// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Build;
import android.os.Environment;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.Form;

public class RUtil {
  /**
   * Checks whether the given {@code path} requires asking the user for file permissions.
   *
   * @param form the form making the request
   * @param path the path to test
   * @param fileScope the scope in which to test
   * @return true if the file will need read permissions and false otherwise
   */
  public static boolean needsFilePermission(Form form, String path, FileScope fileScope) {
    if (fileScope == null) {
      if (path.startsWith("//")) {
        return false;
      }
      if (!path.startsWith("/") && !path.startsWith("file:")) {
        return false;
      }
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
        String fpath = path;
        if (path.startsWith("file:")) {
          fpath = path.substring(5);
        }
        return fpath.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath());
      }
      return FileUtil.isExternalStorageUri(form, path)
        && !FileUtil.isAppSpecificExternalUri(form, path);
    }
    switch (fileScope) {
      case App:
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
      case Asset:
        return form.isRepl() && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
      case Shared:
        return true;
      default:
        return false;
    }
  }
}
