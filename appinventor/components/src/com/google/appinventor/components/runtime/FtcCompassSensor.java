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

import com.qualcomm.robotcore.hardware.CompassSensor;
import com.qualcomm.robotcore.hardware.CompassSensor.CompassMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A component for a compass sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_COMPASS_SENSOR_COMPONENT_VERSION,
    description = "A component for a compass sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcCompassSensor extends FtcHardwareDevice {

  private volatile CompassSensor compassSensor;

  /**
   * Creates a new FtcCompassSensor component.
   */
  public FtcCompassSensor(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * Direction property getter.
   */
  @SimpleProperty(description = "The Direction, in degrees.",
      category = PropertyCategory.BEHAVIOR)
  public double Direction() {
    if (compassSensor != null) {
      try {
        return compassSensor.getDirection();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Direction",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The Status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    if (compassSensor != null) {
      try {
        String status = compassSensor.status();
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

  /**
   * Mode_MEASUREMENT property getter.
   */
  @SimpleProperty(description = "The constant for Mode_MEASUREMENT.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode_MEASUREMENT() {
    return CompassMode.MEASUREMENT_MODE.toString();
  }

  /**
   * Mode_CALIBRATION property getter.
   */
  @SimpleProperty(description = "The constant for Mode_CALIBRATION.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode_CALIBRATION() {
    return CompassMode.CALIBRATION_MODE.toString();
  }

  @SimpleFunction(description = "Change to calibration or measurement mode.")
  public void SetMode(String mode) {
    if (compassSensor != null) {
      try {
        for (CompassMode compassMode : CompassMode.values()) {
          if (compassMode.toString().equalsIgnoreCase(mode)) {
            compassSensor.setMode(compassMode);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "SetMode",
            ErrorMessages.ERROR_FTC_INVALID_COMPASS_MODE, mode);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * CalibrationFailed property getter.
   */
  @SimpleProperty(description = "Whether calibration failed.",
      category = PropertyCategory.BEHAVIOR)
  public boolean CalibrationFailed() {
    if (compassSensor != null) {
      try {
        return compassSensor.calibrationFailed();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "CalibrationFailed",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      compassSensor = hardwareMap.compassSensor.get(getDeviceName());
      if (compassSensor == null) {
        deviceNotFound("CompassSensor", hardwareMap.compassSensor);
      }
    }
    return compassSensor;
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    compassSensor = null;
  }

  // The following were deprecated on 2015/07/29.

  @SimpleProperty(userVisible = false,
      description = "Mode is deprecated. Please use SetMode.",
      category = PropertyCategory.BEHAVIOR)
  public void Mode(String modeString) {
  }

  @SimpleProperty(userVisible = false,
      description = "Mode is deprecated.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode() {
    return "";
  }
}
