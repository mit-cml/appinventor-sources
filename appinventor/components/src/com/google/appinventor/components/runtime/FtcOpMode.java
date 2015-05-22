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
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * A component for an Operational Mode for an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_OP_MODE_COMPONENT_VERSION,
    description = "A component for an Operational Mode for an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcOpMode extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcRobotController.OpModeWrapper {

  private final static String DEFAULT_NAME = "Unnamed Op Mode";

  private final OpMode opMode;

  private volatile String opModeName = DEFAULT_NAME;

  /**
   * Creates a new FtcOpMode component.
   */
  public FtcOpMode(ComponentContainer container) {
    super(container.$form());

    opMode = new OpMode() {
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
      }
    };

    FtcRobotController.addOpModeWrapper(form, this);
  }

  // Properties

  /**
   * OpModeName property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The name of this Op Mode.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String OpModeName() {
    return opModeName;
  }

  /**
   * OpModeName property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = DEFAULT_NAME)
  @SimpleProperty(userVisible = false)
  public void OpModeName(String opModeName) {
    this.opModeName = opModeName;
  }

  // Functions

  @SimpleFunction(description = "Get the number of seconds this op mode has been running.")
  public double GetRuntime() {
    return opMode.getRuntime();
  }

  @SimpleFunction(description = "Adds a text data point to the telemetry for this op mode.")
  public void TelemetryAddTextData(String key, String text) {
    try {
      opMode.telemetry.addData(key, text);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "TelemetryAddTextData",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Adds a numeric data point to the telemetry for this op mode.")
  public void TelemetryAddNumericData(String key, float number) {
    try {
      opMode.telemetry.addData(key, number);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "TelemetryAddNumericData",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  // Events

  @SimpleEvent(description =
      "This event is triggered when the user hits the start button on the driver station.")
  public void Start() {
    EventDispatcher.dispatchEvent(this, "Start");
  }

  @SimpleEvent(description = "This event is triggered repeatedly while this op mode is running.")
  public void Loop() {
    EventDispatcher.dispatchEvent(this, "Loop");
  }

  @SimpleEvent(description = "This event is triggered when the user hits the stop button on the " +
      "driver stations, or right before the user switches to another Op Mode.")
  public void Stop() {
    EventDispatcher.dispatchEvent(this, "Stop");
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    FtcRobotController.removeOpModeWrapper(form, this);
  }

  // FtcRobotController.OpModeWrapper implementation

  @Override
  public String getOpModeName() {
    return opModeName;
  }

  @Override
  public OpMode getOpMode() {
    return opMode;
  }
}
