// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.SdkLevel;

import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.hitechnic.HiTechnicDeviceManager;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.factory.RobotFactory;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant.ConnectStatus;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant.WifiDirectAssistantCallback;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.PowerManager;
import android.util.Log;

import java.net.InetAddress;
import java.util.List;
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
@UsesLibraries(libraries = "ftccommon.jar,hitechnic.jar,robotcore.jar,wirelessp2p.jar,d2xx.jar")
public final class FtcRobotController extends AndroidNonvisibleComponent 
    implements Component, OnInitializeListener, OnDestroyListener, Deleteable,
    WifiDirectAssistantCallback, EventLoopManager.EventLoopMonitor, EventLoop {

  interface Child {
    void createChild();
    void debugChild(StringBuilder sb);
    void destroyChild();
  }

  private static final long USB_SCAN_WAITTIME_MILLIS = 30 * 1000L; // 30 seconds
  private static final long WIFI_DIRECT_TIMEOUT_MILLIS = 2 * 60 * 1000L; // 2 minutes

  private volatile String driverStationAddress = "";
  private final List<Child> children = Lists.newArrayList();
  private final List<FtcOpMode> ftcOpModes = Lists.newArrayList();
  private final AtomicBoolean isInitialized = new AtomicBoolean(false);

  /*
   * wakeLock is set in onInitialize, if the device version is Ice Cream Sandwich or later.
   */
  private PowerManager.WakeLock wakeLock;

  /*
   * wifiDirectAssistant is set in onInitialize, if the device version is Ice Cream Sandwich
   * or later.
   */
  private volatile WifiDirectAssistant wifiDirectAssistant;

  /*
   * robotSetupThread is created in startRobotSetup, which is called from onInitialize, if the
   * device version is Ice Cream Sandwich or later.
   */
  private volatile Thread robotSetupThread;

  /*
   * robot is created by the robotSetupThread, after the driver station is connected.
   */
  private volatile Robot robot;

  /*
   * eventLoopManager and deviceMgr are set in init, which is called after the robotSetupThread
   * calls robot.start().
   */
  private volatile EventLoopManager eventLoopManager;
  /*
   * deviceMgr is set in init
   */
  private volatile HiTechnicDeviceManager deviceMgr;

  // TODO(4.0): remove code begin
  private volatile FtcOpMode activeOpMode;
  // TODO(4.0): remove code end

  public FtcRobotController(ComponentContainer container) {
    super(container.$form());
    form.registerForOnInitialize(this);
    form.registerForOnDestroy(this);
  }

  @Override
  public void onInitialize() {
    System.out.println(System.currentTimeMillis() + " HeyLiz - onInitialize................................................................................");
    isInitialized.set(true);

    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ICE_CREAM_SANDWICH) {
      PowerManager powerManager = (PowerManager) form.getSystemService(Context.POWER_SERVICE);
      wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FtcRoboController");
      wakeLock.acquire();

      wifiDirectAssistant = WifiDirectAssistant.getWifiDirectAssistant(form);
      wifiDirectAssistant.setCallback(this);
      wifiDirectAssistant.disable();
      wifiDirectAssistant.enable();
      wifiDirectAssistant.discoverPeers();
      startRobotSetup();
    }
  }

  EventLoopManager getEventLoopManager() {
    return eventLoopManager;
  }

  HiTechnicDeviceManager getHiTechnicDeviceManager() {
    return deviceMgr;
  }

  boolean isAfterEventLoopInit() {
    return eventLoopManager != null;
  }

  /**
   * Adds a {@link Child} to the children list.
   */
  void addChild(Child child) {
    children.add(child);
  }

  /**
   * Removes a {@link Child} from the children list.
   */
  void removeChild(Child child) {
    children.remove(child);
  }

  /**
   * Adds an {@link FtcOpMode} to the ftcOpModes list.
   *
   * @param ftcOpMode  the {@code FtcOpMode} to be added
   */
  void addFtcOpMode(FtcOpMode ftcOpMode) {
    // TODO(4.0)
    ftcOpModes.add(ftcOpMode);
  }

  /**
   * Removes an {@link FtcOpMode} from the ftcOpModes list.
   *
   * @param ftcOpMode  the {@code FtcOpMode} to be removed
   */
  void removeFtcOpMode(FtcOpMode ftcOpMode) {
    ftcOpModes.remove(ftcOpMode);
  }

  // Properties

  /**
   * DriverStationAddress property getter method.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The address of the driver station.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String DriverStationAddress() {
    return driverStationAddress;
  }

  /**
   * DriverStationAddress property setter method.
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
    System.out.println(System.currentTimeMillis() + " HeyLiz - driverStationAddress is " + driverStationAddress);

    if (wifiDirectAssistant != null && isInitialized.get()) {
      wifiDirectAssistant.enable();
      wifiDirectAssistant.discoverPeers();
      startRobotSetup();
    }
  }

  /**
   * Returns the list of paired wifi direct peers. Each element of the returned
   * list is a String consisting of the peer's address, a space, and the
   * peer's name.
   *
   * @return a List representing the addresses and names of wifi direct peers.
   */
  @SimpleProperty(description = "The addresses and names of wifi direct peers",
      category = PropertyCategory.BEHAVIOR)
  public List<String> AddressesAndNames() {
    List<String> addressesAndNames = Lists.newArrayList();
    if (wifiDirectAssistant != null) {
      for (WifiP2pDevice peer : wifiDirectAssistant.getPeers()) {
        addressesAndNames.add(peer.deviceAddress + " " + peer.deviceName);
      }
    }
    return addressesAndNames;
  }

  // TODO(4.0): remove code begin
  /**
   * ActiveOpMode property getter.
   */
  @SimpleProperty(description = "The active FtcOpMode component",
      category = PropertyCategory.BEHAVIOR)
  public FtcOpMode ActiveOpMode() {
    return activeOpMode;
  }

  /**
   * ActiveOpMode property setter.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_OP_MODE,
      defaultValue = "")
  @SimpleProperty
  public void ActiveOpMode(FtcOpMode activeOpMode) {
    if (this.activeOpMode != null && isAfterEventLoopInit()) {
      this.activeOpMode.triggerStopEvent();
    }

    this.activeOpMode = activeOpMode;
    System.out.println(System.currentTimeMillis() + " HeyLiz - activeOpMode is " + activeOpMode);

    if (this.activeOpMode != null && isAfterEventLoopInit()) {
      this.activeOpMode.triggerStartEvent();
    }
  }
  // TODO(4.0): remove code end

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

  // private

  private void triggerWifiDirectUpdateEvent(final WifiDirectAssistant.Event event) {
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        WifiDirectUpdate(event.toString());
      }
    });
  }

  private void triggerRobotUpdateEvent(final EventLoopManager.EventLoopMonitor.State state) {
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        RobotUpdate(state.toString());
      }
    });
  }

  private void triggerRobotReadyEvent() {
    System.out.println(System.currentTimeMillis() + " HeyLiz - robot ready");
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
    // Truncate the driver station address at the first space.
    // This allows the address to be an element from the AddressesAndNames property.
    int firstSpace = driverStationAddress.indexOf(" ");
    String address = (firstSpace != -1)
        ? driverStationAddress.substring(0, firstSpace)
        : driverStationAddress;

    for (WifiP2pDevice peer : wifiDirectAssistant.getPeers()) {
      if (peer.deviceAddress.equalsIgnoreCase(address)) {
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

  // EventLoopMonitor implementation
  
  @Override
  public void onStateChange(EventLoopManager.EventLoopMonitor.State state) {
    triggerRobotUpdateEvent(state);
  }

  // EventLoop implementation

  @Override
  public void init(EventLoopManager eventLoopManager) throws RobotCoreException {
    this.eventLoopManager = eventLoopManager;

    // TODO(4.0): add code
    /*
    // reset the hardware mappings
    hardwareMap = hardwareFactory.createHardwareMap(eventLoopManager);

    // Start up the op mode manager
    opModeManager.setHardwareMap(hardwareMap);
    */

    deviceMgr = new HiTechnicDeviceManager(form, eventLoopManager);

    for (Child child : children) {
      child.createChild();
    }

    StringBuilder sb = new StringBuilder();
    for (Child child : children) {
      child.debugChild(sb);
    }
    triggerInfoEvent(sb.toString());

    // TODO(4.0): remove code begin
    if (activeOpMode != null) {
      activeOpMode.triggerStartEvent();
    }
    // TODO(4.0): remove code end
  }

  @Override
  public void loop() throws RobotCoreException {
    // TODO(4.0): add code
    /*
    Gamepad gamepads[] = eventLoopManager.getGamepads();
    opModeManager.runActiveOpMode(gamepads);
    */
    // TODO(4.0): remove code begin
    if (activeOpMode != null) {
      activeOpMode.triggerRunEvent();
    }
    // TODO(4.0): remove code end
  }

  @Override
  public void teardown() throws RobotCoreException {
    // TODO(4.0): add code
    /*
    opModeManager.stopActiveOpMode();
    */
    // TODO(4.0): remove code begin
    if (activeOpMode != null) {
      activeOpMode.triggerStopEvent();
    }
    // TODO(4.0): remove code end

    for (Child child : children) {
      child.destroyChild();
    }
  }

  @Override
  // TODO(4.0): add code
  /*
  public void processCommand(Command command) {
    String commandName = command.getName();
  */
  // TODO(4.0): remove code begin
  public void processCommand(String commandName) {
  // TODO(4.0): remove code end
    if (commandName.equals(CommandList.CMD_RESTART_ROBOT)) {
      startRobotSetup();
    // TODO(4.0): add code
    /*
    } else if (commandName.equals(CommandList.CMD_REQUEST_OP_MODE_LIST)) {
      handleCommandRequestOpModeList();
    } else if (commandName.equals(CommandList.CMD_SWITCH_OP_MODE)) {
      handleCommandSwitchOpMode(command.getExtra());
    */
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
