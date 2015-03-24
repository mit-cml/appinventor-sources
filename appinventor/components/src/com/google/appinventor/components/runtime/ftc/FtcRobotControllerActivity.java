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
// Copyright 2015 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.ftc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.hitechnic.HiTechnicHardwareFactory;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.mock.MockDeviceManager;
import com.qualcomm.robotcore.hardware.mock.MockHardwareFactory;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.appinventor.components.runtime.FtcRobotController;

public class FtcRobotControllerActivity {

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
      activity.runOnUiThread(new Runnable() {
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
          activity.runOnUiThread(new Runnable() {
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
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          // if there are no global error messages, getGlobalErrorMsg will return an empty string
          ftcRobotController.setRobotError(RobotLog.getGlobalErrorMsg());
        }
      });
    }

    public void wifiDirectUpdate(final WifiDirectAssistant.Event event) {
      String status = "Wifi Direct - ";

      switch (event) {
        case DISCONNECTED:
          status += "disconnected";
          break;
        case DISCOVERING_PEERS:
        case PEERS_AVAILABLE:
          status += String.format("looking for driver station (MAC address %s)",
              controllerService.getDriverStationMac());
          break;
        case CONNECTING:
          status += String.format("connecting to driver station (MAC address %s)",
              controllerService.getDriverStationMac());
          break;
        case CONNECTED_AS_PEER:
          status += String.format("connected to driver station (MAC address %s)",
              controllerService.getDriverStationMac());
          break;
        case CONNECTION_INFO_AVAILABLE:
          status += String.format("connected to driver station (MAC address %s)",
              controllerService.getDriverStationMac());

          break;
        case ERROR:
          status += "ERROR";
          break;
        default:
          status += "unexpected state: " + event.name();
          break;
      }

      DbgLog.msg(status);
      final String finalStatus = status;
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ftcRobotController.setWifiDirectStatus(finalStatus);
        }
      });
    }

    public void robotUpdate(final String status) {
      DbgLog.msg(status);
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ftcRobotController.setRobotStatus(status);

          // if there are no global error messages, getGlobalErrorMsg will return an empty string
          ftcRobotController.setRobotError(RobotLog.getGlobalErrorMsg());        }
      });
    }

  }

  protected Callback callback = new Callback();
  protected Context context;
  private Utility utility;

  protected FtcRobotControllerService controllerService;

  protected EventLoop eventLoop;

  private final FtcRobotController ftcRobotController;
  private final Activity activity;

  public FtcRobotControllerActivity(FtcRobotController ftcRobotController, Activity activity) {
    this.ftcRobotController = ftcRobotController;
    this.activity = activity;
  }

  public void onNewIntent(Intent intent) {
    if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
      // a new USB device has been attached
      DbgLog.msg("USB Device attached; app restart may be needed");
    }
  }

  public void onCreate() {
    context = activity;
    utility = new Utility();

    // save 4MB of logcat to the SD card
    RobotLog.writeLogcatToDisk(context, 4 * 1024);
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

    final String filename = Utility.CONFIG_FILES_DIR
        + ftcRobotController.getHardwareConfigFilename() + Utility.FILE_EXT;

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
        ServoController sc = dm.createUsbServoController(new SerialNumber("SC"));

        HardwareMap hwMap = new HardwareMap();
        hwMap.dcMotor.put("left", new DcMotor(mc, 1));
        hwMap.dcMotor.put("right", new DcMotor(mc, 2));
        hwMap.servo.put("a", new Servo(sc, 1));
        hwMap.servo.put("b", new Servo(sc, 2));
        factory = new MockHardwareFactory(hwMap);
      } catch (RobotCoreException e) {
        DbgLog.logStacktrace(e);
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        return;
      } catch (InterruptedException e) {
        DbgLog.logStacktrace(e);
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        return;
      }
  } else {
      // HiTechnic Factory for use with HiTechnic hardware
      HiTechnicHardwareFactory hitechnicFactory = new HiTechnicHardwareFactory(context);
      hitechnicFactory.setXmlInputStream(fis);
      factory = hitechnicFactory;
    }

    eventLoop = new FtcEventLoop(factory, callback,
        ftcRobotController, ftcRobotController.getOpModeRegister());

    controllerService.setCallback(callback);
    controllerService.setupRobot(eventLoop);
  }

  private void requestRobotShutdown() {
    if (controllerService == null) return;
    controllerService.shutdownRobot();
  }

  private void requestRobotRestart() {
    requestRobotShutdown();
    requestRobotSetup();
  }
}
