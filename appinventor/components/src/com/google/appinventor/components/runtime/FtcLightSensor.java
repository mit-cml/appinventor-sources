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

import com.qualcomm.robotcore.hardware.LightSensor;

/**
 * A component that provides an interface to a light sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_LIGHT_SENSOR_COMPONENT_VERSION,
    description = "A component that provides an interface to a light sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "robotcore.jar")
public final class FtcLightSensor extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcSensorMux.Child {

  private FtcSensorMux ftcSensorMux;
  private int channel = 1;
  private boolean enableLed;
  private LightSensor lightSensor;
  
  /**
   * Creates a new FtcLightSensor component.
   */
  public FtcLightSensor(ComponentContainer container) {
    super(container.$form());
  }

  private boolean isAfterEventLoopInit() {
    return (ftcSensorMux != null)
        ? ftcSensorMux.isAfterEventLoopInit()
        : false;
  }

  // Properties

  /**
   * FtcSensorMux property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The FtcSensorMux component that this IR sensor belongs to.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public FtcSensorMux FtcSensorMux() {
    return ftcSensorMux;
  }

  /**
   * FtcSensorMux property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_SENSOR_MUX,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void FtcSensorMux(FtcSensorMux ftcSensorMux) {
    if (this.ftcSensorMux != null) {
      if (isAfterEventLoopInit()) {
        destroyLightSensor();
      }
      this.ftcSensorMux.removeChild(this);
      this.ftcSensorMux = null;
    }

    if (ftcSensorMux != null) {
      this.ftcSensorMux = ftcSensorMux;
      this.ftcSensorMux.addChild(this);
      if (isAfterEventLoopInit()) {
        createLightSensor();
      }
    }
  }

  /**
   * Channel property getter method.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The channel.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public int Channel() {
    return channel;
  }

  /**
   * Channel property setter method.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_CHANNEL_NUMBER,
      defaultValue = "1")
  @SimpleProperty(userVisible = false)
  public void Channel(int channel) {
    // TODO: make sure the channel is valid. What are the limits?
    this.channel = channel;
    if (isAfterEventLoopInit()) {
      destroyLightSensor();
      createLightSensor();
    }
  }

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
    if (lightSensor != null) {
      // TODO(4.0): add code
      /*
      lightSensor.enableLed(enableLed);
      */
    }
  }

  /**
   * Light property getter method
   */
  @SimpleProperty(description = "The light detected by the sensor, on a scale of 0 to 1.",
      category = PropertyCategory.BEHAVIOR)
  public double LightLevel() {
    return (lightSensor != null)
        ? lightSensor.getLight() // TODO(4.0): getLightLevel()
        : 0;
  }

  /**
   * Status property getter method
   */
  @SimpleProperty(description = "The Status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    return (lightSensor != null)
        ? lightSensor.status()
        : "";
  }

  private void createLightSensor() {
    if (ftcSensorMux != null) {
      lightSensor = ftcSensorMux.getHiTechnicDeviceManager().createNxtLightSensor(
          ftcSensorMux.getSensorMux(), channel);
    }
  }

  private void destroyLightSensor() {
    if (lightSensor != null) {
      lightSensor = null;
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroyLightSensor();
  }

  // FtcSensorMux.Child implementation

  @Override
  public void createChild() {
    createLightSensor();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    sb.append("lightSensor is ").append((lightSensor == null) ? "null" : "not null").append("\n");
  }

  @Override
  public void destroyChild() {
    destroyLightSensor();
  }
}
