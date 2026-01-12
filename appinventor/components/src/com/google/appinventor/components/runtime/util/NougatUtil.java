// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.google.appinventor.components.runtime.Form;

import java.io.File;

public final class NougatUtil {

  private static final String LOG_TAG = NougatUtil.class.getSimpleName();

  private NougatUtil() {
  }

  public static Uri getPackageUri(Form form, File apk) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      // Use file: URI on versions of Android older the Nougat
      return Uri.fromFile(apk);
    } else {
      // File provider used for SDK 24+ to get a content: URI
      String packageName = form.$context().getPackageName();
      Log.d(LOG_TAG, "packageName = " + packageName);
      return FileProvider.getUriForFile(form.$context(), packageName + ".provider", apk);
    }
  }
}
