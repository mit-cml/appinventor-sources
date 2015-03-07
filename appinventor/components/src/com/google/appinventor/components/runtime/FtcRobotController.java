// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.collect.Maps;
import com.google.appinventor.components.runtime.ftc.FtcRobotControllerA;
import com.google.appinventor.components.runtime.ftc.FtcRobotControllerS;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.SdkLevel;

import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.hitechnic.HiTechnicHardwareFactory;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.factory.RobotFactory;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.LegacyModule;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.Telemetry;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.Util;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant.ConnectStatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FtcRobotController component
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ROBOT_CONTROLLER_COMPONENT_VERSION,
    description = "The primary FTC Robot Controller component",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesPermissions(permissionNames = // TODO: update this list.
                 "android.permission.ACCESS_WIFI_STATE, " +
                 "android.permission.CHANGE_WIFI_STATE, " +
                 "android.permission.ACCESS_NETWORK_STATE, " +
                 "android.permission.CHANGE_NETWORK_STATE, " +
                 "android.permission.INTERNET, " +
                 "android.permission.WRITE_EXTERNAL_STORAGE, " +
                 "android.permission.READ_EXTERNAL_STORAGE, " +
                 "android.permission.BLUETOOTH_ADMIN, " +
                 "android.permission.WAKE_LOCK")
@UsesLibraries(libraries = "RobotCore.jar,FtcCommon.jar,HiTechnic.jar,WirelessP2p.jar,d2xx.jar")
public final class FtcRobotController extends AndroidNonvisibleComponent
    implements Component, OnInitializeListener, OnNewIntentListener, OnDestroyListener, Deleteable,
    OpModeRegister {

  public interface HardwareDevice {
    void setHardwareMap(HardwareMap hardwareMap);
    void debugHardwareDevice(StringBuilder sb);
  }

  interface GamepadDevice {
    void setEventLoopManager(EventLoopManager eventLoopManager);
  }

  interface OpModeWrapper {
    String getOpModeName();
    OpMode getOpMode();
  }

  private static final long USB_SCAN_WAITTIME_MILLIS = 10 * 1000L; // 10 seconds
  private static final long WIFI_DIRECT_TIMEOUT_MILLIS = 2 * 60 * 1000L; // 2 minutes
  private static final String CONFIG_FILES_DIR =
      Environment.getExternalStorageDirectory() + "/FIRST/";
  private static final String CONFIG_FILE_EXT = ".xml";
  private static final String DEFAULT_CONFIGURATION = "robot_config";

  private static final Map<Form, List<HardwareDevice>> hardwareDevices = Maps.newHashMap();
  private static final Object hardwareDevicesLock = new Object();
  private static final Map<Form, List<GamepadDevice>> gamepadDevices = Maps.newHashMap();
  private static final Object gamepadDevicesLock = new Object();
  private static final Map<Form, List<OpModeWrapper>> opModeWrappers = Maps.newHashMap();
  private static final Object opModeWrappersLock = new Object();

  // Telemetry
  private final ElapsedTime telemetryTimer = new ElapsedTime();
  private final double telemetryInterval = 0.250; // in seconds

  private volatile String driverStationAddress = "";
  private volatile String configuration = DEFAULT_CONFIGURATION;

  /*
   * wakeLock is set in onInitialize, if the device version is Ice Cream Sandwich or later.
   */
  private PowerManager.WakeLock wakeLock;

  private FtcRobotControllerS ftcRobotControllerS; // set in onInitialize
  private FtcRobotControllerA ftcRobotControllerA; // set in onInitialize

  /*
   * wifiDirectAssistant is set in onInitialize, if the device version is Ice Cream Sandwich
   * or later.
   */
  private volatile WifiDirectAssistant wifiDirectAssistant;

  private volatile OpModeManager opModeManager;

  /*
   * robot is created by the robotSetupThread.
   */
  private volatile Robot robot;

  /*
   * eventLoopManager is set in init, which is called after the robotSetupThread calls
   * robot.start().
   */
  private volatile EventLoopManager eventLoopManager;

  public FtcRobotController(ComponentContainer container) {
    super(container.$form());
    System.out.println(System.currentTimeMillis() + " HeyLiz - FtcRobotController................................................................................");
    form.registerForOnInitialize(this);
    form.registerForOnNewIntent(this);
    form.registerForOnDestroy(this);
  }

  @Override
  public void onInitialize() {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ICE_CREAM_SANDWICH) {
      PowerManager powerManager = (PowerManager) form.getSystemService(Context.POWER_SERVICE);
      wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FtcRoboController");
      wakeLock.acquire();

      OpModeRegister opModeRegister = this;
      opModeManager = new OpModeManager(new HardwareMap(), opModeRegister);

      ftcRobotControllerS = new FtcRobotControllerS(this);
      FtcRobotControllerA ftcRobotControllerA = new FtcRobotControllerA(this, ftcRobotControllerS);

      ftcRobotControllerS.bindHeyLiz();
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    // In Qualcomm's code, this is done in FtcRobotControllerActivity.
    if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
      // a new USB device has been attached
      DbgLog.msg("USB Device attached; app restart may be needed");
    }
  }
  
  /**
   * Adds a {@link HardwareDevice} to the hardware devices.
   */
  public static void addHardwareDevice(Form form, HardwareDevice hardwareDevice) {
    synchronized (hardwareDevicesLock) {
      List<HardwareDevice> hardwareDevicesForForm = hardwareDevices.get(form);
      if (hardwareDevicesForForm == null) {
        hardwareDevicesForForm = Lists.newArrayList();
        hardwareDevices.put(form, hardwareDevicesForForm);
      }
      hardwareDevicesForForm.add(hardwareDevice);
    }
  }

  /**
   * Removes a {@link HardwareDevice} from the hardware devices.
   */
  public static void removeHardwareDevice(Form form, HardwareDevice hardwareDevice) {
    synchronized (hardwareDevicesLock) {
      List<HardwareDevice> hardwareDevicesForForm = hardwareDevices.get(form);
      if (hardwareDevicesForForm != null) {
        hardwareDevicesForForm.remove(hardwareDevice);
      }
    }
  }

  /**
   * Adds a {@link GamepadDevice} to the gamepad devices.
   */
  static void addGamepadDevice(Form form, GamepadDevice gamepadDevice) {
    synchronized (gamepadDevicesLock) {
      List<GamepadDevice> gamepadDevicesForForm = gamepadDevices.get(form);
      if (gamepadDevicesForForm == null) {
        gamepadDevicesForForm = Lists.newArrayList();
        gamepadDevices.put(form, gamepadDevicesForForm);
      }
      gamepadDevicesForForm.add(gamepadDevice);
    }
  }

  /**
   * Removes a {@link GamepadDevice} from the gamepad devices.
   */
  static void removeGamepadDevice(Form form, GamepadDevice gamepadDevice) {
    synchronized (gamepadDevicesLock) {
      List<GamepadDevice> gamepadDevicesForForm = gamepadDevices.get(form);
      if (gamepadDevicesForForm != null) {
        gamepadDevicesForForm.remove(gamepadDevice);
      }
    }
  }

  /**
   * Adds an {@link OpModeWrapper} to the opModeWrappers list.
   *
   * @param opModeWrapper  the {@code OpModeWrapper} to be added
   */
  static void addOpModeWrapper(Form form, OpModeWrapper opModeWrapper) {
    synchronized (opModeWrappersLock) {
      List<OpModeWrapper> opModeWrappersForForm = opModeWrappers.get(form);
      if (opModeWrappersForForm == null) {
        opModeWrappersForForm = Lists.newArrayList();
        opModeWrappers.put(form, opModeWrappersForForm);
      }
      opModeWrappersForForm.add(opModeWrapper);
    }
  }

  /**
   * Removes an {@link OpModeWrapper} from the opModeWrappers list.
   *
   * @param opModeWrapper  the {@code OpModeWrapper} to be removed
   */
  static void removeOpModeWrapper(Form form, OpModeWrapper opModeWrapper) {
    synchronized (opModeWrappersLock) {
      List<OpModeWrapper> opModeWrappersForForm = opModeWrappers.get(form);
      if (opModeWrappersForForm != null) {
        opModeWrappersForForm.remove(opModeWrapper);
      }
    }
  }

  // Methods called by FtcRobotControllerS and FtcRobotControllerA

  public Activity getActivity() {
    return form;
  }

  public Context getContext() {
    return form;
  }

  public String getDriverStationMac() {
    return driverStationAddress;
  }

  public String getHardwareConfigFilename() {
    return configuration;
  }

  public OpModeRegister getOpModeRegister() {
    return this;
  }

  public void triggerWifiDirectUpdateEvent(final String status) {
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        WifiDirectUpdate(status);
      }
    });
  }

  public void triggerRobotUpdateEvent(final String status) {
    DbgLog.msg(status);
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        RobotUpdate(status);
      }
    });
  }

  // Properties

  /**
   * Configuration property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The name of the robot configuration.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String Configuration() {
    return configuration;
  }

  /**
   * Configuration property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = DEFAULT_CONFIGURATION)
  @SimpleProperty(userVisible = false)
  public void Configuration(String configuration) {
    this.configuration = configuration;
    // HeyLiz restart robot?
  }

  /**
   * DriverStationAddress property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The address of the driver station.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String DriverStationAddress() {
    return driverStationAddress;
  }

  /**
   * DriverStationAddress property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void DriverStationAddress(String driverStationAddress) {
    this.driverStationAddress = driverStationAddress;
    // HeyLiz restart robot?
  }

  // Functions

  @SimpleFunction(description = "RangeClip")
  public double RangeClip(double number, double min, double max) {
    return Range.clip(number, min, max);
  }

  // Events

  @SimpleEvent(description = "WifiDirectUpdate event")
  public void WifiDirectUpdate(String status) {
    EventDispatcher.dispatchEvent(this, "WifiDirectUpdate", status);
  }

  @SimpleEvent(description = "RobotUpdate event")
  public void RobotUpdate(String status) {
    EventDispatcher.dispatchEvent(this, "RobotUpdate", status);
  }


  // HeyLiz who should call this? FtcEventLoop.init
  void initHardwareDevices(HardwareMap hardwareMap) {
    // Initialize each hardware device component.
    synchronized (hardwareDevicesLock) {
      List<HardwareDevice> hardwareDevicesForForm = hardwareDevices.get(form);
      if (hardwareDevicesForForm != null) {
        for (HardwareDevice hardwareDevice : hardwareDevicesForForm) {
          hardwareDevice.setHardwareMap(hardwareMap);
        }
      }
    }
  }

  // HeyLiz who should call this? FtcEventLoop.init
  void initGamepadDevices(EventLoopManager eventLoopManager) {
    synchronized (gamepadDevicesLock) {
      List<GamepadDevice> gamepadDevicesForForm = gamepadDevices.get(form);
      if (gamepadDevicesForForm != null) {
        for (GamepadDevice gamepadDevice : gamepadDevicesForForm) {
          gamepadDevice.setEventLoopManager(eventLoopManager);
        }
      }
    }
  }

  void teardownGamepadDevices() {
    synchronized (gamepadDevicesLock) {
      List<GamepadDevice> gamepadDevicesForForm = gamepadDevices.get(form);
      if (gamepadDevicesForForm != null) {
        for (GamepadDevice gamepadDevice : gamepadDevicesForForm) {
          gamepadDevice.setEventLoopManager(null);
        }
      }
    }
  }

  void teardownHardwareDevices() {
    synchronized (hardwareDevicesLock) {
      List<HardwareDevice> hardwareDevicesForForm = hardwareDevices.get(form);
      if (hardwareDevicesForForm != null) {
        for (HardwareDevice hardwareDevice : hardwareDevicesForForm) {
          hardwareDevice.setHardwareMap(null);
        }
      }
    }
  }

  // OpModeRegister implementation

  @Override
  public void register(OpModeManager opModeManager) {
    synchronized (opModeWrappersLock) {
      List<OpModeWrapper> opModeWrappersForForm = opModeWrappers.get(form);
      if (opModeWrappersForForm != null) {
        for (OpModeWrapper opModeWrapper : opModeWrappersForForm) {
          opModeManager.register(opModeWrapper.getOpModeName(), opModeWrapper.getOpMode());
        }
      }
    }
  }

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    prepareToDie();
  }

  // Deleteable implementation
  @Override
  public void onDelete() {
    prepareToDie();
  }

  private void prepareToDie() {
    ftcRobotControllerS.unbindHeyLiz();

    wakeLock.release();
    wakeLock = null;
  }
}
