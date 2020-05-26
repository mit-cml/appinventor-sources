// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.WebViewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.net.Uri;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.PermissionRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods for calling methods added in Eclair (2.0, API level 5)
 *
 * @author Ryan Bis
 *
 */
public class EclairUtil {
  /*
   * Maps WebView resource names to more user-friendly names
   * Used in setupWebViewPermissions
   * TODO: Might be worthwhile to move this somewhere else.
  */
  private static final Map<String, String> RESOURCE_MAP;

  static {
    RESOURCE_MAP = new HashMap<String, String>();
    RESOURCE_MAP.put("android.webkit.resource.AUDIO_CAPTURE", "microphone");
    RESOURCE_MAP.put("android.webkit.resource.MIDI_SYSEX", "MIDI device");
    RESOURCE_MAP.put("android.webkit.resource.PROTECTED_MEDIA_ID", "EME APIs");
    RESOURCE_MAP.put("android.webkit.resource.VIDEO_CAPTURE", "camera");
  }


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
   * Setup Dialog Box to request geo-location permission from end-user for the Javascript
   * location (navigator.geolocation.getCurrentLocation()) API or other permissions (e.g. camera
   * or microphone).
   *
   * @param webview - The WebView component running the Javascript engine that needs permission
   * @param activity - Its containing activity used for placing the dialog box
   */

  public static void setupWebViewPermissions(final WebViewer caller, WebView webview, final Activity activity) {
    webview.getSettings().setGeolocationDatabasePath(activity.getFilesDir().getAbsolutePath());
    webview.getSettings().setDatabaseEnabled(true);

    // TODO: Might be wortwhile changing this anonymous interface to it's own class
    webview.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
          // No prompt for permission needed; Grant resource
          if (!caller.PromptforPermission()) {
            request.grant(request.getResources());
            return;
          }

          // Get origin of permission request
          String origin = request.getOrigin().toString();

          // Grant resource on accept
          DialogInterface.OnClickListener acceptListener = new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                request.grant(request.getResources());
              }
          };

          // Deny request on reject
          DialogInterface.OnClickListener refuseListener = new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                request.deny();
              }
          };

          String accessString = generateAccessString(request);

          createPermissionAlertDialog(origin, accessString, acceptListener, refuseListener);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
          Callback callback) {
          final Callback theCallback = callback;
          final String theOrigin = origin;
          
          if (!caller.PromptforPermission()) { // Don't prompt, assume permission
            callback.invoke(origin, true, true);
            return;
          }

          DialogInterface.OnClickListener acceptListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              theCallback.invoke(theOrigin, true, true);
            }
          };

          DialogInterface.OnClickListener refuseListener = new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                theCallback.invoke(theOrigin, false, true);
              }
          };

          createPermissionAlertDialog(origin, "location", acceptListener, refuseListener);
        }

        /**
         * Generates a comma-separated string of permissions (resources) that the specified request
         * wants to access in an easy-to-read format.
         * 
         * @param request  Permission request object
         * @return CSV string of permissions
         */
        private String generateAccessString(final PermissionRequest request) {
          StringBuilder accessStringBuilder = new StringBuilder();
          String[] resources = request.getResources();
          String sep = "";

          for (String res : resources) {
            accessStringBuilder.append(sep);

            String resourceName = res;

            // Map to more user-friendly name, if there is a mapping
            if (RESOURCE_MAP.containsKey(resourceName)) {
              resourceName = RESOURCE_MAP.get(resourceName);
            }

            accessStringBuilder.append(resourceName);

            // Separate remaining resources by comma
            sep = ", ";
          }

          return accessStringBuilder.toString();
        }

        /**
         * Creates a permission alert dialog that perform the corresponding listener actions
         * on accept or reject.
         * @param origin  Origin of the permission request (URL string)
         * @param access  String indicating what the permission wants to access
         * @param acceptListener OnClickListener for accepting
         * @param refuseListener OnClickListener for refusing
         */
        private void createPermissionAlertDialog(String origin, String access, DialogInterface.OnClickListener acceptListener,
          DialogInterface.OnClickListener refuseListener) {
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

            alertDialog.setTitle("Permission Request");

            if (origin.equals("file://"))
              origin = "This Application";

            alertDialog.setMessage(origin + " would like to access your " + access + ".");
            
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Allow", acceptListener);
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Refuse", refuseListener);
            
            alertDialog.show();
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
