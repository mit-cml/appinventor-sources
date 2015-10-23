// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * A component for an operation mode for an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_OP_MODE_COMPONENT_VERSION,
    description = "A component for an operation mode for an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcOpMode extends FtcOpModeBase {

  /**
   * Creates a new FtcOpMode component.
   */
  public FtcOpMode(ComponentContainer container) {
    super(container.$form());
  }

  protected OpMode createOpMode() {
    return new OpMode() {
      @Override
      public void init() {
        FtcRobotController.activateOpMode(this);
        Init();
      }

      @Override
      public void init_loop() {
        InitLoop();
      }

      @Override
      public void start() {
        Start();
      }

      @Override
      public void loop() {
        Loop();
      }

      @Override
      public void stop() {
        Stop();
        FtcRobotController.deactivateOpMode();
      }
    };
  }

  @SimpleEvent(description = "This event is triggered once when the INIT button is pressed.")
  public void Init() {
    EventDispatcher.dispatchEvent(this, "Init");
  }

  @SimpleEvent(description = "This event is triggered repeatedly when the INIT button is pressed.")
  public void InitLoop() {
    EventDispatcher.dispatchEvent(this, "InitLoop");
  }

  @SimpleEvent(description = "This event is triggered once when the PLAY button is first pressed.")
  public void Start() {
    EventDispatcher.dispatchEvent(this, "Start");
  }

  @SimpleEvent(description = "This event is triggered repeatedly while this op mode is running.")
  public void Loop() {
    EventDispatcher.dispatchEvent(this, "Loop");
  }

  @SimpleEvent(description = "This event is triggered when this op mode is first disabled.")
  public void Stop() {
    EventDispatcher.dispatchEvent(this, "Stop");
  }

  /**
   * Time property getter.
   */
  @SimpleProperty(description = "The number of seconds this op mode had been running when the " +
      "Loop event was triggered.",
      category = PropertyCategory.BEHAVIOR)
  public double Time() {
    return opMode.time;
  }
}
