// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

import com.google.appinventor.components.runtime.util.AppInvHTTPD;
import com.google.appinventor.components.runtime.util.RetValManager;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.EclairUtil;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
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

  private AppInvHTTPD httpdServer = null;
  public static ReplForm topform;
  private static final String REPL_ASSET_DIR = "/sdcard/AppInventor/assets/";
  private boolean IsUSBRepl = false;
  private boolean assetsLoaded = false;
  private boolean isDirect = false; // True for USB and emulator (AI2)
  private Object replResult = null; // Return result when closing screen in Repl
  private String replResultFormName = null;

  public ReplForm() {
    super();
    topform = this;
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    Log.d("ReplForm", "onCreate");
    Intent intent = getIntent();
    processExtras(intent, false);
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (httpdServer != null) {
        httpdServer.stop();
        httpdServer = null;
    }
    finish();                   // Must really exit here, so if you hits the back button we terminate completely.
    System.exit(0);
  }

  @Override
  protected void startNewForm(String nextFormName, Object startupValue) {
    if (startupValue != null) {
      this.startupValue = jsonEncodeForForm(startupValue, "open another screen with start value");
    }
    RetValManager.pushScreen(nextFormName, startupValue);
  }

  public void setFormName(String formName) {
    this.formName = formName;
    Log.d("ReplForm", "formName is now " + formName);
  }

  @Override
  protected void closeForm(Intent resultIntent) {
    RetValManager.popScreen("Not Yet");
  }

  protected void setResult(Object result) {
    Log.d("ReplForm", "setResult: " + result);
    replResult = result;
    replResultFormName = formName;
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

  // Configure the system menu to include items to kill the application and to show "about"
  // information and providing the "Settings" menu option.

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // This procedure is called only once.  To change the items dynamically
    // we would use onPrepareOptionsMenu.
    super.onCreateOptionsMenu(menu); // sets up the exit and about buttons
    addSettingsButton(menu);         // Now add our button!
    return true;
  }

  public void addSettingsButton(Menu menu) {
    MenuItem showSettingsItem = menu.add(Menu.NONE, Menu.NONE, 3,
      "Settings").setOnMenuItemClickListener(new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem item) {
            PhoneStatus.doSettings();
            return true;
          }
        });
    showSettingsItem.setIcon(android.R.drawable.sym_def_app_icon);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Log.d("ReplForm", "onNewIntent Called");
    processExtras(intent, true);
  }

  void HandleReturnValues() {
    Log.d("ReplForm", "HandleReturnValues() Called, replResult = " + replResult);
    if (replResult != null) {   // Act as if it was returned
      OtherScreenClosed(replResultFormName, replResult);
      Log.d("ReplForm", "Called OtherScreenClosed");
      replResult = null;
    }
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
      assetsLoaded = true;
      if (restart) {
        this.clear();
        if (httpdServer != null) {
          httpdServer.resetSeq();
        } else {                // User manually started the Companion on her phone
          startHTTPD(true);     // but never typed in the UI and then connected via
          httpdServer.setHmacKey("emulator"); // USB. This is an ugly hack
        }
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
        if (httpdServer == null) {
            checkAssetDir();
            httpdServer = new AppInvHTTPD(8001, new File(REPL_ASSET_DIR), secure, this); // Probably should make the port variable
            Log.i("ReplForm", "started AppInvHTTPD");
        }
    } catch (IOException ex) {
      Log.e("ReplForm", "Setting up NanoHTTPD: " + ex.toString());
    }
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
