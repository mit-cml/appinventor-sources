// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.components.runtime;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

import com.google.appinventor.components.runtime.util.ReplCommController;
import com.google.appinventor.components.runtime.util.AppInvHTTPD;
import com.google.appinventor.components.runtime.util.RetValManager;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.EclairUtil;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Activity;
import android.content.Context;

/**
 * Subclass of Form used by the 'stem cell apk', i.e. the Android app that allows communication
 * via the Repl
 *
 * @author markf@google.com (Your Name Here)
 */

public class ReplForm extends Form {

  // Controller for the ReplCommController associated with this form
  private ReplCommController formReplCommController = null;

  private AppInvHTTPD assetServer = null;
  public static ReplForm topform;
  private static final String REPL_ASSET_DIR = "/sdcard/AppInventor/assets/";
  private boolean IsUSBRepl = false;
  private boolean assetsLoaded = false;
  private boolean isDirect = false; // True for USB and emulator (AI2)

  public ReplForm() {
    super();
    topform = this;
  }

  @Override
  public void onCreate(Bundle icicle) {
    Intent intent = getIntent();
    processExtras(intent, false);
    super.onCreate(icicle);

    if (IsUSBRepl) { // Note: Obsolete code for AI1
      PackageManager packageManager = this.$context().getPackageManager();
      // the following is intended to prevent the application from being restarted
      // once it has ever run (so it can be run only once after it is installed)
      packageManager.setComponentEnabledSetting(
        new ComponentName(this.getPackageName(), this.getClass().getName()),
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
      formReplCommController = new ReplCommController(this);
      formReplCommController.startListening(true /*showAlert*/);
      assetsLoaded = true;                       // we don't have any for the usb repl...
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (formReplCommController != null)
        formReplCommController.startListening(true /*showAlert*/);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (formReplCommController != null)
        formReplCommController.stopListening(false /*showAlert*/);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (formReplCommController != null)
        formReplCommController.destroy();
    if (assetServer != null) {
        assetServer.stop();
        assetServer = null;
    }
    finish();                   // Must really exit here, so if you hits the back button we terminate completely.
    System.exit(0);
  }

  @Override
  protected void startNewForm(String nextFormName, Object startupValue) {
    RetValManager.pushScreen(nextFormName, startupValue);
  }

  @Override
  protected void closeForm(Intent resultIntent) {
    RetValManager.popScreen("Not Yet");
  }

  @Override
  protected void closeApplicationFromBlocks() {
    // Switching forms is not allowed in REPL (yet?).
    runOnUiThread(new Runnable() {
      public void run() {
        String message = "Closing forms is not currently supported during development.";
        Toast.makeText(ReplForm.this, message, Toast.LENGTH_LONG).show();
      }
    });
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    processExtras(intent, true);
  }

  protected void processExtras(Intent intent, boolean restart) {
    Bundle extras = intent.getExtras();
    if (extras != null) {
      Log.d("ReplForm", "extras: " + extras);
      Iterator<String> keys = extras.keySet().iterator();
      while (keys.hasNext()) {
        Log.d("ReplForm", "Extra Key: " + keys.next());
      }
    }
    if ((extras != null) && extras.getBoolean("rundirect")) {
      Log.d("ReplForm", "processExtras rundirect is true and restart is " + restart);
      isDirect = true;
      assetsLoaded = false;     // So we can reload them!
      if (restart) {
        this.clear();
        this.$define();
        this.Initialize();        // Restart UI
      }
    }
  }

  public boolean isDirect() {
    return isDirect;
  }

  public void setIsUSBrepl() {
    IsUSBRepl = true;
  }

  // Called from the Phone Status Block to start the Repl HTTPD
  public void startHTTPD(boolean secure) {
    try {
        if (assetServer == null) {
            checkAssetDir();
            assetServer = new AppInvHTTPD(8001, new File(REPL_ASSET_DIR), secure, this); // Probably should make the port variable
            Log.i("ReplForm", "started AppInvHTTPD");
        }
    } catch (IOException ex) {
      Log.e("ReplForm", "Setting up NanoHTTPD: " + ex.toString());
    }
  }

  public void startRepl() {  // Obsolete code for AI1
    Log.i("ReplForm", "startRepl()");
    formReplCommController = new ReplCommController(this);
    formReplCommController.startListening(true /*showAlert*/);
  }

  // Make sure that the REPL asset directory exists.
  private void checkAssetDir() {
    File f = new File(REPL_ASSET_DIR);
    if (!f.exists())
        f.mkdirs();             // Create the directory and all parents
  }

  // We return true if the assets for the Companion have been loaded and
  // displayed so we should look for all future assets in the sdcard which
  // is where assets are placed for the companion.
  // We return false until setAssetsLoaded is called which is done
  // by the phone status block
  public boolean isAssetsLoaded() {
    return assetsLoaded;
  }

  public void setAssetsLoaded() {
    assetsLoaded = true;
  }

}
