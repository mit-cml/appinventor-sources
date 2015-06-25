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

import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;

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
  private static final String DEFAULT_CONFIGURATION = "";

  private static final int NUM_GAMEPADS = 2;
  private static final int COLOR_TRANSPARENT = 0x00FFFFFF;
  private static final int COLOR_BLACK = 0xFF000000;
  private static final int COLOR_DEVICE_NAME = 0xFFC1E2E4;
  private static final int COLOR_HEADER = 0xFF309EA4;
  private static final int COLOR_ERROR = 0xFF990000;

  private static final Map<Form, List<HardwareDevice>> hardwareDevices = Maps.newHashMap();
  private static final Object hardwareDevicesLock = new Object();
  private static final Map<Form, List<GamepadDevice>> gamepadDevices = Maps.newHashMap();
  private static final Object gamepadDevicesLock = new Object();
  private static final Map<Form, List<OpModeWrapper>> opModeWrappers = Maps.newHashMap();
  private static final Object opModeWrappersLock = new Object();

  private final Form form;
  public final LinearLayout view;

  // The request codes for launching other activities.
  public final int requestCodeConfigureRobot;

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

    form.registerForOnInitialize(this);
    requestCodeConfigureRobot = form.registerForActivityResult(this);
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
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ICE_CREAM_SANDWICH) {
      PowerManager powerManager = (PowerManager) form.getSystemService(Context.POWER_SERVICE);
      wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FtcRoboController");
      wakeLock.acquire();

      ftcRobotControllerActivity = new FtcRobotControllerActivity(this, form, configuration);
    } else {
      form.dispatchErrorOccurredEvent(this, "FtcRobotController",
          ErrorMessages.ERROR_FUNCTIONALITY_NOT_SUPPORTED_WIFI_DIRECT);
    }
  }

  // ActivityResultListener implementation

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == requestCodeConfigureRobot) {
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
      // TODO(lizlooney): if onEventLoopInit has already been called, we should call
      // hardwareDevice.setHardwareMap() now.
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
      // TODO(lizlooney): if onEventLoopInit has already been called, we should call
      // gamepadDevice.setEventLoopManager() now.
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
      // TODO(lizlooney): if register has already been called, we should call
      // opModeManager.register() now.
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

  // Called from FtcRobotControllerActivity.requestRobotSetup
  public void beforeSetupRobot() {
    if (usbScanTimeInSeconds > 2) {
      try {
        Thread.sleep((usbScanTimeInSeconds - 2) * 1000);
      } catch (InterruptedException e) {
      }
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

  // Functions

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

  @SimpleFunction(description = "Convert a 1-byte number to a byte array.")
  public Object Convert1ByteNumberToByteArray(String number) {
    // The number parameter is a String, which allows decimal, hexadecimal, and octal numbers to be
    // given, for example "32", "0x20", or "040".
    try {
      return new byte[] { Byte.decode(number) };
    } catch (NumberFormatException e) {
      form.dispatchErrorOccurredEvent(this, "Convert1ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_INVALID_NUMBER, number);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Convert1ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new byte[] { 0 };
  }

  @SimpleFunction(description = "Convert a 2-byte number to a byte array.")
  public Object Convert2ByteNumberToByteArray(short number, boolean bigEndian) {
    try {
      return TypeConversion.shortToByteArray(number,
          bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Convert2ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new byte[] { 0, 0 };
  }

  @SimpleFunction(description = "Convert a 4-byte number to a byte array.")
  public Object Convert4ByteNumberToByteArray(int number, boolean bigEndian) {
    try {
      return TypeConversion.intToByteArray(number,
          bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Convert4ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new byte[] { 0, 0, 0, 0 };
  }

  @SimpleFunction(description = "Convert a 8-byte number to a byte array.")
  public Object Convert8ByteNumberToByteArray(long number, boolean bigEndian) {
    try {
      return TypeConversion.longToByteArray(number,
          bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Convert8ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
  }

  @SimpleFunction(description = "Convert a 1-byte array to an unsigned number.")
  public int ConvertByteArrayToUnsigned1ByteNumber(Object byteArray) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 1) {
          return TypeConversion.unsignedByteToInt(b[0]);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayToUnsigned1ByteNumber",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayToUnsigned1ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert a 1-byte array to a signed number.")
  public int ConvertByteArrayToSigned1ByteNumber(Object byteArray) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 1) {
          return b[0];
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayToSigned1ByteNumber",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayToSigned1ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }


  @SimpleFunction(description = "Convert a 2-byte array to a number.")
  public short ConvertByteArrayTo2ByteNumber(Object byteArray, boolean bigEndian) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 2) {
          return TypeConversion.byteArrayToShort(b,
              bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo2ByteNumber",
          ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo2ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert a 4-byte array to a number.")
  public int ConvertByteArrayTo4ByteNumber(Object byteArray, boolean bigEndian) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 4) {
          return TypeConversion.byteArrayToInt(b,
              bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo4ByteNumber",
          ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo4ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert a 8-byte array to a number.")
  public long ConvertByteArrayTo8ByteNumber(Object byteArray, boolean bigEndian) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 8) {
          return TypeConversion.byteArrayToLong(b,
              bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo8ByteNumber",
          ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo8ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
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
