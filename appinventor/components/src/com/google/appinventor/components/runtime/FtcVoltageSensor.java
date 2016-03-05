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
import com.qualcomm.robotcore.hardware.VoltageSensor;

/**
 * A component for a voltage sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_VOLTAGE_SENSOR_COMPONENT_VERSION,
    description = "A component for a voltage sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcVoltageSensor extends FtcHardwareDevice {

  private volatile VoltageSensor voltageSensor;

  /**
   * Creates a new FtcVoltageSensor component.
   */
  public FtcVoltageSensor(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * Voltage property getter.
   */
  @SimpleProperty(description = "The voltage.",
      category = PropertyCategory.BEHAVIOR)
  public double Voltage() {
    checkHardwareDevice();
    if (voltageSensor != null) {
      try {
        return voltageSensor.getVoltage();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Voltage",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    voltageSensor = hardwareMap.voltageSensor.get(getDeviceName());
    return voltageSensor;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("VoltageSensor", hardwareMap.voltageSensor);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    voltageSensor = null;
  }
}
