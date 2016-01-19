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
  @SimpleProperty(description = "The direction, in degrees.",
      category = PropertyCategory.BEHAVIOR)
  public double Direction() {
    checkHardwareDevice();
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
  @SimpleProperty(description = "The status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    checkHardwareDevice();
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
   * CompassMode_MEASUREMENT_MODE property getter.
   */
  @SimpleProperty(description = "The constant for CompassMode_MEASUREMENT_MODE.",
      category = PropertyCategory.BEHAVIOR)
  public String CompassMode_MEASUREMENT_MODE() {
    return CompassMode.MEASUREMENT_MODE.toString();
  }

  /**
   * CompassMode_CALIBRATION_MODE property getter.
   */
  @SimpleProperty(description = "The constant for CompassMode_CALIBRATION_MODE.",
      category = PropertyCategory.BEHAVIOR)
  public String CompassMode_CALIBRATION_MODE() {
    return CompassMode.CALIBRATION_MODE.toString();
  }

  @SimpleFunction(description = "Change to calibration or measurement mode.\n" +
      "Valid values are CompassMode_CALIBRATION_MODE or CompassMode_MEASUREMENT_MODE.")
  public void SetMode(String compassMode) {
    checkHardwareDevice();
    if (compassSensor != null) {
      try {
        for (CompassMode compassModeValue : CompassMode.values()) {
          if (compassModeValue.toString().equalsIgnoreCase(compassMode)) {
            compassSensor.setMode(compassModeValue);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "SetMode",
            ErrorMessages.ERROR_FTC_INVALID_COMPASS_MODE, compassMode);
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
    checkHardwareDevice();
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
  protected Object initHardwareDeviceImpl() {
    compassSensor = hardwareMap.compassSensor.get(getDeviceName());
    return compassSensor;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("CompassSensor", hardwareMap.compassSensor);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    compassSensor = null;
  }
}
