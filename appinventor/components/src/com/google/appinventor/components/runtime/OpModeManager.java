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

package com.google.appinventor.components.runtime;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages Op Modes
 *
 * Able to switch between op modes
 */
@SuppressWarnings("unused")
public class OpModeManager {

  public static final String DEFAULT_OP_MODE_NAME = "Stop Robot";
  public static final OpMode DEFAULT_OP_MODE = new DefaultOpMode();

  private Map<String, OpMode> opModes = new HashMap<String, OpMode>();

  private String activeOpModeName = DEFAULT_OP_MODE_NAME;
  private OpMode activeOpMode = DEFAULT_OP_MODE;

  private HardwareMap hardwareMap = new HardwareMap();

  public OpModeManager(HardwareMap hardwareMap, OpModeRegister register) {
    this.hardwareMap = hardwareMap;

    // register our default op mode first, that way the user can override it
    register(DEFAULT_OP_MODE_NAME, DEFAULT_OP_MODE);

    // grab users op modes
    register.register(this);

    // switch to the default op mode
    switchOpModes(DEFAULT_OP_MODE_NAME);
  }

  public void setHardwareMap(HardwareMap hardwareMap) {
    this.hardwareMap = hardwareMap;
  }

  public HardwareMap getHardwareMap() {
    return hardwareMap;
  }

  public void switchOpModes(String name) {
    RobotLog.i("Attempting to switch to op mode " + name);

    stopActiveOpMode();

    activeOpModeName = name;
    activeOpMode = (OpMode) opModes.get(name);

    startActiveOpMode();
  }

  public Set<String> getOpModes() {
    return opModes.keySet();
  }

  public String getActiveOpModeName() { return  activeOpModeName; }

  public OpMode getActiveOpMode() {
    return activeOpMode;
  }

  public void startActiveOpMode() {
    activeOpMode.time = activeOpMode.getRuntime();
    activeOpMode.hardwareMap = hardwareMap;
    activeOpMode.start();
  }

  public void runActiveOpMode(Gamepad[] gamepads) {
    activeOpMode.time = activeOpMode.getRuntime();
    activeOpMode.gamepad1 = gamepads[0];
    activeOpMode.gamepad2 = gamepads[1];
    activeOpMode.run();
  }

  public void stopActiveOpMode() {
    activeOpMode.stop();

    // set sane defaults
    activeOpModeName = DEFAULT_OP_MODE_NAME;
    activeOpMode = DEFAULT_OP_MODE;
  }

  public void logOpModes() {
    RobotLog.i("There are " + opModes.size() + " Op Modes");
    for (Map.Entry<String, OpMode> entry : opModes.entrySet()) {
      RobotLog.i("   Op Mode: " + entry.getKey());
    }
  }

  public void register(String name, OpMode opMode) {
    opModes.put(name, opMode);
  }

  /*
   * default op mode
   */
  private static class DefaultOpMode extends OpMode {

    public DefaultOpMode() {
      // take no action
    }

    @Override
    public void start() {
      // take no action
    }

    @Override
    public void run() {
      // power down the servos
      for (Map.Entry<String, ServoController> servoController : hardwareMap.servoController.entrySet()) {
        servoController.getValue().pwmDisable();
      }

      // power down the motors
      for (Map.Entry<String, DcMotor> dcMotor : hardwareMap.dcMotor.entrySet()) {
        dcMotor.getValue().setPowerFloat();
      }
    }

    @Override
    public void stop() {
      // take no action
    }
  }
}
