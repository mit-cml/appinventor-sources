// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All Rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.WebViewer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;

/**
 * Utility class for handling Android API features related to WebView and geolocation.
 */
public class EclairUtil {

  private EclairUtil() {
    // Private constructor to prevent instantiation
  }

  /**
   * Overrides the pending transition animation for an activity.
   *
   * @param activity The activity handling the animation.
   * @param enterAnim The enter animation.
   * @param exitAnim The exit animation.
   */
  public static void overridePendingTransitions(Activity activity, int enterAnim, int exitAnim) {
    if (activity != null) {
      activity.overridePendingTransition(enterAnim, exitAnim);
    }
  }

  /**
   * Configures a WebView to request location permission from users when needed.
   *
   * @param caller The WebViewer instance.
   * @param webview The WebView component requesting geolocation.
   * @param activity The activity hosting the WebView.
   */
  public static void setupWebViewGeoLoc(final WebViewer caller, WebView webview, final Activity activity) {
    if (webview == null || activity == null) return;

    webview.getSettings().setGeolocationDatabasePath(activity.getFilesDir().getAbsolutePath());
    webview.getSettings().setDatabaseEnabled(true);

    webview.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        if (!caller.PromptforPermission()) { 
          callback.invoke(origin, true, true);
          return;
        }

        String displayOrigin = origin.equals("file://") ? "This Application" : origin;

        new AlertDialog.Builder(activity)
          .setTitle("Permission Request")
          .setMessage(displayOrigin + " would like to access your location.")
          .setPositiveButton("Allow", (dialog, which) -> callback.invoke(origin, true, true))
          .setNegativeButton("Refuse", (dialog, which) -> callback.invoke(origin, false, true))
          .show();
      }
    });
  }

  /**
   * Clears all stored geolocation permissions for the WebView.
   */
  public static void clearWebViewGeoLoc() {
    GeolocationPermissions.getInstance().clearAll();
  }

  /**
   * Retrieves the installer package name for a given package.
   *
   * @param packageName The package name.
   * @param activity The activity context.
   * @return The installer package name, or null if not found.
   */
  public static String getInstallerPackageName(String packageName, Activity activity) {
    if (activity == null || packageName == null) return null;
    return activity.getPackageManager().getInstallerPackageName(packageName);
  }

  /**
   * Disables text suggestions on an EditText field.
   *
   * @param editText The EditText widget.
   */
  public static void disableSuggestions(EditText editText) {
    if (editText != null) {
      editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }
  }
}
