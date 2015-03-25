// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
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
import com.google.appinventor.components.runtime.ftc.FtcRobotControllerActivity;
import com.google.appinventor.components.runtime.ftc.FtcRobotControllerService;
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
import com.qualcomm.robotcore.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant.ConnectStatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
@UsesPermissions(permissionNames =
                 "android.permission.ACCESS_WIFI_STATE, " +
                 "android.permission.CHANGE_WIFI_STATE, " +
                 "android.permission.ACCESS_NETWORK_STATE, " +
                 "android.permission.CHANGE_NETWORK_STATE, " +
                 "android.permission.INTERNET, " +
                 "android.permission.WRITE_EXTERNAL_STORAGE, " +
                 "android.permission.READ_EXTERNAL_STORAGE, " +
                 "android.permission.WAKE_LOCK")
@UsesLibraries(libraries = "RobotCore.jar,FtcCommon.jar,HiTechnic.jar,WirelessP2p.jar,d2xx.jar")
public final class FtcRobotController extends AndroidNonvisibleComponent
    implements Component, OnInitializeListener, OnNewIntentListener, OnDestroyListener, Deleteable,
    OpModeRegister {

  interface HardwareDevice {
    void setHardwareMap(HardwareMap hardwareMap);
  }

  interface GamepadDevice {
    void setEventLoopManager(EventLoopManager eventLoopManager);
  }

  interface OpModeWrapper {
    String getOpModeName();
    OpMode getOpMode();
  }

  private static final int DEFAULT_USB_SCAN_TIME_IN_SECONDS = 2;
  private static final String DEFAULT_CONFIGURATION = "robot_config";

  private static final Map<Form, List<HardwareDevice>> hardwareDevices = Maps.newHashMap();
  private static final Object hardwareDevicesLock = new Object();
  private static final Map<Form, List<GamepadDevice>> gamepadDevices = Maps.newHashMap();
  private static final Object gamepadDevicesLock = new Object();
  private static final Map<Form, List<OpModeWrapper>> opModeWrappers = Maps.newHashMap();
  private static final Object opModeWrappersLock = new Object();

  private volatile int usbScanTimeInSeconds = DEFAULT_USB_SCAN_TIME_IN_SECONDS;
  private volatile String driverStationAddress = "";
  private volatile String configuration = DEFAULT_CONFIGURATION;

  private volatile String robotError = "";
  private volatile String wifiDirectStatus = "";
  private volatile String robotStatus = "";

  /*
   * wakeLock is set in onInitialize, if the device version is Ice Cream Sandwich or later.
   */
  private PowerManager.WakeLock wakeLock;

  private FtcRobotControllerService ftcRobotControllerService; // set in onInitialize
  private FtcRobotControllerActivity ftcRobotControllerActivity; // set in onInitialize

  public FtcRobotController(ComponentContainer container) {
    super(container.$form());
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

      ftcRobotControllerService = new FtcRobotControllerService(this, form);
      ftcRobotControllerActivity = new FtcRobotControllerActivity(this, form);

      ftcRobotControllerService.onBind();
      ftcRobotControllerActivity.onCreate();
      ftcRobotControllerActivity.onServiceBind(ftcRobotControllerService);
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onNewIntent(intent);
    }
  }
  
  /**
   * Adds a {@link HardwareDevice} to the hardware devices.
   */
  static void addHardwareDevice(Form form, HardwareDevice hardwareDevice) {
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
  static void removeHardwareDevice(Form form, HardwareDevice hardwareDevice) {
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

  // Called from FtcFtcRobotControllerService
  public int getUsbScanTimeInSeconds() {
    return usbScanTimeInSeconds;
  }

  // Called from FtcFtcRobotControllerService
  public String getDriverStationMac() {
    return driverStationAddress;
  }

  // Called from FtcFtcRobotControllerActivity
  public String getHardwareConfigFilename() {
    return configuration;
  }

  // Called from FtcFtcRobotControllerActivity on the UI thraed
  public void setRobotError(String robotError) {
    if (!this.robotError.equals(robotError)) {
      this.robotError = robotError;
      RobotError(robotError);
    }
  }

  // Called from FtcFtcRobotControllerActivity on the UI thraed
  public void setWifiDirectStatus(String wifiDirectStatus) {
    if (!this.wifiDirectStatus.equals(wifiDirectStatus)) {
      this.wifiDirectStatus = wifiDirectStatus;
      WifiDirectStatus(wifiDirectStatus);
    }
  }

  // Called from FtcFtcRobotControllerActivity on the UI thraed
  public void setRobotStatus(String robotStatus) {
    if (!this.robotStatus.equals(robotStatus)) {
      this.robotStatus = robotStatus;
      RobotStatus(robotStatus);
    }
  }

  // Called from FtcEventLoop.init
  public void onEventLoopInit(EventLoopManager eventLoopManager, HardwareMap hardwareMap) {
    synchronized (gamepadDevicesLock) {
      List<GamepadDevice> gamepadDevicesForForm = gamepadDevices.get(form);
      if (gamepadDevicesForForm != null) {
        for (GamepadDevice gamepadDevice : gamepadDevicesForForm) {
          gamepadDevice.setEventLoopManager(eventLoopManager);
        }
      }
    }
    synchronized (hardwareDevicesLock) {
      List<HardwareDevice> hardwareDevicesForForm = hardwareDevices.get(form);
      if (hardwareDevicesForForm != null) {
        for (HardwareDevice hardwareDevice : hardwareDevicesForForm) {
          hardwareDevice.setHardwareMap(hardwareMap);
        }
      }
    }
  }

  // Called from FtcEventLoop.teardown
  public void onEventLoopTeardown() {
    synchronized (gamepadDevicesLock) {
      List<GamepadDevice> gamepadDevicesForForm = gamepadDevices.get(form);
      if (gamepadDevicesForForm != null) {
        for (GamepadDevice gamepadDevice : gamepadDevicesForForm) {
          gamepadDevice.setEventLoopManager(null);
        }
      }
    }
    synchronized (hardwareDevicesLock) {
      List<HardwareDevice> hardwareDevicesForForm = hardwareDevices.get(form);
      if (hardwareDevicesForForm != null) {
        for (HardwareDevice hardwareDevice : hardwareDevicesForForm) {
          hardwareDevice.setHardwareMap(null);
        }
      }
    }
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
    if (!this.configuration.equals(configuration)) {
      this.configuration = configuration;
      ftcRobotControllerActivity.restartRobot();
    }
  }

  /**
   * UsbScanTimeInSeconds property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The time reserved for scanning USB devices, in seconds.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public int UsbScanTimeInSeconds() {
    return usbScanTimeInSeconds;
  }

  /**
   * UsbScanTimeInSeconds property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = DEFAULT_USB_SCAN_TIME_IN_SECONDS + "")
  @SimpleProperty(userVisible = false)
  public void UsbScanTimeInSeconds(int usbScanTimeInSeconds) {
    this.usbScanTimeInSeconds = usbScanTimeInSeconds;
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
    if (!this.driverStationAddress.equals(driverStationAddress)) {
      this.driverStationAddress = driverStationAddress;
      ftcRobotControllerActivity.restartRobot();
    }
  }

  // Functions

  @SimpleFunction(description = "RangeClip")
  public double RangeClip(double number, double min, double max) {
    try {
      return Range.clip(number, min, max);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "RangeClip",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0.0;
  }

  // TODO(lizlooney): Add support for other com.qualcomm.robotcore.util classes:
  // CurvedWheelMotion, Range (other methods), RollingAverage

  // Events

  @SimpleEvent(description = "RobotError event")
  public void RobotError(String error) {
    EventDispatcher.dispatchEvent(this, "RobotError", error);
  }

  @SimpleEvent(description = "WifiDirectStatus event")
  public void WifiDirectStatus(String status) {
    EventDispatcher.dispatchEvent(this, "WifiDirectStatus", status);
  }

  @SimpleEvent(description = "RobotStatus event")
  public void RobotStatus(String status) {
    EventDispatcher.dispatchEvent(this, "RobotStatus", status);
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
    ftcRobotControllerService.onUnbind();

    wakeLock.release();
    wakeLock = null;
  }
}
