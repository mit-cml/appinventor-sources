// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.LightSensor;

/**
 * A component for a light sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_LIGHT_SENSOR_COMPONENT_VERSION,
    description = "A component for a light sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcLightSensor extends FtcHardwareDevice {

  private volatile boolean enableLed;
  private volatile LightSensor lightSensor;

  /**
   * Creates a new FtcLightSensor component.
   */
  public FtcLightSensor(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

  /**
   * EnableLed property getter.
   */
  @SimpleProperty(description = "Whether to enable the LED light.",
      category = PropertyCategory.BEHAVIOR)
  public boolean EnableLed() {
    return enableLed;
  }

  /**
   * EnableLed property setter.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void EnableLed(boolean enableLed) {
    this.enableLed = enableLed;
    setEnableLed();
  }

  /**
   * Light property getter.
   */
  @SimpleProperty(description = "The light detected by the sensor, on a scale of 0 to 1.",
      category = PropertyCategory.BEHAVIOR)
  public double LightLevel() {
    return (lightSensor != null)
        ? lightSensor.getLightLevel()
        : 0;
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The Status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    return (lightSensor != null)
        ? lightSensor.status()
        : "";
  }

  private void setEnableLed() {
    if (lightSensor != null) {
      lightSensor.enableLed(enableLed);
    }
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void debugHardwareDevice(StringBuilder sb) {
    sb.append("lightSensor is ").append((lightSensor == null) ? "null" : "not null").append("\n");
  }

  // FtcHardwareDevice implementation

  @Override
  void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      lightSensor = hardwareMap.lightSensor.get(getDeviceName());
      setEnableLed();
    }
  }

  @Override
  void clearHardwareDevice() {
    lightSensor = null;
  }
}
