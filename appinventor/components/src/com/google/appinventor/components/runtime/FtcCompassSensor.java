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

import com.qualcomm.robotcore.hardware.CompassSensor;
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
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcCompassSensor extends FtcHardwareDevice {

  private volatile CompassSensor compassSensor;

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
    return (compassSensor != null)
        ? compassSensor.getDirection()
        : 0;
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The Status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    return (compassSensor != null)
        ? compassSensor.status()
        : "";
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void debugHardwareDevice(StringBuilder sb) {
    sb.append("compassSensor is ").append((compassSensor == null) ? "null" : "not null").append("\n");
  }

  // FtcHardwareDevice implementation

  @Override
  protected void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      compassSensor = hardwareMap.compassSensor.get(getDeviceName());
    }
  }

  @Override
  protected void clearHardwareDevice() {
    compassSensor = null;
  }
}
