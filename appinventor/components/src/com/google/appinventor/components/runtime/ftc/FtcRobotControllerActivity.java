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
//AI import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//AI import android.content.ServiceConnection;
import android.content.SharedPreferences;
//AI import android.content.res.Configuration;
import android.hardware.usb.UsbManager;

//AI import android.os.Build;
//AI import android.os.Bundle;
//AI import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
//AI import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.Device;
//AI import com.qualcomm.ftcrobotcontroller.FtcRobotControllerService.FtcRobotControllerBinder;
import com.qualcomm.modernrobotics.ModernRoboticsHardwareFactory;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.Gamepad;
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

  /**
   * Callback methods
   */
  public class Callback {

    /**
     * callback method to restart the robot
     */
    public void restartRobot() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(context, "Restarting Robot", Toast.LENGTH_SHORT).show();
        }
      });

      // this call might be coming from the event loop, so we need to start
      // switch contexts before proceeding
      Thread t = new Thread() {
        @Override
        public void run() {
          try { Thread.sleep(1500); } catch (InterruptedException ignored) { }
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              requestRobotRestart();
            }
          });

        }
      };
      t.start();
    }

    public void updateUi(final String opModeName, final Gamepad[] gamepads) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          for (int i = 0; (i < textGamepad.length) && (i < gamepads.length); i++) {
            if (gamepads[i].id == Gamepad.ID_UNASSOCIATED) {
              textGamepad[i].setText(" "); // for some reason "" isn't working, android won't redraw the UI element
            } else {
              textGamepad[i].setText(gamepads[i].toString());
            }
          }

          textOpMode.setText("Op Mode: " + opModeName);

          // if there are no global error messages, getGlobalErrorMsg will return an empty string
          textErrorMessage.setText(RobotLog.getGlobalErrorMsg());
        }
      });
    }

    public void wifiDirectUpdate(final WifiDirectAssistant.Event event) {
      final String status = "Wifi Direct - ";

      switch (event) {
        case DISCONNECTED:
          updateWifiDirectStatus(status + "disconnected");
          break;
        case CONNECTED_AS_GROUP_OWNER:
          updateWifiDirectStatus(status + "enabled");
          break;
        case ERROR:
          updateWifiDirectStatus(status + "ERROR");
          break;
        case CONNECTION_INFO_AVAILABLE:
          WifiDirectAssistant wifiDirectAssistant = controllerService.getWifiDirectAssistant();
          displayDeviceName(wifiDirectAssistant.getDeviceName());
        default:
          break;
      }
    }

    public void robotUpdate(final String status) {
      DbgLog.msg(status);
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          textRobotStatus.setText(status);


          // if there are no global error messages, getGlobalErrorMsg will return an empty string
          textErrorMessage.setText(RobotLog.getGlobalErrorMsg());
          if (RobotLog.hasGlobalErrorMsg()) {
            dimmer.longBright();
          }
        }
      });
    }

  }

  protected SharedPreferences preferences;

  protected Callback callback = new Callback();
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

  protected BatteryChecker batteryChecker;
  protected Dimmer dimmer;
  protected LinearLayout entireScreenLayout;

  protected FtcRobotControllerService controllerService;

  protected FtcEventLoop eventLoop;

  /*AI
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
  */

  /*AI @Override
  protected */ public void onNewIntent(Intent intent) {
    //AI super.onNewIntent(intent);
    if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
      // a new USB device has been attached
      DbgLog.msg("USB Device attached; app restart may be needed");
    }
  }

  /*AI @Override
  protected */ public void onCreate(/*AI Bundle savedInstanceState*/) {
    //AI super.onCreate(savedInstanceState);

    //AI setContentView(R.layout.activity_ftc_controller);

    context = thisActivity;
    utility = new Utility(thisActivity);
    entireScreenLayout = //AI (LinearLayout) findViewById(R.id.entire_screen)
        ftcRobotController.entireScreenLayout;

    textDeviceName = //AI (TextView) findViewById(R.id.textDeviceName)
        ftcRobotController.textDeviceName;
    textWifiDirectStatus = //AI (TextView) findViewById(R.id.textWifiDirectStatus)
        ftcRobotController.textWifiDirectStatus;
    textRobotStatus = //AI (TextView) findViewById(R.id.textRobotStatus)
        ftcRobotController.textRobotStatus;
    textOpMode = //AI (TextView) findViewById(R.id.textOpMode)
        ftcRobotController.textOpMode;
    textErrorMessage = //AI (TextView) findViewById(R.id.textErrorMessage)
        ftcRobotController.textErrorMessage;
    textGamepad[0] = //AI (TextView) findViewById(R.id.textGamepad1)
        ftcRobotController.textGamepad[0];
    textGamepad[1] = //AI (TextView) findViewById(R.id.textGamepad2)
        ftcRobotController.textGamepad[1];
    //AI immersion = new ImmersiveMode(getWindow().getDecorView());
    dimmer = new Dimmer(thisActivity);
    dimmer.longBright();

    //AI PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    preferences = PreferenceManager.getDefaultSharedPreferences(thisActivity);

    launched = false;

    hittingMenuButtonBrightensScreen();
  }

  /*AI @Override
  protected */ public void onStart() {
    //AI super.onStart();

    // save 4MB of logcat to the SD card
    RobotLog.writeLogcatToDisk(thisActivity, 4 * 1024);

    /*AI
    Intent intent = new Intent(this, FtcRobotControllerService.class);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);
    */

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

  /*AI @Override
  protected */ public void onStop() {
    //AI super.onStop();

    if (controllerService != null) unbindService(/*AI connection */);

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
  */

//AI  @Override
//AI  public boolean onOptionsItemSelected(MenuItem item) {
//AI    switch (item.getItemId()) {
//AI      case R.id.action_restart_robot:
//AI        dimmer.handleDimTimer();
//AI        Toast.makeText(context, "Restarting Robot", Toast.LENGTH_SHORT).show();
//AI        requestRobotRestart();
//AI        return true;
//AI      case R.id.action_wifi_channel_selector:
//AI        if (Build.MODEL.equals(Device.MODEL_FOXDA_FL7007)) {
//AI          startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
//AI        } else {
//AI          startActivityForResult(new Intent(//AI getBaseContext(), FtcWifiChannelSelectorActivity.class),
//AI              "com.qualcomm.ftcrobotcontroller.FtcWifiChannelSelectorActivity",
//AI              REQUEST_CONFIG_WIFI_CHANNEL));
//AI        }
//AI        return true;
//AI      case R.id.action_settings:
//AI        startActivity(new Intent(//AI getBaseContext(), FtcRobotControllerSettingsActivity.class));
//AI            "com.qualcomm.ftcrobotcontroller.FtcRobotControllerSettingsActivity"));
//AI        return true;
//AI      /*AI
//AI      case R.id.action_about:
//AI        startActivity(new Intent(getBaseContext(), AboutActivity.class));
//AI        return true;
//AI      case R.id.action_exit_app:
//AI        finish();
//AI        return true;
//AI      */
//AI      case R.id.action_configuration:
//AI        startActivity(new Intent(//AI getBaseContext(), FtcConfigurationActivity.class));
//AI            "com.qualcomm.ftcrobotcontroller.FtcConfigurationActivity"));
//AI        return true;
//AI      case R.id.action_load:
//AI        startActivity(new Intent(//AI getBaseContext(), FtcLoadFileActivity.class));
//AI            "com.qualcomm.ftcrobotcontroller.FtcLoadFileActivity"));
//AI        return true;
//AI      case R.id.action_autoconfigure:
//AI        startActivity(new Intent(//AI getBaseContext(), AutoConfigureActivity.class));
//AI            "com.qualcomm.ftcrobotcontroller.AutoConfigureActivity"));
//AI      default:
//AI        return super.onOptionsItemSelected(item);
//AI    }
//AI  }

  /*AI
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // don't destroy assets on screen rotation
  }
  */

  /*AI @Override
  protected */ public void onActivityResult(int request, int result, Intent intent) {
    if (request == REQUEST_CONFIG_WIFI_CHANNEL) {
      if (result == Activity.RESULT_OK) {
        Toast toast = Toast.makeText(context, "Configuration Complete", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        showToast(toast);
      }
    }
  }

  public void onServiceBind(FtcRobotControllerService service) {
    DbgLog.msg("Bound to Ftc Controller Service");
    controllerService = service;

    callback.wifiDirectUpdate(controllerService.getWifiDirectStatus());
    callback.robotUpdate(controllerService.getRobotStatus());
    requestRobotSetup();
  }

  private void requestRobotSetup() {
    if (controllerService == null) return;

    boolean hasConfigFile = preferences.contains(//AI getString(R.string.pref_hardware_config_filename));
        PREF_HARDWARE_CONFIG_FILENAME_KEY);
    String activeFilename = utility.getFilenameFromPrefs(//AI R.string.pref_hardware_config_filename, Utility.NO_FILE);
        PREF_HARDWARE_CONFIG_FILENAME_KEY, Utility.NO_FILE);
    if (!launched) {
      if (!hasConfigFile ||
          activeFilename.equalsIgnoreCase(Utility.NO_FILE) ||
          activeFilename.toLowerCase().contains(Utility.UNSAVED.toLowerCase())) {
        /*AI
        utility.saveToPreferences(Utility.NO_FILE, //AI R.string.pref_hardware_config_filename);
            PREF_HARDWARE_CONFIG_FILENAME_KEY);
        DbgLog.msg("No default config file, so launching Hardware Wizard");
        launched = true;
        startActivity(new Intent(//AI getBaseContext(), FtcLoadFileActivity.class));
            "com.qualcomm.ftcrobotcontroller.FtcLoadFileActivity"));
        return;
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
      return;
    }

    HardwareFactory factory;

    if (USE_MOCK_HARDWARE_FACTORY) {
      // TODO: temp testing code. This will be removed in a future release
      try {
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
        factory = new MockHardwareFactory(hwMap);
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

    eventLoop = new FtcEventLoop(factory, callback, ftcRobotController);

    controllerService.setCallback(callback);
    controllerService.setupRobot(eventLoop);

    long fiveMinutes = 300000; //milliseconds
    batteryChecker = new BatteryChecker(thisActivity, eventLoop, fiveMinutes);
    batteryChecker.startBatteryMonitoring();
  }

  private void requestRobotShutdown() {
    if (controllerService == null) return;
    controllerService.shutdownRobot();
    batteryChecker.endBatteryMonitoring();
  }

  private void requestRobotRestart() {
    requestRobotShutdown();
    requestRobotSetup();
  }

  private void updateWifiDirectStatus(String status) {
    DbgLog.msg(status);
    final String finalStatus = status;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        textWifiDirectStatus.setText(finalStatus);
      }
    });
  }

  private void displayDeviceName(final String name) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        textDeviceName.setText(name);
      }
    });
  }

  protected void hittingMenuButtonBrightensScreen(){
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

  public void showToast(final String msg, final int duration) {
    showToast(Toast.makeText(context, msg, duration));
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
  private final FtcRobotController ftcRobotController;
  private final Activity thisActivity;
  private final LinearLayout headerLayout;
  private final TextView textActiveFilename;

  public FtcRobotControllerActivity(FtcRobotController ftcRobotController, Activity activity) {
    this.ftcRobotController = ftcRobotController;
    thisActivity = activity;
    headerLayout = ftcRobotController.headerLayout;
    textActiveFilename = ftcRobotController.textActiveFilename;
  }

  private void unbindService() {
    controllerService = null;
  }

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

  public void restartRobot() {
    callback.restartRobot();
  }
}
