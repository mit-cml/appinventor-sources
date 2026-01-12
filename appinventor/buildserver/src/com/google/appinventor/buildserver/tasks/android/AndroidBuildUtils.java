// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.android;

import com.google.appinventor.buildserver.context.AndroidCompilerContext;
import java.util.Set;

public class AndroidBuildUtils {
  static int computeMinSdk(AndroidCompilerContext context) {
    int minSdk = Integer.parseInt(context.getProject().getMinSdk());
    if (!context.isForCompanion()) {
      for (Set<String> minSdks : context.getComponentInfo().getMinSdksNeeded().values()) {
        for (String sdk : minSdks) {
          int sdkInt = Integer.parseInt(sdk);
          if (sdkInt > minSdk) {
            minSdk = sdkInt;
          }
        }
      }
    }
    return minSdk;
  }
}
