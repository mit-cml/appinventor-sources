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
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.SdkLevel;

import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.hitechnic.HiTechnicHardwareFactory;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
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
import com.qualcomm.robotcore.util.Util;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant.ConnectStatus;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant.WifiDirectAssistantCallback;

import android.content.Context;
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
    implements Component, OnInitializeListener, OnDestroyListener, Deleteable,
    WifiDirectAssistantCallback, EventLoopManager.EventLoopMonitor, EventLoop, OpModeRegister {

  interface HardwareDevice {
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
  private static final String DEFAULT_CONFIG_FILENAME = "robot_config";

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
  private volatile String configFilename = DEFAULT_CONFIG_FILENAME;

  /*
   * wakeLock is set in onInitialize, if the device version is Ice Cream Sandwich or later.
   */
  private PowerManager.WakeLock wakeLock;

  /*
   * wifiDirectAssistant is set in onInitialize, if the device version is Ice Cream Sandwich
   * or later.
   */
  private volatile WifiDirectAssistant wifiDirectAssistant;

  private volatile OpModeManager opModeManager;

  /*
   * robotSetupThread is created in startRobotSetup, which is called from onInitialize, if the
   * device version is Ice Cream Sandwich or later.
   */
  private volatile Thread robotSetupThread;

  /*
   * hardwareFactory is created by the robotSetupThread.
   */
  private volatile HardwareFactory hardwareFactory;

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

      wifiDirectAssistant = WifiDirectAssistant.getWifiDirectAssistant(form);
      wifiDirectAssistant.setCallback(this);
      wifiDirectAssistant.disable(); // TODO(lizlooney): Is this really needed?
      wifiDirectAssistant.enable();
      wifiDirectAssistant.discoverPeers();
      startRobotSetup();
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

  // Properties

  /**
   * ConfigFilename property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The name of the robot configuration file.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String ConfigFilename() {
    return configFilename;
  }

  /**
   * ConfigFilename property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = DEFAULT_CONFIG_FILENAME)
  @SimpleProperty(userVisible = false)
  public void ConfigFilename(String configFilename) {
    this.configFilename = configFilename;
    if (wifiDirectAssistant != null) {
      startRobotSetup();
    }
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
    if (wifiDirectAssistant != null && wifiDirectAssistant.isConnected()) {
      wifiDirectAssistant.disable();
    }

    this.driverStationAddress = driverStationAddress;

    if (wifiDirectAssistant != null) {
      wifiDirectAssistant.enable();
      wifiDirectAssistant.discoverPeers();
      startRobotSetup();
    }
  }

  // Functions

  @SimpleFunction(description = "RangeClip")
  public double RangeClip(double number, double min, double max) {
    return Range.clip(number, min, max);
  }

  // Events

  @SimpleEvent(description = "WifiDirectUpdate event")
  public void WifiDirectUpdate(String event) {
    EventDispatcher.dispatchEvent(this, "WifiDirectUpdate", event);
  }

  @SimpleEvent(description = "RobotUpdate event")
  public void RobotUpdate(String status) {
    EventDispatcher.dispatchEvent(this, "RobotUpdate", status);
  }

  @SimpleEvent(description = "RobotReady event")
  public void RobotReady() {
    EventDispatcher.dispatchEvent(this, "RobotReady");
  }

  // ....begin debugging code
  @SimpleEvent(description = "Info event")
  public void Info(String message) {
    EventDispatcher.dispatchEvent(this, "Info", message);
  }
  // ....end debugging code

  // WifiDirectAssistantCallback implementation

  @Override
  public void onWifiDirectEvent(WifiDirectAssistant.Event event) {
    switch (event) {
      case PEERS_AVAILABLE:
        if (wifiDirectAssistant.getConnectStatus() == ConnectStatus.CONNECTING ||
            wifiDirectAssistant.getConnectStatus() == ConnectStatus.CONNECTED) {
          /*
           * We get extra an extra PEER_AVAILABLE event when first connecting, and right after
           * the connection is complete. Just ignore these events.
           */
          return;
        }
        connectToDriverStation();
        break;
      case CONNECTING:
      case CONNECTED_AS_PEER:
        wifiDirectAssistant.cancelDiscoverPeers();
        break;
      case DISCONNECTED:
        wifiDirectAssistant.discoverPeers();
        break;
      case ERROR:
        String reason = wifiDirectAssistant.getFailureReason();
        Log.e("FtcRobotController", "Wifi Direct Error: " + reason);
        form.dispatchErrorOccurredEvent(this, "",
            ErrorMessages.ERROR_FTC_WIFI_DIRECT_ERROR, reason);
        break;
    }

    triggerWifiDirectUpdateEvent(event);
  }

  // EventLoopManager.EventLoopMonitor implementation
  
  @Override
  public void onStateChange(EventLoopManager.State state) {
    triggerRobotUpdateEvent(state.toString());
  }

  // EventLoop implementation

  @Override
  public void init(EventLoopManager eventLoopManager) throws RobotCoreException, InterruptedException {
    this.eventLoopManager = eventLoopManager;

    HardwareMap hardwareMap = hardwareFactory.createHardwareMap(eventLoopManager);
    opModeManager.setHardwareMap(hardwareMap);

    // Initialize each hardware device component.
    synchronized (hardwareDevicesLock) {
      List<HardwareDevice> hardwareDevicesForForm = hardwareDevices.get(form);
      if (hardwareDevicesForForm != null) {
        for (HardwareDevice hardwareDevice : hardwareDevicesForForm) {
          hardwareDevice.setHardwareMap(hardwareMap);
        }
      }
    }

    synchronized (gamepadDevicesLock) {
      List<GamepadDevice> gamepadDevicesForForm = gamepadDevices.get(form);
      if (gamepadDevicesForForm != null) {
        for (GamepadDevice gamepadDevice : gamepadDevicesForForm) {
          gamepadDevice.setEventLoopManager(eventLoopManager);
        }
      }
    }
  }

  @Override
  public void loop() throws RobotCoreException {
    opModeManager.runActiveOpMode(eventLoopManager.getGamepads());

    // Send telemetry data.
    if (telemetryTimer.time() > telemetryInterval) {
      telemetryTimer.reset();
      Telemetry telemetry = opModeManager.getActiveOpMode().telemetry;

      if (telemetry.hasData()) {
        eventLoopManager.sendTelemetryData(telemetry);
      }
      telemetry.clearData();
    }
  }

  @Override
  public void teardown() throws RobotCoreException {
    opModeManager.stopActiveOpMode();

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

    HardwareMap hardwareMap = opModeManager.getHardwareMap();

    // Power down and close the DC motor controllers.
    for (DcMotorController dcMotorController : hardwareMap.dcMotorController.values()) {
      dcMotorController.close();
    }

    // Power down and close the servo controllers.
    for (ServoController servoController : hardwareMap.servoController.values()) {
      servoController.pwmDisable();
      servoController.close();
    }

    // Power down and close the legacy modules.
    // This should be after the servo and motor controllers, since some of them
    // may be connected through this device.
    for (LegacyModule legacyModule : hardwareMap.legacyModule.values()) {
      legacyModule.close();
    }
  }

  @Override
  public void processCommand(Command command) {
    String commandName = command.getName();
    if (commandName.equals(CommandList.CMD_RESTART_ROBOT)) {
      handleCommandRestartRobot();
    } else if (commandName.equals(CommandList.CMD_REQUEST_OP_MODE_LIST)) {
      handleCommandRequestOpModeList();
    } else if (commandName.equals(CommandList.CMD_SWITCH_OP_MODE)) {
      handleCommandSwitchOpMode(command.getExtra());
    }
  }

  private void handleCommandRestartRobot() {
    startRobotSetup();
  }

  private void handleCommandRequestOpModeList() {
    StringBuilder sb = new StringBuilder();
    String delimiter = "";
    for (String opModeName : opModeManager.getOpModes()) {
      sb.append(delimiter).append(opModeName);
      delimiter = Util.ASCII_RECORD_SEPARATOR;
    }
    String opModeList = sb.toString();
    eventLoopManager.sendCommand(new Command(CommandList.CMD_REQUEST_OP_MODE_LIST_RESP, opModeList));
  }

  private void handleCommandSwitchOpMode(String extra) {
    opModeManager.switchOpModes(extra);
    eventLoopManager.sendCommand(new Command(CommandList.CMD_SWITCH_OP_MODE_RESP,
        opModeManager.getActiveOpModeName()));
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

  // private methods

  private void triggerWifiDirectUpdateEvent(final WifiDirectAssistant.Event event) {
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        WifiDirectUpdate(event.toString());
      }
    });
  }

  private void triggerRobotUpdateEvent(final String state) {
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        RobotUpdate(state);
      }
    });
  }

  private void triggerRobotReadyEvent() {
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        RobotReady();
      }
    });
  }

  // ....begin debugging code
  private void triggerInfoEvent(final String message) {
    System.out.println(System.currentTimeMillis() + " HeyLiz InfoEvent " + message);
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Info(message);
      }
    });
  }
  // ....end debugging code

  private void connectToDriverStation() {
    for (WifiP2pDevice peer : wifiDirectAssistant.getPeers()) {
      if (peer.deviceAddress.equalsIgnoreCase(driverStationAddress)) {
        triggerInfoEvent("Before wifiDirectAssistant.connect");
        wifiDirectAssistant.connect(peer);
        triggerInfoEvent("After wifiDirectAssistant.connect");
        return;
      }
    }
    form.dispatchErrorOccurredEvent(FtcRobotController.this, "DriverStationAddress",
        ErrorMessages.ERROR_FTC_DRIVER_STATION_NOT_FOUND);
  }

  private synchronized void startRobotSetup() {
    // If the robot setup thread is already running, interrupt it and wait for it to die.
    stopRobotSetup();

    triggerInfoEvent("startRobotSetup - Creating new robotSetupThread.");
    robotSetupThread = new Thread(new Runnable() {
      @Override
      public void run() {
        // Shutdown the previous robot.
        if (robot != null) {
          robot.shutdown();
          robot = null;
        }

        long startTime = System.currentTimeMillis();

        String filename = CONFIG_FILES_DIR + configFilename + CONFIG_FILE_EXT;
        FileInputStream fis;
        try {
          fis = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
          triggerInfoEvent("Could not find config file " + filename);
          form.dispatchErrorOccurredEvent(FtcRobotController.this, "",
              ErrorMessages.ERROR_FTC_CONFIG_FILE_NOT_FOUND, filename);
          return;
        }

        HiTechnicHardwareFactory hitechnicFactory = new HiTechnicHardwareFactory(form);
        hitechnicFactory.setXmlInputStream(fis);
        hardwareFactory = hitechnicFactory;

        // If the driver station is not connected yet, wait for it.
        while (!wifiDirectAssistant.isConnected()) {
          triggerInfoEvent("Driver station is not connected yet");
          long elapsedTime = System.currentTimeMillis() - startTime;
          if (elapsedTime > WIFI_DIRECT_TIMEOUT_MILLIS) {
            triggerInfoEvent("After waiting " + WIFI_DIRECT_TIMEOUT_MILLIS + "ms, driver station is not connected");
            form.dispatchErrorOccurredEvent(FtcRobotController.this, "",
                ErrorMessages.ERROR_FTC_TIMEOUT_WHILE_CONNECTING_TO_DRIVER_STATION);
            return;
          }
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // we received an interrupt, abort
            triggerInfoEvent("Abort due to interrupt");
            return;
          }
        }
        triggerInfoEvent("Driver station is connected.");

        // Continue waiting for USB devices to be scanned.
        // TODO: does this have to be done for restarts or only the first time?
        triggerInfoEvent("Waiting for USB devices to be scanned");
        /*
         * Give android a chance to finish scanning for USB devices before
         * we create our robot object.
         *
         * It take Android up to ~300ms per USB device plugged into a hub.
         * Higher quality hubs take less time.
         */
        while (System.currentTimeMillis() - startTime < USB_SCAN_WAITTIME_MILLIS) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // we received an interrupt, abort
            triggerInfoEvent("Abort due to interrupt");
            return;
          }
        }

        // Create the robot.
        try {
          triggerInfoEvent("Before RobotFactory.createRobot");
          robot = RobotFactory.createRobot();
          triggerInfoEvent("After RobotFactory.createRobot");
        } catch (RobotCoreException e) {
          triggerInfoEvent("Unable to create robot! " + e);
          form.dispatchErrorOccurredEvent(FtcRobotController.this, "",
              ErrorMessages.ERROR_FTC_FAILED_TO_CREATE_ROBOT);
          return;
        }

        // Start the robot.
        EventLoopManager.EventLoopMonitor eventLoopMonitor = FtcRobotController.this;
        robot.eventLoopManager.setMonitor(eventLoopMonitor);

        try {
          InetAddress addr = wifiDirectAssistant.getGroupOwnerAddress();
          EventLoop eventLoop = FtcRobotController.this;
          triggerInfoEvent("Before robot.start");
          robot.start(addr, addr, eventLoop);
          triggerInfoEvent("After robot.start");
        } catch (RobotCoreException e) {
          triggerInfoEvent("Failed to start robot " + e);
          form.dispatchErrorOccurredEvent(FtcRobotController.this, "",
              ErrorMessages.ERROR_FTC_FAILED_TO_START_ROBOT);
          return;
        }

        triggerRobotReadyEvent();
      }
    });
    robotSetupThread.start();
  }

  private synchronized void stopRobotSetup() {
    if (robotSetupThread != null && robotSetupThread.isAlive()) {
      triggerInfoEvent("stopRobotSetup - Old robotSetupThread is running. Interrupting it.");
      robotSetupThread.interrupt();
      triggerInfoEvent("stopRobotSetup - Waiting for old robotSetupThread to die.");
      try {
        robotSetupThread.join();
        triggerInfoEvent("stopRobotSetup - Old robotSetupThread is dead.");
      } catch (InterruptedException e) {
        triggerInfoEvent("stopRobotSetup - Interrupted while waiting for old robotSetupThread to die.");
        Thread.currentThread().interrupt();
      }
    }
  }

  private void prepareToDie() {
    stopRobotSetup();
    if (robot != null) {
      robot.shutdown();
      robot = null;
    }

    if (wifiDirectAssistant != null) {
      wifiDirectAssistant.disable();
      wifiDirectAssistant = null;
    }

    wakeLock.release();
    wakeLock = null;
  }
}
