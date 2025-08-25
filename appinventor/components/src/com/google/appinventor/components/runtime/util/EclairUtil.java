// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.Manifest;
import android.webkit.PermissionRequest;

import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.WebViewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.text.InputType;
import android.webkit.GeolocationPermissions;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

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

  public static void setupWebViewGeoLoc(final WebViewer caller, WebView webview, final Activity activity) {
    webview.getSettings().setGeolocationDatabasePath(activity.getFilesDir().getAbsolutePath());
    webview.getSettings().setDatabaseEnabled(true);
    webview.setWebChromeClient(new WebChromeClient() {
        private boolean askedPermission = false;

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
          Callback callback) {
          if (!caller.PromptforPermission()) { // Don't prompt, assume permission
            callback.invoke(origin, true, true);
            return;
          }
          showPermissionDialog(activity, origin, "location", callback);
        }

        @Override
        public void onPermissionRequest(final PermissionRequest request) {
          List<String> permissionsNeeded = new ArrayList<>();
          for (String resource : request.getResources()) {
            if (resource.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
              if (!caller.UsesCamera()) {
                // If the app requests video or audio capture, we will not allow it.
                request.deny();
                return;
              }
              permissionsNeeded.add(Manifest.permission.CAMERA);
            } else if (resource.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
              if (!caller.UsesMicrophone()) {
                // If the app requests video or audio capture, we will not allow it.
                request.deny();
                return;
              }
              permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
            } else {
              // We don't support any other resources.
              request.deny();
              return;
            }
          }
          // At this point, we have a list of permissions that the app is requesting.
          if (activity instanceof Form) {
            Form form = (Form) activity;
            form.askPermission(new BulkPermissionRequest(caller, "WebView Permission Request", permissionsNeeded.toArray(new String[0])) {
              @Override
              public void onGranted() {
                if (caller.PromptforPermission() && !askedPermission) {
                  showPermissionDialog(activity, request.getOrigin().getHost(), "camera and/or microphone", new Callback() {
                    @Override
                    public void invoke(String origin, boolean allow, boolean remember) {
                      if (allow) {
                        askedPermission = true;
                        request.grant(request.getResources());
                      } else {
                        request.deny();
                      }
                    }
                  });
                } else {
                  request.grant(request.getResources());
                }
              }

              @Override
              public void onDenied(String[] permissions) {
                request.deny();
                super.onDenied(permissions);
              }
            });
          } else {
            // If the activity is not a Form, we deny the request.
            request.deny();
          }
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

  private static void showPermissionDialog(final Activity activity, String origin, String item,
      final Callback callback) {
    final String theOrigin = origin;
    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
    alertDialog.setTitle("Permission Request");
    if (origin.equals("file://"))
      origin = "This Application";
    alertDialog.setMessage(origin + " would like to access your " + item + ".");
    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Allow",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            callback.invoke(theOrigin, true, true);
          }
        });
    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Refuse",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            callback.invoke(theOrigin, false, true);
          }
        });
    alertDialog.show();
  }
}
