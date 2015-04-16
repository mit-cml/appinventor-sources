// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

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
import com.google.appinventor.components.runtime.ftc.Utility;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.SdkLevel;

import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
@UsesLibraries(libraries = "RobotCore.jar,FtcCommon.jar,ModernRobotics.jar,WirelessP2p.jar,d2xx.jar")
public final class FtcRobotController extends AndroidViewComponent implements OnInitializeListener,
    ActivityResultListener, OnNewIntentListener, OnCreateOptionsMenuListener, OnDestroyListener,
    Deleteable, OpModeRegister {

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
  public final LinearLayout entireScreenLayout;
  public final TextView textDeviceName;
  public final LinearLayout headerLayout;
  public final TextView textActiveFilename;
  public final TextView textWifiDirectStatus;
  public final TextView textRobotStatus;
  public final TextView[] textGamepad = new TextView[NUM_GAMEPADS];
  public final TextView textOpMode;
  public final TextView textErrorMessage;

  // The request code for launching other activities.
  private final int requestCode;

  // Backing for properties.
  private volatile int usbScanTimeInSeconds = DEFAULT_USB_SCAN_TIME_IN_SECONDS;
  private volatile String configuration = DEFAULT_CONFIGURATION;

  /*
   * wakeLock, ftcRobotControllerService, and ftcRobotControllerActivity are set in onInitialize,
   * if the device version is Ice Cream Sandwich or later.
   */
  private PowerManager.WakeLock wakeLock;
  private FtcRobotControllerService ftcRobotControllerService;
  private FtcRobotControllerActivity ftcRobotControllerActivity;

  public FtcRobotController(ComponentContainer container) {
    super(container.$form());
    form = container.$form();

    Context context = container.$context();
    entireScreenLayout = new LinearLayout(context);
    textDeviceName = new TextView(context);
    headerLayout = new LinearLayout(context);
    textActiveFilename = new TextView(context);
    textWifiDirectStatus = new TextView(context);
    textRobotStatus = new TextView(context);
    textGamepad[0] = new TextView(context);
    textGamepad[1] = new TextView(context);
    textOpMode = new TextView(context);
    textErrorMessage = new TextView(context);
    initLayout(context);
    
    container.$add(this);

    form.registerForOnInitialize(this);
    requestCode = form.registerForActivityResult(this);
    form.registerForOnNewIntent(this);
    form.registerForOnCreateOptionsMenu(this);
    form.registerForOnDestroy(this);
  }

  // AndroidViewComponent implementation

  @Override
  public View getView() {
    return entireScreenLayout;
  }

  // OnInitializeListener implementation

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
      ftcRobotControllerActivity.onStart();
    } else {
      textErrorMessage.setText("Wi-Fi peer-to-peer connectivity is not supported on this device.");
      form.dispatchErrorOccurredEvent(this, "FtcRobotController",
          ErrorMessages.ERROR_FUNCTIONALITY_NOT_SUPPORTED_WIFI_DIRECT);
    }
  }

  // ActivityResultListener implementation

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode) {
      if (ftcRobotControllerActivity != null) {
        ftcRobotControllerActivity.onActivityResult(requestCode, resultCode, data);
      }
    }
  }

  // OnNewIntentListener implementation

  @Override
  public void onNewIntent(Intent intent) {
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onNewIntent(intent);
    }
  }

  // OnCreateOptionsMenuListener implementation

  @Override
  public void onCreateOptionsMenu(Menu menu) {
    MenuItem restartRobot = menu.add(Menu.NONE, Menu.NONE, Menu.FIRST, "Restart Robot")
        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (ftcRobotControllerActivity != null) {
          ftcRobotControllerActivity.restartRobot();
        }
        return true;
      }
    });
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

  // Called from FtcFtcRobotControllerService
  public int getUsbScanTimeInSeconds() {
    return usbScanTimeInSeconds;
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
      if (!TextUtils.isEmpty(configuration)) {
        Utility utility = new Utility(form);
        utility.saveToPreferences(configuration,
            FtcRobotControllerActivity.PREF_HARDWARE_CONFIG_FILENAME_KEY);
      }
      if (ftcRobotControllerActivity != null) {
        ftcRobotControllerActivity.restartRobot();
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

  // TODO(lizlooney): Consider adding support for other com.qualcomm.robotcore.util classes:
  // BatteryChecker, CurvedWheelMotion, RollingAverage

  private void initLayout(Context context) {
    entireScreenLayout.setOrientation(LinearLayout.VERTICAL);
    LinearLayout deviceNameLayout = new LinearLayout(context);
    deviceNameLayout.setOrientation(LinearLayout.HORIZONTAL);
    deviceNameLayout.setBackgroundColor(COLOR_DEVICE_NAME);

    TextView label = new TextView(context);
    label.setBackgroundColor(COLOR_TRANSPARENT);
    label.setTextColor(COLOR_BLACK);
    label.setText("Device Name:");
    deviceNameLayout.addView(label,
        new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1));
    textDeviceName.setBackgroundColor(COLOR_TRANSPARENT);
    textDeviceName.setTextColor(COLOR_BLACK);
    deviceNameLayout.addView(textDeviceName,
        new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    entireScreenLayout.addView(deviceNameLayout,
        new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

    headerLayout.setOrientation(LinearLayout.HORIZONTAL);
    headerLayout.setBackgroundColor(COLOR_HEADER);
    label = new TextView(context);
    label.setBackgroundColor(COLOR_TRANSPARENT);
    label.setTextColor(COLOR_BLACK);
    label.setText("Active Configuration File:");
    headerLayout.addView(label,
        new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1));
    textActiveFilename.setBackgroundColor(COLOR_TRANSPARENT);
    textActiveFilename.setTextColor(COLOR_BLACK);
    headerLayout.addView(textActiveFilename,
        new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    entireScreenLayout.addView(headerLayout,
        new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    textWifiDirectStatus.setBackgroundColor(COLOR_TRANSPARENT);
    textWifiDirectStatus.setTextColor(COLOR_BLACK);
    entireScreenLayout.addView(textWifiDirectStatus,
        new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    textRobotStatus.setBackgroundColor(COLOR_TRANSPARENT);
    textRobotStatus.setTextColor(COLOR_BLACK);
    entireScreenLayout.addView(textRobotStatus,
        new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    textOpMode.setBackgroundColor(COLOR_TRANSPARENT);
    textOpMode.setTextColor(COLOR_BLACK);
    entireScreenLayout.addView(textOpMode,
        new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    textErrorMessage.setBackgroundColor(COLOR_TRANSPARENT);
    textErrorMessage.setTextColor(COLOR_ERROR);
    textErrorMessage.setTypeface(
        Typeface.create(textErrorMessage.getTypeface(), Typeface.BOLD));
    textErrorMessage.setMinLines(2);
    textErrorMessage.setMaxLines(4);
    entireScreenLayout.addView(textErrorMessage,
        new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    // Add a spacer that takes the full width and all the remaining weight.
    entireScreenLayout.addView(new TextView(context),
        new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1));
    textGamepad[0].setBackgroundColor(COLOR_TRANSPARENT);
    textGamepad[0].setTextColor(COLOR_BLACK);
    entireScreenLayout.addView(textGamepad[0],
        new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    textGamepad[1].setBackgroundColor(COLOR_TRANSPARENT);
    textGamepad[1].setTextColor(COLOR_BLACK);
    entireScreenLayout.addView(textGamepad[1],
        new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
  }

  private void prepareToDie() {
    form.unregisterForActivityResult(this);

    if (ftcRobotControllerService != null) {
      ftcRobotControllerService.onUnbind();
    }
    if (ftcRobotControllerActivity != null) {
      ftcRobotControllerActivity.onStop();
    }

    if (wakeLock != null) {
      wakeLock.release();
      wakeLock = null;
    }
  }
}
