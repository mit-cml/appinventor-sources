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
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A component for an analog input device of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ANALOG_INPUT_COMPONENT_VERSION,
    description = "A component for an analog input device of an FTC robot.",
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

  /**
   * Value property getter.
   */
  @SimpleProperty(description = "The current ADC results from the A0-A7 channel input pins.",
      category = PropertyCategory.BEHAVIOR)
  public int Value() {
    checkHardwareDevice();
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

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    analogInput = hardwareMap.analogInput.get(getDeviceName());
    return analogInput;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("AnalogInput", hardwareMap.analogInput);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    analogInput = null;
  }
}
