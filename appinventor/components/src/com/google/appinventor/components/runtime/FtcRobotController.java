// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.ftc.FtcRobotControllerActivity;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.SdkLevel;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.TypeConversion;
import com.qualcomm.robotcore.util.Version;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The primary FTC Robot Controller component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ROBOT_CONTROLLER_COMPONENT_VERSION,
    description = "The primary FTC Robot Controller component",
    category = ComponentCategory.FIRSTTECHCHALLENGE)
@SimpleObject
@UsesPermissions(permissionNames =
                 "android.permission.ACCESS_WIFI_STATE, " +
                 "android.permission.CHANGE_WIFI_STATE, " +
                 "android.permission.WAKE_LOCK, " +
                 "android.permission.ACCESS_NETWORK_STATE, " +
                 "android.permission.CHANGE_NETWORK_STATE, " +
                 "android.permission.INTERNET, " +
                 "android.permission.WRITE_EXTERNAL_STORAGE, " +
                 "android.permission.READ_EXTERNAL_STORAGE, " +
                 "android.permission.WRITE_SETTINGS")
@UsesLibraries(libraries =
  "FtcAnalytics.jar," +
  "FtcCommon.jar," +
  "FtcHardware.jar," +
  "FtcModernRobotics.jar," +
  "FtcRobotCore.jar," +
  "FtcWirelessP2p.jar")
public final class FtcRobotController extends AndroidViewComponent implements OnInitializeListener,
    ActivityResultListener, OnNewIntentListener, OnCreateOptionsMenuListener,
    OnOptionsItemSelectedListener, OnPauseListener, OnResumeListener, OnStartListener,
    OnStopListener, OnDestroyListener, Deleteable, OpModeRegister {

  interface GamepadDevice {
    void initGamepadDevice(OpMode opMode);
    void clearGamepadDevice();
  }

  interface HardwareDevice {
    void initHardwareDevice(OpMode opMode);
    void clearHardwareDevice();
  }

  interface OpModeWrapper {
    String getOpModeName();
    OpMode getOpMode();
  }

  private static final int DEFAULT_USB_SCAN_TIME_IN_SECONDS = 0;
  private static final String DEFAULT_CONFIGURATION = "";

  private static final Object robotControllersLock = new Object();
  private static final List<FtcRobotController> robotControllers = Lists.newArrayList();
  private static final Object gamepadDevicesLock = new Object();
  private static final List<GamepadDevice> gamepadDevices = Lists.newArrayList();
  private static final Object hardwareDevicesLock = new Object();
  private static final List<HardwareDevice> hardwareDevices = Lists.newArrayList();
  private static final Object opModeWrappersLock = new Object();
  private static final List<OpModeWrapper> opModeWrappers = Lists.newArrayList();
  private static volatile OpMode activeOpMode;
  private static volatile HardwareMap activeHardwareMap;

  private final Form form;
  public final LinearLayout view;

  // The request codes for launching other activities.
  public final int requestCodeConfigureRobot;
  public final int requestCodeConfigureWifiChannel;

  // Backing for properties.
  private volatile int usbScanTimeInSeconds = DEFAULT_USB_SCAN_TIME_IN_SECONDS;
  private volatile String configuration = DEFAULT_CONFIGURATION;
  private volatile int backgroundColor = Component.COLOR_WHITE;

  /*
   * wakeLock and ftcRobotControllerActivity are set in onInitialize,
   * if the device version is Ice Cream Sandwich or later.
   */
  private PowerManager.WakeLock wakeLock;
  private FtcRobotControllerActivity ftcRobotControllerActivity;

  public FtcRobotController(ComponentContainer container) {
    super(container.$form());
    form = container.$form();

    Context context = container.$context();
    view = new LinearLayout(context);

    container.$add(this);

    synchronized (robotControllersLock) {
      robotControllers.add(this);
    }

    form.registerForOnInitialize(this);
    requestCodeConfigureRobot = form.registerForActivityResult(this);
    requestCodeConfigureWifiChannel = form.registerForActivityResult(this);
    form.registerForOnNewIntent(this);
    form.omitExitMenu();
    form.registerForOnCreateOptionsMenu(this);
    form.registerForOnOptionsItemSelected(this);
    form.registerForOnDestroy(this);
    form.registerForOnPause(this);
    form.registerForOnResume(this);
    form.registerForOnStart(this);
    form.registerForOnStop(this);
  }

  // AndroidViewComponent implementation

  @Override
  public View getView() {
    return view;
  }

  // OnInitializeListener implementation

  @Override
  public void onInitialize() {
    int robotControllersCount;
    synchronized (robotControllersLock) {
      robotControllersCount = robotControllers.size();
    }
    if (robotControllersCount == 1) {
      if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ICE_CREAM_SANDWICH) {
        PowerManager powerManager = (PowerManager) form.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FtcRobotController");
        wakeLock.acquire();

        ftcRobotControllerActivity = new FtcRobotControllerActivity(this, form, configuration);
        view.requestLayout();
      } else {
        form.dispatchErrorOccurredEvent(this, "FtcRobotController",
            ErrorMessages.ERROR_FUNCTIONALITY_NOT_SUPPORTED_WIFI_DIRECT);
      }
    } else {
      form.dispatchErrorOccurredEvent(this, "FtcRobotController",
          ErrorMessages.ERROR_FTC_TOO_MANY_ROBOT_CONTROLLERS);
    }
  }

  // ActivityResultListener implementation

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == requestCodeConfigureRobot ||
        requestCode == requestCodeConfigureWifiChannel) {
      if (ftcRobotControllerActivity != null) {
        ftcRobotControllerActivity.onActivityResultAI(requestCode, resultCode, data);
      }
    }
  }

  // OnNewIntentListener implementation

  @Override
  public void onNewIntent(Intent intent) {
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onNewIntentAI(intent);
    }
  }

  // OnCreateOptionsMenuListener implementation

  @Override
  public void onCreateOptionsMenu(Menu menu) {
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onCreateOptionsMenu(menu);
    }
  }

  // OnOptionsItemSelectedListener implementation

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (ftcRobotControllerActivity != null) {
      return ftcRobotControllerActivity.onOptionsItemSelected(item);
    }
    return false;
  }

  // OnPauseListener implementation

  @Override
  public void onPause() {
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onPause();
    }
  }

  // OnResumeListener implementation

  @Override
  public void onResume() {
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onResumeAI();
    }
  }

  // OnStartListener implementation

  @Override
  public void onStart() {
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onStartAI();
    }
  }

  // OnStopListener implementation

  @Override
  public void onStop() {
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onStopAI();
    }
  }

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    prepareToDie();
    synchronized (robotControllersLock) {
      robotControllers.remove(this);
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onStopAI();
    }
    prepareToDie();
    synchronized (robotControllersLock) {
      robotControllers.remove(this);
    }
  }

  // OpModeRegister implementation

  @Override
  public void register(OpModeManager opModeManager) {
    synchronized (opModeWrappersLock) {
      Collections.sort(opModeWrappers, new Comparator<OpModeWrapper>() {
        @Override
        public int compare(OpModeWrapper opModeWrapper1, OpModeWrapper opModeWrapper2) {
          String name1 = opModeWrapper1.getOpModeName();
          String name2 = opModeWrapper2.getOpModeName();
          return name1.compareToIgnoreCase(name2);
        }
      });
      for (OpModeWrapper opModeWrapper : opModeWrappers) {
        opModeManager.register(opModeWrapper.getOpModeName(), opModeWrapper.getOpMode());
      }
    }
  }

  /**
   * Adds a {@link GamepadDevice} to the gamepad devices.
   */
  static void addGamepadDevice(GamepadDevice gamepadDevice) {
    synchronized (gamepadDevicesLock) {
      gamepadDevices.add(gamepadDevice);
    }
  }

  /**
   * Removes a {@link GamepadDevice} from the gamepad devices.
   */
  static void removeGamepadDevice(GamepadDevice gamepadDevice) {
    synchronized (gamepadDevicesLock) {
      gamepadDevices.remove(gamepadDevice);
    }
  }

  /**
   * Adds a {@link HardwareDevice} to the hardware devices.
   */
  static void addHardwareDevice(HardwareDevice hardwareDevice) {
    synchronized (hardwareDevicesLock) {
      hardwareDevices.add(hardwareDevice);
    }
  }

  /**
   * Removes a {@link HardwareDevice} from the hardware devices.
   */
  static void removeHardwareDevice(HardwareDevice hardwareDevice) {
    synchronized (hardwareDevicesLock) {
      hardwareDevices.remove(hardwareDevice);
    }
  }

  /**
   * Adds an {@link OpModeWrapper} to the opModeWrappers list.
   *
   * @param opModeWrapper  the {@code OpModeWrapper} to be added
   */
  static void addOpMode(OpModeWrapper opModeWrapper) {
    synchronized (opModeWrappersLock) {
      opModeWrappers.add(opModeWrapper);
    }
  }

  /**
   * Removes an {@link OpModeWrapper} from the opModeWrappers list.
   *
   * @param opModeWrapper  the {@code OpModeWrapper} to be removed
   */
  static void removeOpMode(OpModeWrapper opModeWrapper) {
    synchronized (opModeWrappersLock) {
      opModeWrappers.remove(opModeWrapper);
    }
  }

  // Called from FtcRobotControllerActivity.requestRobotSetup
  public void beforeSetupRobot() {
    if (usbScanTimeInSeconds > 0) {
      try {
        Thread.sleep(usbScanTimeInSeconds * 1000);
      } catch (InterruptedException e) {
      }
    }
  }

  static void activateOpMode(OpMode opMode) {
    activeOpMode = opMode;
    activeHardwareMap = opMode.hardwareMap;

    synchronized (hardwareDevicesLock) {
      for (HardwareDevice hardwareDevice : hardwareDevices) {
        hardwareDevice.initHardwareDevice(opMode);
      }
    }

    synchronized (gamepadDevicesLock) {
      for (GamepadDevice gamepadDevice : gamepadDevices) {
        gamepadDevice.initGamepadDevice(opMode);
      }
    }
  }

  static void deactivateOpMode() {
    activeOpMode = null;
    activeHardwareMap = null;

    synchronized (hardwareDevicesLock) {
      for (HardwareDevice hardwareDevice : hardwareDevices) {
        hardwareDevice.clearHardwareDevice();
      }
    }

    synchronized (gamepadDevicesLock) {
      for (GamepadDevice gamepadDevice : gamepadDevices) {
        gamepadDevice.clearGamepadDevice();
      }
    }
  }

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
      if (!TextUtils.isEmpty(configuration)) {
        if (ftcRobotControllerActivity != null) {
          ftcRobotControllerActivity.onConfigurationPropertyChanged(configuration);
        }
      }
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
   * LIBRARY_VERSION property getter.
   */
  @SimpleProperty(description = "The constant for LIBRARY_VERSION.",
      category = PropertyCategory.BEHAVIOR)
  public String LIBRARY_VERSION() {
    return Version.LIBRARY_VERSION;
  }

  /**
   * BackgroundColor property getter.
   */
  @SimpleProperty(description = "Returns the background color",
      category = PropertyCategory.APPEARANCE)
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * BackgroundColor property setter.
   */
  @SimpleProperty(description = "Specifies the background color.")
  public void BackgroundColor(final int argb) {
    backgroundColor = argb;

    // Update the UI.
    final View relativeLayout = ftcRobotControllerActivity.getRelativeLayout();
    relativeLayout.post(new Runnable() {
      public void run() {
        relativeLayout.setBackgroundColor(argb);
        relativeLayout.invalidate();
      }
    });
  }

  @SimpleFunction(description = "Adds a text data point to the telemetry for the active op mode.")
  public void TelemetryAddTextData(String key, String text) {
    // Copy the activeOpMode field into a local variable so we avoid any race condition caused by
    // another thread setting the activeOpMode field to null before we finish using it.
    OpMode opMode = this.activeOpMode;
    if (opMode != null) {
      try {
        opMode.telemetry.addData(key, text);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "TelemetryAddTextData",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Adds a numeric data point to the telemetry for the active op mode.")
  public void TelemetryAddNumericData(String key, String number) {
    // Copy the activeOpMode field into a local variable so we avoid any race condition caused by
    // another thread setting the activeOpMode field to null before we finish using it.
    OpMode opMode = this.activeOpMode;
    if (opMode != null) {
      // Try to parse the number as a float, but if that fails, fallback to text.
      try {
        opMode.telemetry.addData(key, Float.parseFloat(number));
        return;
      } catch (Throwable e) {
        // Exception is ignored. Fallback to treating number as text.
      }

      try {
        opMode.telemetry.addData(key, number);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "TelemetryAddNumericData",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * TelemetrySorted property setter.
   */
  @SimpleProperty
  public void TelemetrySorted(boolean sorted) {
    // Copy the activeOpMode field into a local variable so we avoid any race condition caused by
    // another thread setting the activeOpMode field to null before we finish using it.
    OpMode opMode = this.activeOpMode;
    if (opMode != null) {
      try {
        opMode.telemetry.setSorted(sorted);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "TelemetrySorted",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * TelemetrySorted property getter.
   */
  @SimpleProperty(description = "Whether the telemetry should be sorted by its keys on the driver station.",
      category = PropertyCategory.BEHAVIOR)
  public boolean TelemetrySorted() {
    // Copy the activeOpMode field into a local variable so we avoid any race condition caused by
    // another thread setting the activeOpMode field to null before we finish using it.
    OpMode opMode = this.activeOpMode;
    if (opMode != null) {
      try {
        return opMode.telemetry.isSorted();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "TelemetrySorted",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return true;
  }

  @SimpleFunction(description = "Clip number if number is less than min or greater than max")
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

  @SimpleFunction(description = "Scale a number in the range of x1 to x2, to the range of y1 to y2")
  public double RangeScale(double number, double x1, double x2, double y1, double y2) {
    try {
      return Range.scale(number, x1, x2, y1, y2);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "RangeScale",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0.0;
  }

  @SimpleFunction(description = "Log information about hardware devices.")
  public void LogDevices() {
    // Copy the activeHardwareMap field into a local variable so we avoid any race condition caused by
    // another thread setting the activeHardwareMap field to null before we finish using it.
    HardwareMap hardwareMap = this.activeHardwareMap;
    if (hardwareMap != null) {
      try {
        hardwareMap.logDevices();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "LogDevices",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Create a byte array.")
  public Object CreateByteArray(int length) {
    if (length > 0) {
      return new byte[length];
    } else {
      form.dispatchErrorOccurredEvent(this, "CreateByteArray",
          ErrorMessages.ERROR_FTC_INVALID_LENGTH, length);
    }
    return new byte[0];
  }

  @SimpleFunction(description = "Returns the length of a byte array.")
  public int LengthOfByteArray(Object byteArray) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] array = (byte[]) byteArray;
        return array.length;
      } else {
        form.dispatchErrorOccurredEvent(this, "LengthOfByteArray",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "LengthOfByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description =
      "Copies one or more bytes from the source byte array to the destination byte array.")
  public void CopyBytes(Object sourceByteArray, int sourcePosition,
      Object destinationByteArray, int destinationPosition, int length) {
    try {
      if (sourceByteArray instanceof byte[]) {
        if (destinationByteArray instanceof byte[]) {
          if (length > 0) {
            byte[] source = (byte[]) sourceByteArray;
            byte[] destination = (byte[]) destinationByteArray;
            if (sourcePosition >= 0 &&
                sourcePosition + length <= source.length) {
              if (destinationPosition >= 0 &&
                  destinationPosition + length <= destination.length) {
                System.arraycopy(source, sourcePosition, destination, destinationPosition, length);
              } else {
                form.dispatchErrorOccurredEvent(this, "CopyBytes",
                    ErrorMessages.ERROR_FTC_INVALID_POSITION, "destinationPosition",
                    destinationPosition);
              }
            } else {
              form.dispatchErrorOccurredEvent(this, "CopyBytes",
                  ErrorMessages.ERROR_FTC_INVALID_POSITION, "sourcePosition", sourcePosition);
            }
          } else {
            form.dispatchErrorOccurredEvent(this, "CopyBytes",
                ErrorMessages.ERROR_FTC_INVALID_LENGTH, length);
          }
        } else {
          form.dispatchErrorOccurredEvent(this, "CopyBytes",
              ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "destinationByteArray");
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "CopyBytes",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "sourceByteArray");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "CopyBytes",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Get a 1-byte number from a byte array.")
  public int Get1ByteNumberFromByteArray(Object byteArray, int position, boolean unsigned) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] source = (byte[]) byteArray;
        if (position >= 0 && position + 1 <= source.length) {
          if (unsigned) {
            return TypeConversion.unsignedByteToInt(source[position]);
          } else {
            return source[position];
          }
        } else {
          form.dispatchErrorOccurredEvent(this, "Get1ByteNumberFromByteArray",
              ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "Get1ByteNumberFromByteArray",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Get1ByteNumberFromByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Get a 2-byte number from a byte array.")
  public short Get2ByteNumberFromByteArray(Object byteArray, int position, boolean bigEndian) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] source = (byte[]) byteArray;
        if (position >= 0 && position + 2 <= source.length) {
          byte[] dest = new byte[2];
          System.arraycopy(source, position, dest, 0, 2);
          return TypeConversion.byteArrayToShort(dest,
              bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        } else {
          form.dispatchErrorOccurredEvent(this, "Get2ByteNumberFromByteArray",
              ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "Get2ByteNumberFromByteArray",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Get2ByteNumberFromByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Get a 4-byte number from a byte array.")
  public int Get4ByteNumberFromByteArray(Object byteArray, int position, boolean bigEndian) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] source = (byte[]) byteArray;
        if (position >= 0 && position + 4 <= source.length) {
          byte[] dest = new byte[4];
          System.arraycopy(source, position, dest, 0, 4);
          return TypeConversion.byteArrayToInt(dest,
              bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        } else {
          form.dispatchErrorOccurredEvent(this, "Get4ByteNumberFromByteArray",
              ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "Get4ByteNumberFromByteArray",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Get4ByteNumberFromByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Get a 8-byte number from a byte array.")
  public long Get8ByteNumberFromByteArray(Object byteArray, int position, boolean bigEndian) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] source = (byte[]) byteArray;
        if (position >= 0 && position + 8 <= source.length) {
          byte[] dest = new byte[8];
          System.arraycopy(source, position, dest, 0, 8);
          return TypeConversion.byteArrayToLong(dest,
              bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        } else {
          form.dispatchErrorOccurredEvent(this, "Get8ByteNumberFromByteArray",
              ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "Get8ByteNumberFromByteArray",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Get8ByteNumberFromByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Put a 1-byte number into a byte array.")
  public void Put1ByteNumberIntoByteArray(String number, Object byteArray, int position) {
    // The number parameter is a String, which allows decimal, hexadecimal, and octal numbers to be
    // given, for example "32", "0x20", or "040".
    try {
      byte b = Byte.decode(number);
      if (byteArray instanceof byte[]) {
        byte[] dest = (byte[]) byteArray;
        if (position >= 0 && position + 1 <= dest.length) {
          dest[position] = b;
        } else {
          form.dispatchErrorOccurredEvent(this, "Put1ByteNumberIntoByteArray",
              ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "Put1ByteNumberIntoByteArray",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
      }
    } catch (NumberFormatException e) {
      form.dispatchErrorOccurredEvent(this, "Put1ByteNumberIntoByteArray",
          ErrorMessages.ERROR_FTC_INVALID_NUMBER, number);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Put1ByteNumberIntoByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Put a 2-byte number into a byte array.")
  public void Put2ByteNumberIntoByteArray(short number, Object byteArray, int position,
      boolean bigEndian) {
    try {
      byte[] source = TypeConversion.shortToByteArray(number,
          bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
      if (byteArray instanceof byte[]) {
        byte[] dest = (byte[]) byteArray;
        if (position >= 0 && position + 2 <= dest.length) {
          System.arraycopy(source, 0, dest, position, 2);
        } else {
          form.dispatchErrorOccurredEvent(this, "Put2ByteNumberIntoByteArray",
              ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "Put2ByteNumberIntoByteArray",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Put2ByteNumberIntoByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Put a 4-byte number into a byte array.")
  public void Put4ByteNumberIntoByteArray(int number, Object byteArray, int position,
      boolean bigEndian) {
    try {
      byte[] source = TypeConversion.intToByteArray(number,
          bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
      if (byteArray instanceof byte[]) {
        byte[] dest = (byte[]) byteArray;
        if (position >= 0 && position + 4 <= dest.length) {
          System.arraycopy(source, 0, dest, position, 4);
        } else {
          form.dispatchErrorOccurredEvent(this, "Put4ByteNumberIntoByteArray",
              ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "Put4ByteNumberIntoByteArray",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Put4ByteNumberIntoByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Put a 8-byte number into a byte array.")
  public void Put8ByteNumberIntoByteArray(long number, Object byteArray, int position,
      boolean bigEndian) {
    try {
      byte[] source = TypeConversion.longToByteArray(number,
          bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
      if (byteArray instanceof byte[]) {
        byte[] dest = (byte[]) byteArray;
        if (position >= 0 && position + 8 <= dest.length) {
          System.arraycopy(source, 0, dest, position, 8);
        } else {
          form.dispatchErrorOccurredEvent(this, "Put8ByteNumberIntoByteArray",
              ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
        }
      } else {
        form.dispatchErrorOccurredEvent(this, "Put8ByteNumberIntoByteArray",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Put8ByteNumberIntoByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Checks the state of a bit in a bit field. " +
      "Returns true if the target bit is one.")
  public boolean IsBitSet(long bitField, int bitPosition) {
    return ((bitField >> bitPosition) & 1) == 1;
  }

  // Components don't normally override Width and Height, but we do it here so that
  // the automatic width and height will be fill parent.
  @Override
  @SimpleProperty()
  public void Width(int width) {
    if (width == LENGTH_PREFERRED) {
      width = LENGTH_FILL_PARENT;
    }
    super.Width(width);
  }

  @Override
  @SimpleProperty()
  public void Height(int height) {
    if (height == LENGTH_PREFERRED) {
      height = LENGTH_FILL_PARENT;
    }
    super.Height(height);
  }

  private void prepareToDie() {
    form.unregisterForActivityResult(this);

    if (wakeLock != null) {
      wakeLock.release();
      wakeLock = null;
    }
  }
}
