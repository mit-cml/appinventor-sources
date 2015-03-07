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

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

/**
 * A component for an ultrasonic sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ULTRASONIC_SENSOR_COMPONENT_VERSION,
    description = "A component for an ultrasonic sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcUltrasonicSensor extends FtcHardwareDevice {

  private volatile UltrasonicSensor ultrasonicSensor;

  /**
   * Creates a new FtcUltrasonicSensor component.
   */
  public FtcUltrasonicSensor(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

  /**
   * UltrasonicLevel property getter.
   */
  @SimpleProperty(description = "The UltrasonicLevel.",
      category = PropertyCategory.BEHAVIOR)
  public double UltrasonicLevel() {
    return (ultrasonicSensor != null)
        ? ultrasonicSensor.getUltrasonicLevel()
        : 0;
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The Status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    return (ultrasonicSensor != null)
        ? ultrasonicSensor.status()
        : "";
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void debugHardwareDevice(StringBuilder sb) {
    sb.append("ultrasonicSensor is ").append((ultrasonicSensor == null) ? "null" : "not null").append("\n");
  }

  // FtcHardwareDevice implementation

  @Override
  protected void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      ultrasonicSensor = hardwareMap.ultrasonicSensor.get(getDeviceName());
    }
  }

  @Override
  protected void clearHardwareDevice() {
    ultrasonicSensor = null;
  }
}
