// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.ftc;

import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.LegacyModule;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.Telemetry;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Util;

import static java.util.Map.Entry;

/**
 * Main event loop to control robot
 */
public class FtcEventLoop implements EventLoop {

  // Event loop manager
  EventLoopManager eventLoopManager;

  // Telemetry
  ElapsedTime telemetryTimer = new ElapsedTime();
  double telemetryInterval = 0.250; // in seconds

  private final FtcRobotControllerA.Callback callback;

  // Hardware Factory and Map
  private final HardwareFactory hardwareFactory;
  HardwareMap hardwareMap = new HardwareMap();

  private final OpModeManager opModeManager;

  // Gamepad UI Timer
  ElapsedTime updateGamepadUi = new ElapsedTime();

  FtcEventLoop(HardwareFactory hardwareFactory, FtcRobotControllerA.Callback callback,
      OpModeRegister opModeRegister) {
    this.hardwareFactory = hardwareFactory;
    this.callback = callback;
    opModeManager = new OpModeManager(hardwareMap, opModeRegister);
  }

  /**
   * Init method
   * <p>
   * This code will run when the robot first starts up. Place any initialization code in this
   * method.
   * <p>
   * It is important to save a copy of the event loop manager from this method, as that is how
   * you'll get access to the gamepad.
   * <p>
   * If an Exception is thrown then the event loop manager will not start the robot.
   * <p>
   * @see com.qualcomm.robotcore.eventloop.EventLoop#init(com.qualcomm.robotcore.eventloop.EventLoopManager)
   */
  @Override
  public void init(EventLoopManager eventLoopManager) throws RobotCoreException, InterruptedException {
    DbgLog.msg("======= INIT START =======");
    this.eventLoopManager = eventLoopManager;

    // reset the hardware mappings
    hardwareMap = hardwareFactory.createHardwareMap(eventLoopManager);

    // Start up the op mode manager
    opModeManager.setHardwareMap(hardwareMap);

    DbgLog.msg("======= INIT FINISH =======");
  }

  /**
   * Loop method, this will be called repeatedly while the robot is running.
   * <p>
   * @see com.qualcomm.robotcore.eventloop.EventLoop#loop()
   */
  @Override
  public void loop() throws RobotCoreException {

    // Get access to gamepad 1 and 2
    Gamepad gamepads[] = eventLoopManager.getGamepads();
    callback.updateUi(opModeManager.getActiveOpModeName(), gamepads);

    opModeManager.runActiveOpMode(gamepads);

    // send telemetry data
    if (telemetryTimer.time() > telemetryInterval) {
      telemetryTimer.reset();
      Telemetry telemetry = opModeManager.getActiveOpMode().telemetry;

      if (telemetry.hasData()) eventLoopManager.sendTelemetryData(telemetry);
      telemetry.clearData();
    }
  }

  /**
   * Teardown method
   * <p>
   * This method will be called when the robot is being shut down. This method should stop the robot. There will be no more changes to write
   * to the hardware after this method is called.
   * <p>
   * If an exception is thrown, then the event loop manager will attempt to shut down the robot
   * without the benefit of this method.
   * <p>
   * @see com.qualcomm.robotcore.eventloop.EventLoop#teardown()
   */
  @Override
  public void teardown() throws RobotCoreException {
    DbgLog.msg("======= TEARDOWN =======");

    // stop the op mode
    opModeManager.stopActiveOpMode();

    // power down and close the DC motor controllers
    for (Entry<String, DcMotorController> mc : hardwareMap.dcMotorController.entrySet()) {
      String name = mc.getKey();
      DcMotorController controller = mc.getValue();
      DbgLog.msg("Stopping DC Motor Controller " + name);
      controller.close();
    }

    // power down and close the servo controllers
    for (Entry<String, ServoController> sc : hardwareMap.servoController.entrySet()) {
      String name = sc.getKey();
      ServoController controller = sc.getValue();
      DbgLog.msg("Stopping Servo Controller " + name);
      controller.close();
    }

    // power down and close the legacy modules
    // this should be after the servo and motor controllers, since some of them
    // may be connected through this device
    for (Entry<String, LegacyModule> lm : hardwareMap.legacyModule.entrySet()) {
      String name = lm.getKey();
      LegacyModule module = lm.getValue();
      DbgLog.msg("Stopping Legacy Module" + name);
      module.close();
    }

    DbgLog.msg("======= TEARDOWN COMPLETE =======");
  }

  /**
   * If the driver station sends over a command, it will be routed to this method. You can choose
   * what to do with this command, or you can just ignore it completely.
   */
  @Override
  public void processCommand(Command command) {
    DbgLog.msg("Processing Command: " + command);

    String name = command.getName();
    String extra = command.getExtra();

    if (name.equals(CommandList.CMD_RESTART_ROBOT)) {
      handleCommandRestartRobot();
    } else if (name.equals(CommandList.CMD_REQUEST_OP_MODE_LIST)) {
      handleCommandRequestOpModeList();
    } else if (name.equals(CommandList.CMD_SWITCH_OP_MODE)) {
      handleCommandSwitchOpMode(extra);
    } else {
      DbgLog.msg("Unknown command: " + name);
    }
  }

  private void handleCommandRestartRobot() {
    callback.restartRobot();
  }

  private void handleCommandRequestOpModeList() {
    String opModeList = "";
    for (String opModeName : opModeManager.getOpModes()) {
      if (opModeList.isEmpty() == false) opModeList += Util.ASCII_RECORD_SEPARATOR;
      opModeList += opModeName;
    }
    eventLoopManager.sendCommand(new Command(CommandList.CMD_REQUEST_OP_MODE_LIST_RESP, opModeList));
  }

  private void handleCommandSwitchOpMode(String extra) {
    String newOpMode = extra;
    if (eventLoopManager.state != EventLoopManager.State.RUNNING){
      newOpMode = OpModeManager.DEFAULT_OP_MODE_NAME;
    }
    opModeManager.switchOpModes(newOpMode);
    eventLoopManager.sendCommand(new Command(CommandList.CMD_SWITCH_OP_MODE_RESP, opModeManager.getActiveOpModeName()));
  }
}
