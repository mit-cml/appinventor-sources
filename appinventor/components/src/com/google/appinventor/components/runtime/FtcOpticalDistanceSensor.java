// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

/**
 * A component for an optical distance sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_OPTICAL_DISTANCE_SENSOR_COMPONENT_VERSION,
    description = "A component for an optical distance sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcOpticalDistanceSensor extends FtcHardwareDevice {

  private volatile OpticalDistanceSensor opticalDistanceSensor;

  /**
   * Creates a new FtcOpticalDistanceSensor component.
   */
  public FtcOpticalDistanceSensor(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * LightDetected property getter.
   */
  @SimpleProperty(description = "The light detected by the sensor, on a scale of 0 to 1.",
      category = PropertyCategory.BEHAVIOR)
  public double LightDetected() {
    checkHardwareDevice();
    if (opticalDistanceSensor != null) {
      try {
        return opticalDistanceSensor.getLightDetected();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "LightDetected",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * LightDetectedRaw property getter.
   */
  @SimpleProperty(description = "The light detected by the sensor, as an integer.",
      category = PropertyCategory.BEHAVIOR)
  public int LightDetectedRaw() {
    checkHardwareDevice();
    if (opticalDistanceSensor != null) {
      try {
        return opticalDistanceSensor.getLightDetectedRaw();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "LightDetectedRaw",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Enable the LED light.")
  public void EnableLed(boolean enable) {
    checkHardwareDevice();
    if (opticalDistanceSensor != null) {
      try {
        opticalDistanceSensor.enableLed(enable);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableLed",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    checkHardwareDevice();
    if (opticalDistanceSensor != null) {
      try {
        String status = opticalDistanceSensor.status();
        if (status != null) {
          return status;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Status",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    opticalDistanceSensor = hardwareMap.opticalDistanceSensor.get(getDeviceName());
    return opticalDistanceSensor;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("OpticalDistanceSensor", hardwareMap.opticalDistanceSensor);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    opticalDistanceSensor = null;
  }
}
