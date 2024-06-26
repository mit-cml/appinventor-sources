// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.assets;

import android.Manifest;
import android.os.Build;

import com.google.appinventor.components.runtime.BaseAiComponent;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.PermissionResultHandler;

/**
 * Helper class to handle the transition from targetSdk 7 to targetSdk 26 and the change in permission
 * model that Google implemented in Android 6.0 Marshmallow.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class SDK26Helper {

  public static boolean shouldAskForPermission(Form form) {
    return form.getApplicationInfo().targetSdkVersion >= 23 &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
  }

  public static void askForPermission(final BaseAiComponent baseAiComponent, final Runnable next) {
    baseAiComponent.getForm().askPermission(Manifest.permission.CAMERA, new PermissionResultHandler() {
      @Override
      public void HandlePermissionResponse(String permission, boolean granted) {
        if (granted) {
          next.run();
        } else {
          baseAiComponent.getForm().PermissionDenied(baseAiComponent, "WebViewer", permission);
        }
      }
    });
  }
}