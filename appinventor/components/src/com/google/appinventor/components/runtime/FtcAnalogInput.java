// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.AnalogInput;

/**
 * A component for an analog input of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ANALOG_INPUT_COMPONENT_VERSION,
    description = "A component for an analog input of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcAnalogInput extends FtcHardwareDevice {

  private volatile AnalogInput analogInput;

  /**
   * Creates a new FtcAnalogInput component.
   */
  public FtcAnalogInput(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

  /**
   * Value property getter.
   */
  @SimpleProperty(description = "The channel state.",
      category = PropertyCategory.BEHAVIOR)
  public int Value() {
    if (analogInput != null) {
      try {
        return analogInput.getValue();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Value",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  // HardwareDevice implementation

  @Override
  public void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      analogInput = hardwareMap.analogInput.get(getDeviceName());
      if (analogInput == null) {
        deviceNotFound("AnalogInput", hardwareMap.analogInput);
      }
    }
  }

  @Override
  public void clearHardwareDevice() {
    analogInput = null;
  }
}
