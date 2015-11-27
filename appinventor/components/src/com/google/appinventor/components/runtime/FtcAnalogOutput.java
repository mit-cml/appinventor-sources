// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.AnalogOutput;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A component for an analog output device of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ANALOG_OUTPUT_COMPONENT_VERSION,
    description = "A component for an analog output device of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcAnalogOutput extends FtcHardwareDevice {

  private volatile AnalogOutput analogOutput;

  /**
   * Creates a new FtcAnalogOutput component.
   */
  public FtcAnalogOutput(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Sets the channel output voltage.\n" +
      "If mode == 0: takes input from -1023-1023, output in the range -4 to +4 volts.\n" +
      "If mode == 1, 2, or 3: takes input from 0-1023, output in the range 0 to 8 volts.")
  public void SetAnalogOutputVoltage(int voltage) {
    checkHardwareDevice();
    if (analogOutput != null) {
      try {
        analogOutput.setAnalogOutputVoltage(voltage);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetAnalogOutputVoltage",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Sets the channel output frequency in the range 1-5,000 Hz in " +
      "mode 1, 2 or 3.")
  public void SetAnalogOutputFrequency(int frequency) {
    checkHardwareDevice();
    if (analogOutput != null) {
      try {
        analogOutput.setAnalogOutputFrequency(frequency);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetAnalogOutputFrequency",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Sets the channel operating mode.\n" +
      "Mode 0: Voltage output. Range: -4V - 4V.\n" +
      "Mode 1: Sine wave output. Range: 0 - 8V.\n" +
      "Mode 2: Square wave output. Range: 0 - 8V.\n" +
      "Mode 3: Triangle wave output. Range: 0 - 8V.")
  public void SetAnalogOutputMode(int mode) {
    checkHardwareDevice();
    if (analogOutput != null) {
      try {
        analogOutput.setAnalogOutputMode((byte) mode);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetAnalogOutputMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    analogOutput = hardwareMap.analogOutput.get(getDeviceName());
    return analogOutput;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("AnalogOutput", hardwareMap.analogOutput);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    analogOutput = null;
  }
}
