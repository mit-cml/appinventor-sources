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
  // We need a backing field for the Mode property because CompassSensor doesn't have a getMode
  // method.
  private volatile CompassMode mode = CompassMode.MEASUREMENT_MODE;

  /**
   * Creates a new FtcCompassSensor component.
   */
  public FtcCompassSensor(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

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

  /**
   * Mode property getter.
   */
  @SimpleProperty(description = "The mode: MEASUREMENT_MODE or CALIBRATION_MODE.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode() {
    if (compassSensor != null) {
      try {
        // CompassSensor doesn't have a getMode method. Use our backing field instead.
        CompassMode mode = this.mode;
        if (mode != null) {
          return mode.toString();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Mode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  /**
   * Mode property setter.
   */
  @SimpleProperty
  public void Mode(String modeString) {
    if (compassSensor != null) {
      try {
        for (CompassMode mode : CompassMode.values()) {
          if (mode.toString().equalsIgnoreCase(modeString)) {
            compassSensor.setMode(mode);
            // Set our backing field.
            this.mode = mode;
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "Mode",
            ErrorMessages.ERROR_FTC_INVALID_COMPASS_MODE, modeString);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Mode",
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

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void initHardwareDevice(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      compassSensor = hardwareMap.compassSensor.get(getDeviceName());
      if (compassSensor == null) {
        deviceNotFound("CompassSensor", hardwareMap.compassSensor);
      }
    }
  }

  @Override
  public void clearHardwareDevice() {
    compassSensor = null;
  }
}
