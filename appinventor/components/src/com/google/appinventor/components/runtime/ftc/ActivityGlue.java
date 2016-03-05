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
import android.view.MenuInflater;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;

import com.google.appinventor.components.runtime.FtcRobotController;
import com.google.appinventor.components.runtime.collect.Maps;

import java.util.Map;

/**
 * ActivityGlue is the base class for FtcRobotControllerActivity. It acts like glue between
 * FtcRobotControllerActivity and the actual activity of the app.
 */
class ActivityGlue {
  static final int RESULT_OK = Activity.RESULT_OK;

  protected final Activity thisActivity;
  protected final FtcRobotController aiFtcRobotController;
  private final Map<Integer, Integer> requestCodeToConstant = Maps.newHashMap();
  private final Map<Integer, Integer> constantToRequestCode = Maps.newHashMap();

  ActivityGlue(Activity activity, FtcRobotController aiFtcRobotController) {
    R.init(activity);
    thisActivity = activity;
    this.aiFtcRobotController = aiFtcRobotController;

    requestCodeToConstant.put(
        aiFtcRobotController.requestCodeConfigureRobot,
        LaunchActivityConstantsList.FTC_ROBOT_CONTROLLER_ACTIVITY_CONFIGURE_ROBOT);
    constantToRequestCode.put(
        LaunchActivityConstantsList.FTC_ROBOT_CONTROLLER_ACTIVITY_CONFIGURE_ROBOT,
        aiFtcRobotController.requestCodeConfigureRobot);
    requestCodeToConstant.put(
        aiFtcRobotController.requestCodeConfigureWifiChannel,
        FtcRobotControllerActivity.REQUEST_CONFIG_WIFI_CHANNEL);
    constantToRequestCode.put(
        FtcRobotControllerActivity.REQUEST_CONFIG_WIFI_CHANNEL,
        aiFtcRobotController.requestCodeConfigureWifiChannel);
  }

  /*
   * Called from FtcRobotController.resultReturned.
   */
  public void onActivityResultAI(int requestCode, int result, Intent intent) {
    try {
      onActivityResult(requestCodeToConstant.get(requestCode), result, intent);
    } catch (Throwable e) {
      DbgLog.error("Could not handle activity result for request code " + requestCode);
    }
  }

  /*
   * Called from FtcRobotController.onNewIntent.
   */
  public void onNewIntentAI(Intent intent) {
    onNewIntent(intent);
  }

  /*
   * Called from FtcRobotController.onResume.
   */
  public void onResumeAI() {
    onResume();
  }

  /*
   * Called from FtcRobotController.onStart.
   */
  public void onStartAI() {
    onStart();
  }

  /*
   * Called from FtcRobotController.onStop and FtcRobotController.onDelete.
   */
  public void onStopAI() {
    onStop();
  }

  // Activity methods that are overridden in FtcRobotControllerActivity.

  protected void onActivityResult(int request, int result, Intent intent) {
  }

  public void onConfigurationChanged(Configuration newConfig) {
  }

  protected void onCreate(Bundle savedInstanceState) {
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    return false;
  }

  protected void onNewIntent(Intent intent) {
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    return false;
  }

  public void onPause() {
  }

  protected void onResume() {
  }

  protected void onStart() {
  }

  protected void onStop() {
  }

  // Activity methods that are called from FtcRobotControllerActivity.

  void bindService(Intent intent, ServiceConnection connection, int flags) {
    thisActivity.bindService(intent, connection, flags);
  }

  View findViewById(int id) {
    return thisActivity.findViewById(id);
  }

  void finish() {
    thisActivity.finish();
  }

  ActionBar getActionBar() {
    return thisActivity.getActionBar();
  }

  Context getBaseContext() {
    return thisActivity.getBaseContext();
  }

  MenuInflater getMenuInflater() {
    return thisActivity.getMenuInflater();
  }

  String getString(int id) {
    return thisActivity.getString(id);
  }

  Object getSystemService(String s) {
    return thisActivity.getSystemService(s);
  }

  Window getWindow() {
    return thisActivity.getWindow();
  }

  void openOptionsMenu() {
    thisActivity.openOptionsMenu();
  }

  void runOnUiThread(Runnable runnable) {
    thisActivity.runOnUiThread(runnable);
  }

  void setContentView(int layoutId) {
    // Inflate the layout into the FtcRobotController component's view.
    thisActivity.getLayoutInflater().inflate(layoutId, aiFtcRobotController.view, true);
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
      thisActivity.startActivityForResult(intent, constantToRequestCode.get(i));
    } catch (Throwable e) {
      DbgLog.error("Could not start activity for result with intent " + intent + " and " + i);
    }
  }

  void unbindService(ServiceConnection connection) {
    thisActivity.unbindService(connection);
  }
}
