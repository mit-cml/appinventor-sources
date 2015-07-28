// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.SdkLevel;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.TypeConversion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FtcRobotController component
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
                 "android.permission.ACCESS_NETWORK_STATE, " +
                 "android.permission.CHANGE_NETWORK_STATE, " +
                 "android.permission.INTERNET, " +
                 "android.permission.WRITE_EXTERNAL_STORAGE, " +
                 "android.permission.READ_EXTERNAL_STORAGE, " +
                 "android.permission.WRITE_SETTINGS, " +
                 "android.permission.WAKE_LOCK")
@UsesLibraries(libraries =
  "FtcAnalytics.jar," +
  "FtcCommon.jar," +
  "FtcModernRobotics.jar," +
  "FtcRobotCore.jar," +
  "FtcWirelessP2p.jar," +
  "d2xx.jar")
public final class FtcRobotController extends AndroidViewComponent implements OnInitializeListener,
    ActivityResultListener, OnNewIntentListener, OnCreateOptionsMenuListener,
    OnOptionsItemSelectedListener, OnDestroyListener, Deleteable, OpModeRegister {

  interface GamepadDevice {
    void initGamepadDevice(Gamepad gamepad1, Gamepad gamepad2);
    void clearGamepadDevice();
  }

  interface HardwareDevice {
    void initHardwareDevice(HardwareMap hardwareMap);
    void clearHardwareDevice();
  }

  interface OpModeWrapper {
    String getOpModeName();
    OpMode getOpMode();
  }

  private static final int DEFAULT_USB_SCAN_TIME_IN_SECONDS = 0;
  private static final String DEFAULT_CONFIGURATION = "";

  private static final AtomicInteger robotControllersCounter = new AtomicInteger(0);
  private static final Object gamepadDevicesLock = new Object();
  private static final List<GamepadDevice> gamepadDevices = Lists.newArrayList();
  private static final Object hardwareDevicesLock = new Object();
  private static final List<HardwareDevice> hardwareDevices = Lists.newArrayList();
  private static final Object opModeWrappersLock = new Object();
  private static final List<OpModeWrapper> opModeWrappers = Lists.newArrayList();
  private static volatile OpMode activeOpMode;

  private final Form form;
  public final LinearLayout view;

  // The request codes for launching other activities.
  public final int requestCodeConfigureRobot;
  public final int requestCodeConfigureWifiChannel;

  // Backing for properties.
  private volatile int usbScanTimeInSeconds = DEFAULT_USB_SCAN_TIME_IN_SECONDS;
  private volatile String configuration = DEFAULT_CONFIGURATION;

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

    robotControllersCounter.incrementAndGet();

    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    int preferredWidth = display.getWidth();
    int preferredHeight = display.getHeight();
    container.setChildWidth(this, preferredWidth);
    container.setChildHeight(this, preferredHeight);

    form.registerForOnInitialize(this);
    requestCodeConfigureRobot = form.registerForActivityResult(this);
    requestCodeConfigureWifiChannel = form.registerForActivityResult(this);
    form.registerForOnNewIntent(this);
    form.registerForOnCreateOptionsMenu(this);
    form.registerForOnOptionsItemSelected(this);
    form.registerForOnDestroy(this);
  }

  // AndroidViewComponent implementation

  @Override
  public View getView() {
    return view;
  }

  // OnInitializeListener implementation

  @Override
  public void onInitialize() {
    Log.e("HeyLiz", "FtcRobotController onInitialize");
    if (robotControllersCounter.get() == 1) {
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

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    prepareToDie();
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    prepareToDie();
    robotControllersCounter.decrementAndGet();
  }

  // OpModeRegister implementation

  @Override
  public void register(OpModeManager opModeManager) {
    synchronized (opModeWrappersLock) {
      for (OpModeWrapper opModeWrapper : opModeWrappers) {
        opModeManager.register(opModeWrapper.getOpModeName(), opModeWrapper.getOpMode());
      }
    }
  }

  /**
   * Adds a {@link GamepadDevice} to the gamepad devices.
   */
  static void addGamepadDevice(GamepadDevice gamepadDevice) {
    Log.e("HeyLiz", "addGamepadDevice gamepadDevice");
    synchronized (gamepadDevicesLock) {
      gamepadDevices.add(gamepadDevice);
    }
  }

  /**
   * Removes a {@link GamepadDevice} from the gamepad devices.
   */
  static void removeGamepadDevice(GamepadDevice gamepadDevice) {
    Log.e("HeyLiz", "removeGamepadDevice gamepadDevice");
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

  // Called before an FtcOpMode's Init event is triggered.
  static void beforeOpModeInit(OpMode opMode) {
    Log.e("HeyLiz", "beforeOpModeInit opMode");
    activeOpMode = opMode;
    synchronized (hardwareDevicesLock) {
      for (HardwareDevice hardwareDevice : hardwareDevices) {
        Log.e("HeyLiz", "beforeOpModeInit initHardwareDevice");
        hardwareDevice.initHardwareDevice(opMode.hardwareMap);
      }
    }
  }

  // Called before an FtcOpMode's Loop event is triggered.
  static void beforeOpModeLoop(OpMode opMode) {
    Log.e("HeyLiz", "beforeOpModeLoop opMode: " + opMode);
    synchronized (gamepadDevicesLock) {
      for (GamepadDevice gamepadDevice : gamepadDevices) {
        Log.e("HeyLiz", "beforeOpModeLoop initGamepadDevice");
        gamepadDevice.initGamepadDevice(opMode.gamepad1, opMode.gamepad2);
      }
    }
  }

  // Called after an FtcOpMode's Stop event is triggered.
  static void afterOpModeStop(OpMode opMode) {
    synchronized (gamepadDevicesLock) {
      for (GamepadDevice gamepadDevice : gamepadDevices) {
        Log.e("HeyLiz", "afterOpModeStop clearGamepadDevice");
        gamepadDevice.clearGamepadDevice();
      }
    }
    synchronized (hardwareDevicesLock) {
      for (HardwareDevice hardwareDevice : hardwareDevices) {
        Log.e("HeyLiz", "afterOpModeStop clearHardwareDevice");
        hardwareDevice.clearHardwareDevice();
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
    // TODO(lizlooney): consider removing this property
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
    // TODO(lizlooney): consider removing this property
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

  @SimpleFunction(description = "Adds a text data point to the telemetry for the active op mode.")
  public void TelemetryAddTextData(String key, String text) {
    OpMode activeOpMode = this.activeOpMode;
    if (activeOpMode != null) {
      try {
        activeOpMode.telemetry.addData(key, text);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "TelemetryAddTextData",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Adds a numeric data point to the telemetry for the active op mode.")
  public void TelemetryAddNumericData(String key, String number) {
    OpMode activeOpMode = this.activeOpMode;
    if (activeOpMode != null) {
      // Try to parse the number as a float, but if that fails, fallback to text.
      try {
        activeOpMode.telemetry.addData(key, Float.parseFloat(number));
        return;
      } catch (Throwable e) {
        // Exception is ignored. Fallback to treating number as text.
      }

      try {
        activeOpMode.telemetry.addData(key, number);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "TelemetryAddNumericData",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
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

  @SimpleFunction(description = "Checks the state of a bit. Returns true if the target bit is true")
  public boolean IsBitSet(long number, int bitPosition) {
    return ((number >> bitPosition) & 1) == 1;
  }

  // TODO(lizlooney): Consider adding support for com.qualcomm.robotcore.uti.RollingAverage

  private void prepareToDie() {
    form.unregisterForActivityResult(this);

    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onStopAI();
    }

    if (wakeLock != null) {
      wakeLock.release();
      wakeLock = null;
    }
  }
}
