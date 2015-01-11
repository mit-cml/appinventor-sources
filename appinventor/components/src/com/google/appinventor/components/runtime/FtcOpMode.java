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
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

// TODO(4.0): add code
/*
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
*/

import java.util.concurrent.TimeUnit;

/**
 * FtcOpMode component
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_OP_MODE_COMPONENT_VERSION,
    description = "A component that represents an Op Mode of an FTC Robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
public final class FtcOpMode extends AndroidNonvisibleComponent
    implements Component, Deleteable {
  private final static String DEFAULT_NAME = "Unnamed Op Mode";

  private FtcRobotController ftcRobotController;

  // TODO(4.0): add code
  /*
  OpMode opmode;
  */
  // TODO(4.0): remove code begin
  private volatile long startTimeInNanoseconds = 0;
  // TODO(4.0): remove code end
  private String name = DEFAULT_NAME;

  /**
   * Creates a new FtcOpMode
   */
  public FtcOpMode(ComponentContainer container) {
    super(container.$form());

    // TODO(4.0): add code
    /*
    opmode = new OpMode() {
      public void start() {
        Start();
      }

      public void run() {
        Run();
      }

      public void stop() {
        Stop();
      }
    };
    */
  }
  
  // Properties

  /**
   * FtcRobotController property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(
  description = "The FtcRobotController component that this op mode belongs to.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public FtcRobotController FtcRobotController() {
    return ftcRobotController;
  }

  /**
   * FtcRobotController property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_ROBOT_CONTROLLER,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void FtcRobotController(FtcRobotController ftcRobotController) {
    if (this.ftcRobotController != null) {
      this.ftcRobotController.removeFtcOpMode(this);
      this.ftcRobotController = null;
    }

    if (ftcRobotController != null) {
      this.ftcRobotController = ftcRobotController;
      this.ftcRobotController.addFtcOpMode(this);
    }
  }

  /**
   * Name property getter method.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The name of this op mode.",
      category = PropertyCategory.BEHAVIOR,
      userVisible = false)
  public String OpModeName() {
    return name;
  }

  /**
   * Name property setter method.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = DEFAULT_NAME)
  @SimpleProperty(userVisible = false)
  public void OpModeName(String name) {
    this.name = name;
  }

  @SimpleFunction(description = "Get the number of seconds this op mode has been running.")
  public double GetRuntime() {
    // TODO(4.0): add code
    /*
    return opMode.getRuntime();
    */
    // TODO(4.0): remove code begin
    long durationInNanoseconds = System.nanoTime() - startTimeInNanoseconds;
    return TimeUnit.SECONDS.convert(durationInNanoseconds, TimeUnit.NANOSECONDS);
    // TODO(4.0): remove code end
  }

  // Events

  @SimpleEvent(description = "This event is run when this op mode is enabled.")
  public void Start() {
    startTimeInNanoseconds = System.nanoTime();
    EventDispatcher.dispatchEvent(this, "Start");
  }

  @SimpleEvent(description = "This event is run repeatedly while this op mode is running.")
  public void Run() {
    EventDispatcher.dispatchEvent(this, "Run");
  }

  @SimpleEvent(description = "This event is run when this op mode is disabled.")
  public void Stop() {
    EventDispatcher.dispatchEvent(this, "Stop");
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    if (ftcRobotController != null) {
      ftcRobotController.removeFtcOpMode(this);
      ftcRobotController = null;
    }
  }

  // TODO(4.0): remove code begin
  // OpMode implementation

  public void triggerStartEvent() {
    // All FtcOpMode events are executed on robot event thread, not on the Android event thread.
    Start();
  }

  public void triggerRunEvent() {
    // All FtcOpMode events are executed on robot event thread, not on the Android event thread.
    Run();
  }

  public void triggerStopEvent() {
    // All FtcOpMode events are executed on robot event thread, not on the Android event thread.
    Stop();
  }
  // TODO(4.0): remove code end
}
