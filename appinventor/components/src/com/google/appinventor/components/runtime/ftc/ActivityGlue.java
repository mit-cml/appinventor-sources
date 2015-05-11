// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ftc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.MenuInflater;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.qualcomm.ftccommon.DbgLog;

import com.google.appinventor.components.runtime.FtcRobotController;

class ActivityGlue {
  static final int RESULT_OK = Activity.RESULT_OK;

  final ResourceIds R;
  final FtcRobotController aiFtcRobotController;
  final Activity thisActivity;

  ActivityGlue(FtcRobotController aiFtcRobotController, Activity activity) {
    R = new ResourceIds(activity);
    this.aiFtcRobotController = aiFtcRobotController;
    thisActivity = activity;
  }

  /*
   * Called from FtcRobotController.resultReturned.
   */
  public void resultReturned(int request, int result, Intent intent) {
    onActivityResult(request, result, intent);
  }

  /*
   * Called from FtcRobotController.onNewIntent.
   */
  public void onNewIntentAI(Intent intent) {
    onNewIntent(intent);
  }

  /*
   * Called from FtcRobotController.prepareToDie
   */
  public void prepareToDie() {
    onStop();
  }

  // Activity methods that are overridden in FtcRobotControllerActivity.

  protected void onCreate(Bundle savedInstanceState) {
  }

  protected void onNewIntent(Intent intent) {
  }

  protected void onStart() {
  }

  protected void onResume() {
  }

  public void onPause() {
  }

  protected void onStop() {
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    return false;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    return false;
  }

  public void onConfigurationChanged(Configuration newConfig) {
  }

  protected void onActivityResult(int request, int result, Intent intent) {
  }

  // Activity methods that are called from FtcRobotControllerActivity.

  void runOnUiThread(Runnable runnable) {
    thisActivity.runOnUiThread(runnable);
  }

  ActionBar getActionBar() {
    return thisActivity.getActionBar();
  }

  Window getWindow() {
    return thisActivity.getWindow();
  }

  void startActivity(Intent intent) {
    try {
      thisActivity.startActivity(intent);
    } catch (Throwable e) {
      DbgLog.error("Could not start activity with intent " + intent);
    }
  }

  void startActivityForResult(Intent intent, int i) {
    try {
      thisActivity.startActivityForResult(intent, i);
    } catch (Throwable e) {
      DbgLog.error("Could not start activity with intent " + intent);
    }
  }

  void bindService(Intent intent, ServiceConnection connection, int flags) {
    thisActivity.bindService(intent, connection, flags);
  }

  void unbindService(ServiceConnection connection) {
    thisActivity.unbindService(connection);
  }

  MenuInflater getMenuInflater() {
    return thisActivity.getMenuInflater();
  }

  void setContentView(int layoutId) {
    // Inflate the layout into the FtcRobotController component's view.
    thisActivity.getLayoutInflater().inflate(layoutId, aiFtcRobotController.view, true);
  }

  View findViewById(int id) {
    return thisActivity.findViewById(id);
  }

  String getString(int id) {
    return thisActivity.getString(id);
  }

  void finish() {
    thisActivity.finish();
  }

  // ResourceIds simulates R.<type>.<name> used in FtcRobotControllerActivity.java

  static class ResourceIds {
    class RId {
      // from menu/ftc_robot_controller.xml
      final int action_restart_robot;
      final int action_wifi_channel_selector;
      final int action_settings;
      final int action_configuration;
      final int action_load;
      final int action_autoconfigure;
      final int action_view_logs;
      final int action_about;
      final int action_exit_app;
      // from layout/activity_ftc_controller.xml
      final int entire_screen;
      final int included_header;
      final int textWifiDirectStatus;
      final int textRobotStatus;
      final int textOpMode;
      final int textErrorMessage;
      final int textGamepad1;
      final int textGamepad2;
      // from layout/device_name.xml
      final int textDeviceName;
      // from layout/header.xml
      final int active_filename;

      RId(Context context) {
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        action_restart_robot =
            resources.getIdentifier("action_restart_robot", "id", packageName);
        action_wifi_channel_selector =
            resources.getIdentifier("action_wifi_channel_selector", "id", packageName);
        action_settings =
            resources.getIdentifier("action_settings", "id", packageName);
        action_configuration =
            resources.getIdentifier("action_configuration", "id", packageName);
        action_load =
            resources.getIdentifier("action_load", "id", packageName);
        action_autoconfigure =
            resources.getIdentifier("action_autoconfigure", "id", packageName);
        action_view_logs =
            resources.getIdentifier("action_view_logs", "id", packageName);
        action_about =
            resources.getIdentifier("action_about", "id", packageName);
        action_exit_app =
            resources.getIdentifier("action_exit_app", "id", packageName);
        entire_screen =
            resources.getIdentifier("entire_screen", "id", packageName);
        included_header =
            resources.getIdentifier("included_header", "id", packageName);
        textWifiDirectStatus =
            resources.getIdentifier("textWifiDirectStatus", "id", packageName);
        textRobotStatus =
            resources.getIdentifier("textRobotStatus", "id", packageName);
        textOpMode =
            resources.getIdentifier("textOpMode", "id", packageName);
        textErrorMessage =
            resources.getIdentifier("textErrorMessage", "id", packageName);
        textGamepad1 =
            resources.getIdentifier("textGamepad1", "id", packageName);
        textGamepad2 =
            resources.getIdentifier("textGamepad2", "id", packageName);
        textDeviceName =
            resources.getIdentifier("textDeviceName", "id", packageName);
        active_filename =
            resources.getIdentifier("active_filename", "id", packageName);
      }
    }
    class RLayout {
      final int activity_ftc_controller;

      RLayout(Context context) {
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        activity_ftc_controller = 
            resources.getIdentifier("activity_ftc_controller", "layout", packageName);
      }
    }
    class RMenu {
      final int ftc_robot_controller;

      RMenu(Context context) {
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        ftc_robot_controller =
            resources.getIdentifier("ftc_robot_controller", "menu", packageName);
      }
    }
    class RString {
      final int pref_hardware_config_filename;

      RString(Context context) {
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        pref_hardware_config_filename = 
            resources.getIdentifier("pref_hardware_config_filename", "string", packageName);
      }
    }
    class RXml {
      final int preferences;

      RXml(Context context) {
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        preferences = 
            resources.getIdentifier("preferences", "xml", packageName);
      }
    }

    final RId id;
    final RLayout layout;
    final RMenu menu;
    final RString string;
    final RXml xml;

    ResourceIds(Context context) {
      id = new RId(context);
      layout = new RLayout(context);
      menu = new RMenu(context);
      string = new RString(context);
      xml = new RXml(context);
    }
  }
}
