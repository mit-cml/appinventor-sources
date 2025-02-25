// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

/**
 * Shows a runtime error alert with a single button.  Pressing the button will
 * kill the application.  This is called by the Yail runtime mechanism
 * Components should throw a YailRuntimeErrorException, which will call this.
 * @author halabelson@google.com (Hal Abelson)
 */
public final class RuntimeErrorAlert {

  private static final String TAG = "RuntimeErrorAlert";


  public static void alert(final Object context, final String message, final String title, final String buttonText) {
    alert(context, /* only alert dialog */ false, message, title, buttonText);
  }

  public static void alert(final Object context, boolean toast, String message, String title, String buttonText) {
    Log.e(TAG, "alert(" + message + ", " + title + ", " + buttonText);
    if (message == null) {
      // do not pass null, or it will result in a crash
      message = title + " <No error message>";
    }

    if (toast) {
      Toast.makeText((Context) context, message, Toast.LENGTH_LONG).show();
      RetValManager.sendError(message);
    } else {
      AlertDialog alertDialog = new AlertDialog.Builder((Context) context).create();
      alertDialog.setTitle(title);
      alertDialog.setMessage(message);
      alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, buttonText, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          ((Activity) context).finish();
        }
      });
      alertDialog.show();
    }

  }
}
