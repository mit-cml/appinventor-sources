// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.ftc.FtcHardwareDevice;

import com.qualcomm.robotcore.hardware.AccelerationSensor;
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
    return (accelerationSensor != null)
        ? accelerationSensor.getAcceleration().x
        : 0;
  }

  /**
   * Y Acceleration property getter.
   */
  @SimpleProperty(description = "The Y Acceleration, in g's.",
      category = PropertyCategory.BEHAVIOR)
  public double YAccel() {
    return (accelerationSensor != null)
        ? accelerationSensor.getAcceleration().y
        : 0;
  }

  /**
   * Z Acceleration property getter.
   */
  @SimpleProperty(description = "The Z Acceleration, in g's.",
      category = PropertyCategory.BEHAVIOR)
  public double ZAccel() {
    return (accelerationSensor != null)
        ? accelerationSensor.getAcceleration().z
        : 0;
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The Status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    return (accelerationSensor != null)
        ? accelerationSensor.status()
        : "";
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void debugHardwareDevice(StringBuilder sb) {
    sb.append("accelerationSensor is ").append((accelerationSensor == null) ? "null" : "not null").append("\n");
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
