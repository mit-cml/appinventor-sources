// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * A component for a linear operation mode for an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_LINEAR_OP_MODE_COMPONENT_VERSION,
    description = "A component for a linear operation mode for an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcLinearOpMode extends FtcOpModeBase {

  private LinearOpMode linearOpMode;

  /**
   * Creates a new FtcLinearOpMode component.
   */
  public FtcLinearOpMode(ComponentContainer container) {
    super(container.$form());
  }

  protected OpMode createOpMode() {
    linearOpMode = new LinearOpMode() {
      @Override
      public void runOpMode() throws InterruptedException {
        FtcRobotController.activateOpMode(this);
        try {
          RunOpMode();
        } finally {
          FtcRobotController.deactivateOpMode();
        }
      }
    };
    return linearOpMode;
  }

  @SimpleEvent(description = "This event is triggered when this op mode is run.")
  public void RunOpMode() {
    EventDispatcher.dispatchEvent(this, "RunOpMode");
  }

  @SimpleFunction(description = "Pause the Linear Op Mode until start has been pressed.")
  public void WaitForStart() throws InterruptedException {
    linearOpMode.waitForStart();
  }

  @SimpleFunction(description = "Wait for one full cycle of the hardware.\n" +
      "Each cycle of the hardware your commands are sent out to the hardware;\n" +
      "and the latest data is read back in.\n" +
      "This method has a strong guarantee to wait for at least one full hardware\n" +
      "hardware cycle.")
  public void WaitOneFullHardwareCycle() throws InterruptedException {
    linearOpMode.waitOneFullHardwareCycle();
  }

  @SimpleFunction(description = "Wait for the start of the next hardware cycle.\n" +
      "Each cycle of the hardware your commands are sent out to the hardware;\n" +
      "and the latest data is read back in.\n" +
      "This method will wait for the current hardware cycle to finish, which is\n" +
      "also the start of the next hardware cycle.")
  public void WaitForNextHardwareCycle() throws InterruptedException {
    linearOpMode.waitForNextHardwareCycle();
  }

  @SimpleFunction(description = "Sleep for the given amount of milliseconds.")
  public void Sleep(long milliseconds) throws InterruptedException {
    linearOpMode.sleep(milliseconds);
  }

  @SimpleFunction(description = "Returns true as long as the op mode is active.")
  public boolean OpModeIsActive() {
    return linearOpMode.opModeIsActive();
  }

  /**
   * Time property getter.
   */
  @SimpleProperty(description = "The number of seconds this op mode had been running when the " +
      "RunOpMode event was triggered.",
      category = PropertyCategory.BEHAVIOR)
  public double Time() {
    return opMode.time;
  }
}
