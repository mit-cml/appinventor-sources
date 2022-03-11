// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import android.Manifest;
import android.app.AlertDialog;
import com.google.appinventor.components.runtime.PermissionResultHandler;
import com.google.appinventor.components.runtime.WebViewer;
import android.app.Activity;
import android.content.DialogInterface;

import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.EditText;

/**
 * Helper methods for calling methods added in Eclair (2.0, API level 5)
 *
 * @author Ryan Bis
 *
 */
public class EclairUtil {

  private EclairUtil() {
  }

  /**
   * Calls {@link Activity#overridePendingTransition(int, int)}. This is used
   * to set a different animation type for screen transition animations.
   *
   * @param activity - The activity handling the animation
   * @param enterAnim - The enter animation type
   * @param exitAnim - The exit animation type
   */
  public static void overridePendingTransitions(Activity activity, int enterAnim, int exitAnim) {
    activity.overridePendingTransition(enterAnim, exitAnim);
  }

  /**
   * Setup Dialog Box to request location permission from end-user for the Javascript
   * location (navigator.geolocation.getCurrentLocation()) API.
   *
   * @param webview - The WebView component running the Javascript engine that needs permission
   * @param activity - Its containing activity used for placing the dialog box
   */

  public static void setupWebViewGeoLoc(final WebViewer caller, final WebView webview, 
      final Activity activity) {
    webview.getSettings().setGeolocationDatabasePath(activity.getFilesDir().getAbsolutePath());
    webview.getSettings().setDatabaseEnabled(true);
    webview.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
          Callback callback) {
          final Callback theCallback = callback;
          final String theOrigin = origin;
          final WebViewer theCaller = caller;

          if (!theCaller.container.$form()
              .isDeniedPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Permission already granted
            theCallback.invoke(theOrigin, true, true);
            return;
          }

          if (!theCaller.UsesLocation()) {
            return;
          }

          theCaller.container.$form().askPermission(Manifest.permission.ACCESS_FINE_LOCATION,
              new PermissionResultHandler() {
                @Override
                public void HandlePermissionResponse(String permission, boolean granted) {
                  if (granted) {
                    // Permission granted
                    if (!theCaller.PromptforPermission()) { // Don't prompt, assume permission
                      theCallback.invoke(theOrigin, true, true);
                      return;
                    }
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle("Permission Request");
                    alertDialog.setCancelable(false);
                    String tempOrigin = theOrigin;
                    if (theOrigin.equals("file://")) {
                      tempOrigin = "This Application";
                    }

                    alertDialog.setMessage(tempOrigin + " would like to access your location.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Allow", 
                        new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                            theCallback.invoke(theOrigin, true, true);
                          }
                        });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Refuse", 
                        new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                            theCallback.invoke(theOrigin, false, true);
                          }
                        });
                    alertDialog.show();
                  } else {
                    // Permission for location not granted
                    theCallback.invoke(theOrigin, false, true);
                    theCaller.container.$form().dispatchPermissionDeniedEvent(theCaller, "HomeUrl",
                        Manifest.permission.ACCESS_FINE_LOCATION);
                  }
                }
              });
        }
      });
  }

  /**
   * Clear Stored Location permissions. When the geolocation API is used in
   * the WebViewer, the end user is prompted on a per URL basis for whether
   * or not permission should be granted to access their location. This
   * function clears this information for all locations.
   *
   * As the permissions interface is not available on phones older then
   * Eclair, this function is a no-op on older phones.
   */
  public static void clearWebViewGeoLoc() {
    GeolocationPermissions permissions = GeolocationPermissions.getInstance();
    permissions.clearAll();
  }

  public static String getInstallerPackageName(String pname, Activity form) {
    return form.getPackageManager().getInstallerPackageName(pname);
  }

  /**
   * Disable suggestions on EditText widgets. This was added to
   * support SDK levels where suggestions crashed apps without the
   * appropriate Android Support library compiled into the app.
   *
   * @param textview EditText widget to have its suggestion feature
   * disabled.
   */
  public static void disableSuggestions(EditText textview) {
    textview.setInputType(textview.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
  }

}
