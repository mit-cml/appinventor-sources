/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

// Modified for App Inventor by Liz Looney
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ftc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
//AI import android.content.res.Configuration;
import android.hardware.usb.UsbManager;

//AI import android.os.Build;
//AI import android.os.Bundle;
//AI import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
//AI import android.view.Menu;
//AI import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.Device;
import com.qualcomm.ftccommon.FtcRobotControllerService;
import com.qualcomm.ftccommon.FtcRobotControllerService.FtcRobotControllerBinder;
import com.qualcomm.ftccommon.Restarter;
import com.qualcomm.ftccommon.UpdateUI;
import com.qualcomm.modernrobotics.ModernRoboticsHardwareFactory;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
//AI import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.hardware.mock.MockDeviceManager;
import com.qualcomm.robotcore.hardware.mock.MockHardwareFactory;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.Dimmer;
import com.qualcomm.robotcore.util.ImmersiveMode;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.appinventor.components.runtime.FtcRobotController;

public class FtcRobotControllerActivity /*AI extends Activity */ {

  private static final int REQUEST_CONFIG_WIFI_CHANNEL = 1;
  private static final boolean USE_MOCK_HARDWARE_FACTORY = false;
  private static final int NUM_GAMEPADS = 2;

  protected static final String VIEW_LOGS_ACTION = "com.qualcomm.ftcrobotcontroller.VIEW_LOGS";

  protected SharedPreferences preferences;

  protected UpdateUI.Callback callback;
  protected Context context;
  private Utility utility;
  private boolean launched;

  protected TextView textDeviceName;
  protected TextView textWifiDirectStatus;
  protected TextView textRobotStatus;
  protected TextView[] textGamepad = new TextView[NUM_GAMEPADS];
  protected TextView textOpMode;
  protected TextView textErrorMessage;
  //AI protected ImmersiveMode immersion;

  protected UpdateUI updateUI;
  protected BatteryChecker batteryChecker;
  protected Dimmer dimmer;
  protected LinearLayout entireScreenLayout;

  protected FtcRobotControllerService controllerService;

  protected FtcEventLoop eventLoop;

  protected class RobotRestarter implements Restarter {

    public void requestRestart() {
      requestRobotRestart();
    }

  }

  protected ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      FtcRobotControllerBinder binder = (FtcRobotControllerBinder) service;
      onServiceBind(binder.getService());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      controllerService = null;
    }
  };

  //AI @Override
  protected void onNewIntent(Intent intent) {
    //AI super.onNewIntent(intent);
    if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
      // a new USB device has been attached
      DbgLog.msg("USB Device attached; app restart may be needed");
    }
  }

  //AI @Override
  protected void onCreate(/*AI Bundle savedInstanceState*/) {
    //AI super.onCreate(savedInstanceState);

    //AI setContentView(R.layout.activity_ftc_controller);

    context = thisActivity;
    utility = new Utility(thisActivity);
    /*AI
    entireScreenLayout = (LinearLayout) findViewById(R.id.entire_screen);

    textDeviceName = (TextView) findViewById(R.id.textDeviceName);
    textWifiDirectStatus = (TextView) findViewById(R.id.textWifiDirectStatus);
    textRobotStatus = (TextView) findViewById(R.id.textRobotStatus);
    textOpMode = (TextView) findViewById(R.id.textOpMode);
    textErrorMessage = (TextView) findViewById(R.id.textErrorMessage);
    textGamepad[0] = (TextView) findViewById(R.id.textGamepad1);
    textGamepad[1] = (TextView) findViewById(R.id.textGamepad2);
    immersion = new ImmersiveMode(getWindow().getDecorView());
    */
    dimmer = new Dimmer(thisActivity);
    dimmer.longBright();
    Restarter restarter = new RobotRestarter();

    updateUI = new UpdateUI(thisActivity, dimmer);
    updateUI.setRestarter(restarter);
    updateUI.setTextViews(textWifiDirectStatus, textRobotStatus,
        textGamepad, textOpMode, textErrorMessage, textDeviceName);
    callback = updateUI.new Callback();

    //AI PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    preferences = PreferenceManager.getDefaultSharedPreferences(thisActivity);

    launched = false;

    hittingMenuButtonBrightensScreen();
  }

  //AI @Override
  protected void onStart() {
    //AI super.onStart();

    // save 4MB of logcat to the SD card
    RobotLog.writeLogcatToDisk(thisActivity, 4 * 1024);

    Intent intent = new Intent(thisActivity, FtcRobotControllerService.class);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);

    utility.updateHeader(Utility.NO_FILE, //AI R.string.pref_hardware_config_filename, R.id.active_filename, R.id.included_header);
        PREF_HARDWARE_CONFIG_FILENAME_KEY, textActiveFilename, headerLayout);

    callback.wifiDirectUpdate(WifiDirectAssistant.Event.DISCONNECTED);

    entireScreenLayout.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        dimmer.handleDimTimer();
        return false;
      }
    });
  }

  /*AI
  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
  }
  */

  //AI @Override
  protected void onStop() {
    //AI super.onStop();

    if (controllerService != null) unbindService(connection);

    RobotLog.cancelWriteLogcatToDisk(thisActivity);
  }

  /*AI
  @Override
  public void onWindowFocusChanged(boolean hasFocus){
    super.onWindowFocusChanged(hasFocus);
    // When the window loses focus (e.g., the action overflow is shown),
    // cancel any pending hide action. When the window gains focus,
    // hide the system UI.
    if (hasFocus) {
      if (ImmersiveMode.apiOver19()){
        // Immersive flag only works on API 19 and above.
        immersion.hideSystemUI();
      }
    } else {
      immersion.cancelSystemUIHide();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.ftc_robot_controller, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_restart_robot:
        dimmer.handleDimTimer();
        Toast.makeText(context, "Restarting Robot", Toast.LENGTH_SHORT).show();
        requestRobotRestart();
        return true;
      case R.id.action_wifi_channel_selector:
        if (Build.MODEL.equals(Device.MODEL_FOXDA_FL7007)) {
          startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
        } else {
          startActivityForResult(new Intent(getBaseContext(), FtcWifiChannelSelectorActivity.class), REQUEST_CONFIG_WIFI_CHANNEL);
        }
        return true;
      case R.id.action_settings:
        startActivity(new Intent(getBaseContext(), FtcRobotControllerSettingsActivity.class));
        return true;
      case R.id.action_about:
        startActivity(new Intent(getBaseContext(), AboutActivity.class));
        return true;
      case R.id.action_exit_app:
        finish();
        return true;
      case R.id.action_configuration:
        startActivity(new Intent(getBaseContext(), FtcConfigurationActivity.class));
        return true;
      case R.id.action_load:
          startActivity(new Intent(getBaseContext(), FtcLoadFileActivity.class));
          return true;
      case R.id.action_autoconfigure:
        startActivity(new Intent(getBaseContext(), AutoConfigureActivity.class));
      case R.id.action_view_logs:
        Intent viewLogsIntent = new Intent(VIEW_LOGS_ACTION);
        viewLogsIntent.putExtra(ViewLogsActivity.FILENAME, RobotLog.getLogFilename(this));
        startActivity(viewLogsIntent);
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // don't destroy assets on screen rotation
  }
  */

  //AI @Override
  protected void onActivityResult(int request, int result, Intent intent) {
    if (request == REQUEST_CONFIG_WIFI_CHANNEL) {
      if (result == RESULT_OK) {
        Toast toast = Toast.makeText(context, "Configuration Complete", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        showToast(toast);
      }
    }
  }

  public void onServiceBind(FtcRobotControllerService service) {
    DbgLog.msg("Bound to Ftc Controller Service");
    controllerService = service;
    updateUI.setControllerService(controllerService);

    callback.wifiDirectUpdate(controllerService.getWifiDirectStatus());
    callback.robotUpdate(controllerService.getRobotStatus());
    requestRobotSetup();
  }

  private void requestRobotSetup() {
    if (controllerService == null) return;

    FileInputStream fis = fileSetup();
    // if we can't find the file, don't try and build the robot.
    if (fis == null) { return; }

    HardwareFactory factory;

    if (USE_MOCK_HARDWARE_FACTORY) {
      // TODO: temp testing code. This will be removed in a future release
      try {
        factory = buildMockHardware();
      } catch (RobotCoreException e) {
        DbgLog.logStacktrace(e);
        Toast.makeText(thisActivity, e.getMessage(), Toast.LENGTH_LONG).show();
        return;
      } catch (InterruptedException e) {
        DbgLog.logStacktrace(e);
        Toast.makeText(thisActivity, e.getMessage(), Toast.LENGTH_LONG).show();
        return;
      }
  } else {
      // Modern Robotics Factory for use with Modern Robotics hardware
      ModernRoboticsHardwareFactory modernroboticsFactory = new ModernRoboticsHardwareFactory(context);
      modernroboticsFactory.setXmlInputStream(fis);
      factory = modernroboticsFactory;
    }

    eventLoop = new FtcEventLoop(factory, callback, aiFtcRobotController);

    controllerService.setCallback(callback);
    aiFtcRobotController.beforeSetupRobot();
    controllerService.setupRobot(eventLoop);

    long fiveMinutes = 300000; //milliseconds
    batteryChecker = new BatteryChecker(thisActivity, eventLoop, fiveMinutes);
    batteryChecker.startBatteryMonitoring();
  }

  private FileInputStream fileSetup() {
    boolean hasConfigFile = preferences.contains(//AI getString(R.string.pref_hardware_config_filename));
        PREF_HARDWARE_CONFIG_FILENAME_KEY);
    String activeFilename = utility.getFilenameFromPrefs(//AI R.string.pref_hardware_config_filename, Utility.NO_FILE);
        PREF_HARDWARE_CONFIG_FILENAME_KEY, Utility.NO_FILE);
    if (!launched) {
      if (!hasConfigFile ||
          activeFilename.equalsIgnoreCase(Utility.NO_FILE) ||
          activeFilename.toLowerCase().contains(Utility.UNSAVED.toLowerCase())) {
        /*AI
        utility.saveToPreferences(Utility.NO_FILE, R.string.pref_hardware_config_filename);
        DbgLog.msg("No default config file, so launching Hardware Wizard");
        launched = true;
        startActivity(new Intent(getBaseContext(), FtcLoadFileActivity.class));
        return null;
        */
      }
    }

    utility.updateHeader(Utility.NO_FILE, //AI R.string.pref_hardware_config_filename, R.id.active_filename, R.id.included_header);
        PREF_HARDWARE_CONFIG_FILENAME_KEY, textActiveFilename, headerLayout);

    final String filename = Utility.CONFIG_FILES_DIR
        + utility.getFilenameFromPrefs(//AI R.string.pref_hardware_config_filename, Utility.NO_FILE) + Utility.FILE_EXT;
        PREF_HARDWARE_CONFIG_FILENAME_KEY, Utility.NO_FILE) + Utility.FILE_EXT;

    FileInputStream fis;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e) {
      String msg = "Cannot open robot configuration file - " + filename;
      utility.complainToast(msg, context);
      DbgLog.msg(msg);
      return null;
    }
    return fis;
  }

  // TODO: temp testing code. This will be removed in a future release
  private MockHardwareFactory buildMockHardware() throws RobotCoreException, InterruptedException{
    DeviceManager dm = new MockDeviceManager(null, null);
    DcMotorController mc = dm.createUsbDcMotorController(new SerialNumber("MC"));
    DcMotorController mc2 = dm.createUsbDcMotorController(new SerialNumber("MC2"));
    ServoController sc = dm.createUsbServoController(new SerialNumber("SC"));

    HardwareMap hwMap = new HardwareMap();
    hwMap.dcMotor.put("left", new DcMotor(mc, 1));
    hwMap.dcMotor.put("right", new DcMotor(mc, 2));
    hwMap.dcMotor.put("flag", new DcMotor(mc2, 1));
    hwMap.dcMotor.put("arm", new DcMotor(mc2, 2));
    hwMap.servo.put("a", new Servo(sc, 1));
    hwMap.servo.put("b", new Servo(sc, 6));

    return new MockHardwareFactory(hwMap);
  }

  private void requestRobotShutdown() {
    if (controllerService == null) return;
    controllerService.shutdownRobot();
    if (batteryChecker != null) {
      batteryChecker.endBatteryMonitoring();
    }
  }

  private void requestRobotRestart() {
    requestRobotShutdown();
    requestRobotSetup();
  }

  protected void hittingMenuButtonBrightensScreen() {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
        @Override
        public void onMenuVisibilityChanged(boolean isVisible) {
          if (isVisible) {
            dimmer.handleDimTimer();
          }
        }
      });
    }
  }

  public void showToast(final Toast toast) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast.show();
      }
    });
  }

  // For App Inventor:
  public static final String PREF_HARDWARE_CONFIG_FILENAME_KEY = "pref_hardware_config_filename";
  private static final int RESULT_OK = Activity.RESULT_OK;
  private final FtcRobotController aiFtcRobotController;
  private final Activity thisActivity;
  private final TextView textActiveFilename;
  private final LinearLayout headerLayout;

  public FtcRobotControllerActivity(FtcRobotController aiFtcRobotController, Activity activity) {
    this.aiFtcRobotController = aiFtcRobotController;
    thisActivity = activity;
    textActiveFilename = aiFtcRobotController.textActiveFilename;
    headerLayout = aiFtcRobotController.headerLayout;
    entireScreenLayout = aiFtcRobotController.entireScreenLayout;
    textDeviceName = aiFtcRobotController.textDeviceName;
    textWifiDirectStatus = aiFtcRobotController.textWifiDirectStatus;
    textRobotStatus = aiFtcRobotController.textRobotStatus;
    textOpMode = aiFtcRobotController.textOpMode;
    textErrorMessage = aiFtcRobotController.textErrorMessage;
    textGamepad[0] = aiFtcRobotController.textGamepad[0];
    textGamepad[1] = aiFtcRobotController.textGamepad[1];

    onCreate();
    onStart();
  }

  // Called from FtcRobotController.
  public void restartRobot() {
    callback.restartRobot();
  }

  public void onNewIntentAI(Intent intent) {
    onNewIntent(intent);
  }

  public void onActivityResultAI(int request, int result, Intent intent) {
    onActivityResultAI(request, result, intent);
  }

  public void onStopAI() {
    onStop();
  }

  // Activity methods.

  private void runOnUiThread(Runnable runnable) {
    thisActivity.runOnUiThread(runnable);
  }

  private ActionBar getActionBar() {
    return thisActivity.getActionBar();
  }

  private void startActivity(Intent intent) {
    try {
      thisActivity.startActivity(intent);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void startActivityForResult(Intent intent, int i) {
    try {
      thisActivity.startActivityForResult(intent, i);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void bindService(Intent intent, ServiceConnection connection, int flags) {
    thisActivity.bindService(intent, connection, flags);
  }

  private void unbindService(ServiceConnection connection) {
    thisActivity.unbindService(connection);
  }
}
