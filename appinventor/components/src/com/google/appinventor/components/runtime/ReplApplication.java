// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.components.runtime.util.EclairUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.multidex.MultiDex;

import org.acra.*;
import org.acra.annotation.*;

/**
 * Subclass of Application. Normally App Inventor apps just use the
 * android.app.Application class as their main application. However we
 * use the ACRA debugging application with the MIT AI Companion
 * app.  This class is only pointed to by the Android Manifest if
 * Compiler.java (which builds the Manifest) is building the Wireless
 * version of the MIT AI Companion.  In this fashion we only turn on
 * ACRA when using the Wireless MIT AI Companion.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

@ReportsCrashes(formKey="")
public class ReplApplication extends Application {

  private boolean active = false;
  private static ReplApplication thisInstance;
  // the installed variable tells us whether or
  // not we have successfully installed the additional
  // dex files, including the DexOpt pass.
  // we default to true because in a non-Companion
  // context this module isn't used, but is still
  // consulted. The Application used in non-Companion
  // apps is MultiDexApplication which will always
  // do the full install if it is needed.
  public static boolean installed = true;

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    // Note: We call this with "false" which will only
    // splice in the secondary dex files if it doesn't
    // have to load the files and run the expensive
    // DexOpt pass. If DexOpt is required, then it
    // will be done in Form.onCreate()
    // Note: We only do this dance for the Companion
    installed = MultiDex.install(this, false);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    thisInstance = this;
    String acraUri = GitBuildId.getAcraUri();
    if (acraUri.equals("")) {
      Log.i("ReplApplication", "ACRA Not Active");
    } else {
      Log.i("ReplApplication", "ACRA Active, URI = " + acraUri);
      ACRAConfiguration config = ACRA.getNewDefaultConfig(this);
      config.setFormUri(acraUri);
      config.setDisableSSLCertValidation(true); // So we can use an MIT or self signed cert
      ACRA.setConfig(config);                   // On the server.
      ACRA.init(this);
      active = true;
    }
  }

  public static void reportError(Throwable ex, String reportId) {
    ACRA.getErrorReporter().putCustomData("reportid", reportId);
    reportError(ex);
  }

  public static void reportError(Throwable ex) {
    if (thisInstance != null && thisInstance.active)
      ACRA.getErrorReporter().handleException(ex);
  }

  public static boolean isAcraActive() {
    if (thisInstance != null && thisInstance.active) {
      return true;
    } else {
      return false;
    }
  }
}
