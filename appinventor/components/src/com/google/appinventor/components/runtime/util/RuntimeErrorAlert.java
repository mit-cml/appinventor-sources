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

/**
 * Shows a runtime error alert with a single button.  Pressing the button will
 * kill the application.  This is called by the Yail runtime mechanism
 * Components should throw a YailRuntimeErrorException, which will call this.
 * @author halabelson@google.com (Hal Abelson)
 */
public final class RuntimeErrorAlert {
  public static void alert(final Object context,
      final String message, final String title,final String buttonText) {
    Log.i("RuntimeErrorAlert", "in alert");
    AlertDialog alertDialog = new AlertDialog.Builder((Context) context).create();
    alertDialog.setTitle(title);
    alertDialog.setMessage(message);
    alertDialog.setButton(buttonText, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        ((Activity) context).finish();
      }});
    if (message == null) {
      // Avoid passing null to Log.e, which would cause a NullPointerException.
      Log.e(RuntimeErrorAlert.class.getName(), "No error message available");
    } else {
      Log.e(RuntimeErrorAlert.class.getName(), message);
    }
    alertDialog.show();
  }
}
