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
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

//package com.qualcomm.ftcrobotcontroller;
package com.google.appinventor.components.runtime.ftc;

import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.FtcEventLoopHandler;
import com.qualcomm.ftccommon.UpdateUI;
/* Removed for App Inventor
import com.qualcomm.ftcrobotcontroller.opmodes.FtcOpModeRegister;
*/
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
// Added for App Inventor:
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.Util;

// Added for App Inventor:
import com.google.appinventor.components.runtime.FtcRobotController;

/**
 * Main event loop to control robot
 */
public class FtcEventLoop implements EventLoop, BatteryChecker.BatteryWatcher {

  FtcEventLoopHandler ftcEventLoopHandler;

  OpModeManager opModeManager = new OpModeManager(new HardwareMap());

  public FtcEventLoop(HardwareFactory hardwareFactory, UpdateUI.Callback callback) {
    this.ftcEventLoopHandler = new FtcEventLoopHandler(hardwareFactory, callback);
  }

  public OpModeManager getOpModeManager() {
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

    opModeManager.registerOpModes(new FtcOpModeRegister());

    ftcEventLoopHandler.init(eventLoopManager);

    HardwareMap hardwareMap = ftcEventLoopHandler.getHardwareMap();

    // Start up the op mode manager
    opModeManager.setHardwareMap(hardwareMap);

    // Added for App Inventor:
    aiFtcRobotController.onEventLoopInit(eventLoopManager, hardwareMap);
    DbgLog.msg("======= INIT FINISH =======");
  }

  /**
   * Loop method, this will be called repeatedly while the robot is running.
   * <p>
   * @see com.qualcomm.robotcore.eventloop.EventLoop#loop()
   */
  @Override
  public void loop() throws RobotCoreException {

    ftcEventLoopHandler.displayGamePadInfo(opModeManager.getActiveOpModeName());
    Gamepad gamepads[] = ftcEventLoopHandler.getGamepads();

    opModeManager.runActiveOpMode(gamepads);

    // send telemetry data
    ftcEventLoopHandler.sendTelemetryData(opModeManager.getActiveOpMode().telemetry);

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
    ftcEventLoopHandler.shutdownMotorControllers();

    // power down and close the servo controllers
    ftcEventLoopHandler.shutdownServoControllers();

    // power down and close the legacy modules
    // this should be after the servo and motor controllers, since some of them
    // may be connected through this device
    ftcEventLoopHandler.shutdownLegacyModules();

    // Added for App Inventor:
    aiFtcRobotController.onEventLoopTeardown();
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
    } else if (name.equals(CommandList.CMD_RESUME_OP_MODE)) {
      handleResumeOpMode(extra);
    } else if (name.equals(CommandList.CMD_CANCEL_RESUME)) {
      handleCancelResume();
    } else {
      DbgLog.msg("Unknown command: " + name);
    }
  }

  private void handleCancelResume() {
    if (ftcEventLoopHandler.waitingForRestart()) {
      opModeManager.cancelResume();
      ftcEventLoopHandler.finishRestart(OpModeManager.DEFAULT_OP_MODE_NAME);
    }
  }

  private void handleResumeOpMode(String extra) {
    if (ftcEventLoopHandler.waitingForRestart()) {
      opModeManager.resumeOpMode();
      ftcEventLoopHandler.finishRestart(extra);

      //send response
      ftcEventLoopHandler.sendCommand(new Command(CommandList.CMD_RESUME_OP_MODE_RESP, opModeManager.getActiveOpModeName()));
    }
  }

  private void handleCommandRestartRobot() {
    ftcEventLoopHandler.restartRobot();
  }

  private void handleCommandRequestOpModeList() {
    String opModeList = "";
    for (String opModeName : opModeManager.getOpModes()) {
      if (opModeList.isEmpty() == false) opModeList += Util.ASCII_RECORD_SEPARATOR;
      opModeList += opModeName;
    }
    ftcEventLoopHandler.sendCommand(new Command(CommandList.CMD_REQUEST_OP_MODE_LIST_RESP, opModeList));
  }

  private void handleCommandSwitchOpMode(String extra) {

    // if the event loop isn't running, switch to stop op
    String newOpMode = ftcEventLoopHandler.getOpMode(extra);

    opModeManager.switchOpModes(newOpMode);

    RobotLog.clearGlobalErrorMsg();

    //send response
    ftcEventLoopHandler.sendCommand(new Command(CommandList.CMD_SWITCH_OP_MODE_RESP, opModeManager.getActiveOpModeName()));
  }

  public void updateBatteryLevel(float percent) {
    ftcEventLoopHandler.sendTelemetry(EventLoopManager.RC_BATTERY_LEVEL_KEY, "RobotController battery level: " + percent + "%");
  }


  // Added for App Inventor:
  private FtcRobotController aiFtcRobotController;

  FtcEventLoop(HardwareFactory hardwareFactory, UpdateUI.Callback callback,
      FtcRobotController aiFtcRobotController) {
    this(hardwareFactory, callback);
    this.aiFtcRobotController = aiFtcRobotController;
  }

  class FtcOpModeRegister implements OpModeRegister {
    public void register(OpModeManager manager) {
      aiFtcRobotController.register(manager);
    }
  }
}
