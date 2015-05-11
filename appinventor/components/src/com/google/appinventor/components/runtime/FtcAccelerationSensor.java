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

import com.qualcomm.robotcore.hardware.AccelerationSensor;
import com.qualcomm.robotcore.hardware.AccelerationSensor.Acceleration;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A component for an acceleration sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ACCELERATION_SENSOR_COMPONENT_VERSION,
    description = "A component for an acceleration sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcAccelerationSensor extends FtcHardwareDevice {

  private volatile AccelerationSensor accelerationSensor;

  /**
   * Creates a new FtcAccelerationSensor component.
   */
  public FtcAccelerationSensor(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

  /**
   * X Acceleration property getter.
   */
  @SimpleProperty(description = "The X Acceleration, in g's.",
      category = PropertyCategory.BEHAVIOR)
  public double XAccel() {
    if (accelerationSensor != null) {
      try {
        Acceleration acceleration = accelerationSensor.getAcceleration();
        if (acceleration != null) {
          return acceleration.x;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "XAccel",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Y Acceleration property getter.
   */
  @SimpleProperty(description = "The Y Acceleration, in g's.",
      category = PropertyCategory.BEHAVIOR)
  public double YAccel() {
    if (accelerationSensor != null) {
      try {
        Acceleration acceleration = accelerationSensor.getAcceleration();
        if (acceleration != null) {
          return acceleration.y;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "YAccel",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Z Acceleration property getter.
   */
  @SimpleProperty(description = "The Z Acceleration, in g's.",
      category = PropertyCategory.BEHAVIOR)
  public double ZAccel() {
    if (accelerationSensor != null) {
      try {
        Acceleration acceleration = accelerationSensor.getAcceleration();
        if (acceleration != null) {
          return acceleration.z;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ZAccel",
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
    if (accelerationSensor != null) {
      try {
        String status = accelerationSensor.status();
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
  protected void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      accelerationSensor = hardwareMap.accelerationSensor.get(getDeviceName());
    }
  }

  @Override
  protected void clearHardwareDevice() {
    accelerationSensor = null;
  }
}
