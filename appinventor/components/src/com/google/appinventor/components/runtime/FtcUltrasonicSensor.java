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

// TODO(4.0): add code
/*
import com.qualcomm.robotcore.hardware.UltrasonicSensor;
*/

/**
 * A component that provides an interface to an ultrasonic sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ULTRASONIC_SENSOR_COMPONENT_VERSION,
    description = "A component that provides an interface to an ultrasonic sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "robotcore.jar")
public final class FtcUltrasonicSensor extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcSensorMux.Child {

  private FtcSensorMux ftcSensorMux;
  private int channel = 1;
  // TODO(4.0): add code
  /*
  private UltrasonicSensor ultrasonicSensor;
  */
  
  /**
   * Creates a new FtcUltrasonicSensor component.
   */
  public FtcUltrasonicSensor(ComponentContainer container) {
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
  @SimpleProperty(description = "The FtcSensorMux component that this ultrasonic sensor belongs to.",
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
        destroyUltrasonicSensor();
      }
      this.ftcSensorMux.removeChild(this);
      this.ftcSensorMux = null;
    }

    if (ftcSensorMux != null) {
      this.ftcSensorMux = ftcSensorMux;
      this.ftcSensorMux.addChild(this);
      if (isAfterEventLoopInit()) {
        createUltrasonicSensor();
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
      destroyUltrasonicSensor();
      createUltrasonicSensor();
    }
  }

  /**
   * UltrasonicLevel property getter method
   */
  @SimpleProperty(description = "The UltrasonicLevel.",
      category = PropertyCategory.BEHAVIOR)
  public double UltrasonicLevel() {
    // TODO(4.0): add code
    /*
    return (ultrasonicSensor != null)
        ? ultrasonicSensor.getUltrasonicLevel()
        : 0;
    */
    // TODO(4.0): remove code begin
    return 0;
    // TODO(4.0): remove code end
  }

  private void createUltrasonicSensor() {
    if (ftcSensorMux != null) {
      // TODO(4.0): add code
      /*
      ultrasonicSensor = ftcSensorMux.getHiTechnicDeviceManager().createNxtUltrasonicSensor(
          ftcSensorMux.getSensorMux(), channel);
      */
    }
  }

  private void destroyUltrasonicSensor() {
    // TODO(4.0): add code
    /*
    ultrasonicSensor = null;
    */
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroyUltrasonicSensor();
  }

  // FtcSensorMux.Child implementation

  @Override
  public void createChild() {
    createUltrasonicSensor();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    // TODO(4.0): add code
    /*
    sb.append("ultrasonicSensor is ").append((ultrasonicSensor == null) ? "null" : "not null").append("\n");
    */
  }

  @Override
  public void destroyChild() {
    destroyUltrasonicSensor();
  }
}
