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

import com.qualcomm.robotcore.hardware.AccelerationSensor;

/**
 * A component that provides an interface to an acceleration sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ACCELERATION_SENSOR_COMPONENT_VERSION,
    description = "A component that provides an interface to an acceleration sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "robotcore.jar")
public final class FtcAccelerationSensor extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcSensorMux.Child {

  private FtcSensorMux ftcSensorMux;
  private int channel = 1;
  private AccelerationSensor accelerationSensor;
  
  /**
   * Creates a new FtcAccelerationSensor component.
   */
  public FtcAccelerationSensor(ComponentContainer container) {
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
        destroyAccelerationSensor();
      }
      this.ftcSensorMux.removeChild(this);
      this.ftcSensorMux = null;
    }

    if (ftcSensorMux != null) {
      this.ftcSensorMux = ftcSensorMux;
      this.ftcSensorMux.addChild(this);
      if (isAfterEventLoopInit()) {
        createAccelerationSensor();
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
      destroyAccelerationSensor();
      createAccelerationSensor();
    }
  }

  /**
   * X Acceleration property getter method
   */
  @SimpleProperty(description = "The X Acceleration, in g's.",
      category = PropertyCategory.BEHAVIOR)
  public double XAccel() {
    return (accelerationSensor != null)
        ? accelerationSensor.getAcceleration().x
        : 0;
  }

  /**
   * Y Acceleration property getter method
   */
  @SimpleProperty(description = "The Y Acceleration, in g's.",
      category = PropertyCategory.BEHAVIOR)
  public double YAccel() {
    return (accelerationSensor != null)
        ? accelerationSensor.getAcceleration().y
        : 0;
  }

  /**
   * Z Acceleration property getter method
   */
  @SimpleProperty(description = "The Z Acceleration, in g's.",
      category = PropertyCategory.BEHAVIOR)
  public double ZAccel() {
    return (accelerationSensor != null)
        ? accelerationSensor.getAcceleration().z
        : 0;
  }

  /**
   * Status property getter method
   */
  @SimpleProperty(description = "The Status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    return (accelerationSensor != null)
        ? accelerationSensor.status()
        : "";
  }

  private void createAccelerationSensor() {
    if (ftcSensorMux != null) {
      accelerationSensor = ftcSensorMux.getHiTechnicDeviceManager().createNxtAccelerationSensor(
          ftcSensorMux.getSensorMux(), channel);
    }
  }

  private void destroyAccelerationSensor() {
    accelerationSensor = null;
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroyAccelerationSensor();
  }

  // FtcSensorMux.Child implementation

  @Override
  public void createChild() {
    createAccelerationSensor();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    sb.append("accelerationSensor is ").append((accelerationSensor == null) ? "null" : "not null").append("\n");
  }

  @Override
  public void destroyChild() {
    destroyAccelerationSensor();
  }
}
