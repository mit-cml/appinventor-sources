// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * A base class for components for operation modes for an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@SimpleObject
public abstract class FtcOpModeBase extends AndroidNonvisibleComponent
    implements Component, OnDestroyListener, Deleteable, FtcRobotController.OpModeWrapper {

  private static final String DEFAULT_NAME = "Unnamed Op Mode";

  protected final OpMode opMode;

  private volatile String opModeName = DEFAULT_NAME;

  protected FtcOpModeBase(ComponentContainer container) {
    super(container.$form());

    this.opMode = createOpMode();

    FtcRobotController.addOpMode(this);
    form.registerForOnDestroy(this);
  }

  protected abstract OpMode createOpMode();

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

  @SimpleFunction(description = "Get the number of seconds this op mode has been running.")
  public double GetRuntime() {
    return opMode.getRuntime();
  }

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    FtcRobotController.removeOpMode(this);
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    FtcRobotController.removeOpMode(this);
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
