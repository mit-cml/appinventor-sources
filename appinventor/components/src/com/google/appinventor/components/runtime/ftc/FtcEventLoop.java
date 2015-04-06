/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

// Modified for App Inventor by Liz Looney
// Copyright 2015 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.ftc;

import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.DbgLog;
//AI import com.qualcomm.ftcrobotcontroller.opmodes.FtcOpModeRegister;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.LegacyModule;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.Telemetry;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.util.Map.Entry;

import com.google.appinventor.components.runtime.FtcRobotController;

/**
 * Main event loop to control robot
 */
public class FtcEventLoop implements EventLoop, BatteryChecker.BatteryWatcher {

  // Event loop manager
  EventLoopManager eventLoopManager;

  // Telemetry
  ElapsedTime telemetryTimer = new ElapsedTime();
  double telemetryInterval = 0.250; // in seconds

  FtcRobotControllerActivity.Callback callback;

  // Hardware Factory and Map
  HardwareFactory hardwareFactory;
  HardwareMap hardwareMap = new HardwareMap();

  OpModeManager opModeManager /*AI = new OpModeManager(hardwareMap, new FtcOpModeRegister())*/;

  private FtcRobotController ftcRobotController;

  FtcEventLoop(HardwareFactory hardwareFactory, FtcRobotControllerActivity.Callback callback) {
    this.hardwareFactory = hardwareFactory;
    this.callback = callback;
  }

  public OpModeManager getOpModeManager(){
    return opModeManager;
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

    ftcRobotController.onEventLoopInit(eventLoopManager, hardwareMap);
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

      getRobotBatteryInfo();

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

    ftcRobotController.onEventLoopTeardown();
    DbgLog.msg("======= TEARDOWN COMPLETE =======");
  }

  /**
   * If the driver station sends over a command, it will be routed to this method. You can choose
   * what to do with this command, or you can just ignore it completely.
   */
  @Override
  public void processCommand(Command command) {
    DbgLog.msg("Processing Command: " + command.getName() + " " + command.getExtra());

    String name = command.getName();
    String extra = command.getExtra();

    if (name.equals(CommandList.CMD_RESTART_ROBOT)) {
      handleCommandRestartRobot();
    } else if (name.equals(CommandList.CMD_REQUEST_OP_MODE_LIST)) {
      handleCommandRequestOpModeList();
    } else if (name.equals(CommandList.CMD_SWITCH_OP_MODE)) {
      handleCommandSwitchOpMode(extra);
    } else if (name.equals(CommandList.CMD_CANCEL_OP_RESTART)) {
      handleCancelRestartOpMode(extra);
    } else {
      DbgLog.msg("Unknown command: " + name);
    }
  }

  private void handleCancelRestartOpMode(String extra) {

    if (eventLoopManager.state == EventLoopManager.State.DROPPED_CONNECTION) {
      opModeManager.switchOpModes(OpModeManager.DEFAULT_OP_MODE_NAME);
      eventLoopManager.restartOpMode(extra);
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
    if (eventLoopManager.state != EventLoopManager.State.RUNNING) {
      newOpMode = OpModeManager.DEFAULT_OP_MODE_NAME;
    }
    opModeManager.switchOpModes(newOpMode);

    if (eventLoopManager.isWaitingForRestart()){
      eventLoopManager.restartOpMode(newOpMode);
    }

    //eventLoopManager.restartOpMode(newOpMode);
    eventLoopManager.sendCommand(new Command(CommandList.CMD_SWITCH_OP_MODE_RESP, opModeManager.getActiveOpModeName()));
  }

  public void updateBatteryLevel(float percent) {
    sendBatteryInfo(EventLoopManager.RC_BATTERY_LEVEL_KEY, "RobotController Battery Level Remaining: " + percent + "%");
  }

  private void getRobotBatteryInfo() {
    double minBatteryLevel = Double.MAX_VALUE;

    for (VoltageSensor sensor : hardwareMap.voltageSensor) {
      if (sensor.getVoltage() < minBatteryLevel){
        minBatteryLevel = sensor.getVoltage();
      }
    }

    String msg;
    if (hardwareMap.voltageSensor.size() == 0) {
      msg = "Robot Battery Level: " + "unknown";
    } else {
      BigDecimal rounded = new BigDecimal(minBatteryLevel).setScale(2, RoundingMode.HALF_UP);
      msg = "Robot Battery Level: " + rounded.doubleValue();
    }
    sendBatteryInfo(EventLoopManager.ROBOT_BATTERY_LEVEL_KEY, msg);
  }

  private void sendBatteryInfo(String tag, String msg) {
    Telemetry telemetry = new Telemetry();
    telemetry.setTag(tag);
    telemetry.addData(tag, msg);
    if (eventLoopManager != null) {
      eventLoopManager.sendTelemetryData(telemetry);
    }
  }

  // For App Inventor:

  FtcEventLoop(HardwareFactory hardwareFactory, FtcRobotControllerActivity.Callback callback,
      FtcRobotController ftcRobotController) {
    this(hardwareFactory, callback);
    this.ftcRobotController = ftcRobotController;
    opModeManager = new OpModeManager(hardwareMap, /* OpModeRegister */ ftcRobotController);
  }
}
