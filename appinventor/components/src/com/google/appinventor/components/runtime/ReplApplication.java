// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.components.runtime;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.components.runtime.util.EclairUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * Subclass of Application. Normally App Inventor apps just use the
 * android.app.Application class as their main application. However we
 * use the Bugsense debugging application with the MIT AI Companion
 * app.  This class is only pointed to by the Android Manifest if
 * Compiler.java (which builds the Manifest) is building the Wireless
 * version of the MIT AI Companion.  In this fashion we only turn on
 * bugsense when using the Wireless MIT AI Companion.
 *
 * Bugsense *can* be hooked into an Activity as well as an
 * Application, however ACRA, which we may use as an alternative, only
 * hooks into an Application object, so that is why we hook bugsense
 * here. It Makes changing over to ACRA reasonably easy.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

public class ReplApplication extends Application {

  private static ReplApplication theInstance = null;
  private boolean active = false;

  @Override
  public void onCreate() {
    super.onCreate();
    String apikey = GitBuildId.getBugsenseApiKey();
    if ((SdkLevel.getLevel() > SdkLevel.LEVEL_DONUT) && !apikey.equals("")) {
      EclairUtil.setupBugSense((Context) this, apikey);
      theInstance = this;
      active = true;
      Log.i("ReplApplication", "Bugsense Active APIKEY = " + apikey);
    } else {
      Log.i("ReplApplication", "Bugsense NOT ACTIVE");
    }
  }

  public static void reportError(Exception ex) {
    if (theInstance != null && theInstance.active) {
      EclairUtil.sendBugSenseException(ex);
    }
  }
}
