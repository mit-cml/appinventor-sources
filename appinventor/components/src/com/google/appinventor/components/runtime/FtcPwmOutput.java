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

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PWMOutput;

/**
 * A component for a PWM output device of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_PWM_OUTPUT_COMPONENT_VERSION,
    description = "A component for a PWM output device of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcPwmOutput extends FtcHardwareDevice {

  private volatile PWMOutput pwmOutput;

  /**
   * Creates a new FtcPwmOutput component.
   */
  public FtcPwmOutput(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * PulseWidthOutputTime property setter.
   */
  @SimpleProperty
  public void PulseWidthOutputTime(int time) {
    checkHardwareDevice();
    if (pwmOutput != null) {
      try {
        pwmOutput.setPulseWidthOutputTime(time);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "PulseWidthOutputTime",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * PulseWidthOutputTime property getter.
   */
  @SimpleProperty(description = "The pulse width output time for this port.\n" +
      "Typically set to a value between 750 and 2,250 to control a servo.",
      category = PropertyCategory.BEHAVIOR)
  public int PulseWidthOutputTime() {
    checkHardwareDevice();
    if (pwmOutput != null) {
      try {
        return pwmOutput.getPulseWidthOutputTime();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "PulseWidthOutputTime",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * PulseWidthOutputPeriod property setter.
   */
  @SimpleProperty
  public void PulseWidthPeriod(int period) {
    checkHardwareDevice();
    if (pwmOutput != null) {
      try {
        pwmOutput.setPulseWidthPeriod(period);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "PulseWidthPeriod",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * PulseWidthOutputPeriod property getter.
   */
  @SimpleProperty(description = "The pulse width output period.\n" +
      "Typically set to 20,000 to control servo.",
      category = PropertyCategory.BEHAVIOR)
  public int PulseWidthPeriod() {
    checkHardwareDevice();
    if (pwmOutput != null) {
      try {
        return pwmOutput.getPulseWidthPeriod();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "PulseWidthPeriod",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    pwmOutput = hardwareMap.pwmOutput.get(getDeviceName());
    return pwmOutput;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("PWMOutput", hardwareMap.pwmOutput);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    pwmOutput = null;
  }
}
