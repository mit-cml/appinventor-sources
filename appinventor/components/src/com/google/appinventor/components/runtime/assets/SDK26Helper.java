// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package edu.mit.appinventor.ai.personalimageclassifier;

import android.Manifest;
import android.os.Build;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.PermissionResultHandler;

/**
 * Helper class to handle the transition from targetSdk 7 to targetSdk 26 and the change in permission
 * model that Google implemented in Android 6.0 Marshmallow.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
class SDK26Helper {

  static boolean shouldAskForPermission(Form form) {
    return form.getApplicationInfo().targetSdkVersion >= 23 &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
  }

  static void askForPermission(final PersonalImageClassifier personalImageClassifier, final Runnable next) {
    personalImageClassifier.getForm().askPermission(Manifest.permission.CAMERA, new PermissionResultHandler() {
      @Override
      public void HandlePermissionResponse(String permission, boolean granted) {
        if (granted) {
          next.run();
        } else {
          personalImageClassifier.getForm().PermissionDenied(personalImageClassifier, "WebViewer", permission);
        }
      }
    });
  }
}