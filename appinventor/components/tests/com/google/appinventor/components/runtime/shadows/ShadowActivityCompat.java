// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.shadows;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * A shadow implementation of ActivityCompat that allows one to
 * grant/deny permissions requested when the app calls
 * {@link ActivityCompat#requestPermission(Activity,String,String)}.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@Implements(ActivityCompat.class)
public class ShadowActivityCompat {

  private static Activity activity;
  private static String[] permissions;
  private static int requestCode;

  @Implementation
  public static void requestPermissions(final Activity activity, final String[] permissions, final int requestCode) {
    ShadowActivityCompat.activity = activity;
    ShadowActivityCompat.permissions = permissions;
    ShadowActivityCompat.requestCode = requestCode;
  }

  /**
   * Grants the last set of permissions requested by the app.
   */
  public static void grantLastRequestedPermissions() {
    int[] result = new int[permissions.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = PackageManager.PERMISSION_GRANTED;
    }
    activity.onRequestPermissionsResult(requestCode, permissions, result);
  }

  /**
   * Denies the last set of permissions requested by the app.
   */
  public static void denyLastRequestedPermissions() {
    int[] result = new int[permissions.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = PackageManager.PERMISSION_DENIED;
    }
    activity.onRequestPermissionsResult(requestCode, permissions, result);
  }
}
