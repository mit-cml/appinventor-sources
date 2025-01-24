package com.google.appinventor.components.runtime.util;

import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class PermissionUtil {
  private static final String LOG_TAG = "PermissionUtil";

  public static String[] getPackagePermission(ContextWrapper activity) {
    try {
      PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(),
          PackageManager.GET_PERMISSIONS);
      return packageInfo.requestedPermissions;
    } catch (Exception e) {
      Log.e(LOG_TAG, "Exception while attempting to learn permissions.", e);
      return new String[] {};
    }
  }
}
