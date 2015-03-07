// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.ftc;

import com.google.appinventor.components.runtime.FtcRobotController;

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
import com.qualcomm.robotcore.util.ImmersiveMode;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FtcRobotControllerA {

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
      // HeyLiz! Not sure about this one.
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
              controllerS.getDriverStationMac());
          break;
        case CONNECTING:
          status += String.format("connecting to driver station (MAC address %s)",
              controllerS.getDriverStationMac());
          break;
        case CONNECTED_AS_PEER:
          status += String.format("connected to driver station (MAC address %s)",
              controllerS.getDriverStationMac());
          break;
        case CONNECTION_INFO_AVAILABLE:
          status += String.format("connected to driver station (MAC address %s)",
              controllerS.getDriverStationMac());

          break;
        case ERROR:
          status += "ERROR";
          break;
        default:
          status += "unexpected state: " + event.name();
          break;
      }

      DbgLog.msg(status);
      ftcRobotController.triggerWifiDirectUpdateEvent(status);
    }

    public void robotUpdate(final String status) {
      DbgLog.msg(status);
      ftcRobotController.triggerRobotUpdateEvent(status);
    }

  }

  protected final Callback callback = new Callback();

  private final FtcRobotController ftcRobotController;
  private final Activity activity;
  private final Context context;
  private final FtcRobotControllerS controllerS;

  private EventLoop eventLoop;

  public FtcRobotControllerA(FtcRobotController ftcRobotController, FtcRobotControllerS controllerS) {
    this.ftcRobotController = ftcRobotController;
    activity = ftcRobotController.getActivity();
    context = ftcRobotController.getContext();
    this.controllerS = controllerS;
    DbgLog.msg("Bound to Ftc Controller S");

    callback.wifiDirectUpdate(controllerS.getWifiDirectStatus());
    callback.robotUpdate(controllerS.getRobotStatus());
    requestRobotSetup();
  }

  private void requestRobotSetup() {
    if (controllerS == null) return;

    final String filename = Utility.CONFIG_FILES_DIR
        + ftcRobotController.getHardwareConfigFilename() + Utility.FILE_EXT;

    FileInputStream fis;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e) {
      String msg = "Cannot open robot configuration file - " + filename;
      Utility.complainToast(msg, context);
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

    eventLoop = new FtcEventLoop(factory, callback, ftcRobotController.getOpModeRegister());

    controllerS.setCallback(callback);
    controllerS.setupRobot(eventLoop);
  }

  private void requestRobotShutdown() {
    if (controllerS == null) return;
    controllerS.shutdownRobot();
  }

  private void requestRobotRestart() {
    requestRobotShutdown();
    requestRobotSetup();
  }
}
