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

import com.qualcomm.robotcore.hardware.IrSeekerSensor;

/**
 * A component that provides an interface to an IR seeker sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_IR_SEEKER_SENSOR_COMPONENT_VERSION,
    description = "A component that provides an interface to an IR seeker sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "robotcore.jar")
public final class FtcIrSeekerSensor extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcSensorMux.Child {

  private FtcSensorMux ftcSensorMux;
  private int channel = 1;
  private String mode = "DC";
  private IrSeekerSensor irSeekerSensor;
  
  /**
   * Creates a new FtcIrSeekerSensor component.
   */
  public FtcIrSeekerSensor(ComponentContainer container) {
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
  @SimpleProperty(description = "The FtcSensorMux component that this IR seeker sensor belongs to.",
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
        destroyIrSeekerSensor();
      }
      this.ftcSensorMux.removeChild(this);
      this.ftcSensorMux = null;
    }

    if (ftcSensorMux != null) {
      this.ftcSensorMux = ftcSensorMux;
      this.ftcSensorMux.addChild(this);
      if (isAfterEventLoopInit()) {
        createIrSeekerSensor();
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
      destroyIrSeekerSensor();
      createIrSeekerSensor();
    }
  }

  /**
   * Mode property getter method.
   */
  @SimpleProperty(description = "The mode; DC or AC.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode() {
    // TODO(4.0): add code
    /*
    return (irSeekerSensor != null)
        ? irSeekerSensor.getMode()
        : mode;
    */
    // TODO(4.0): remove code begin
    return mode;
    // TODO(4.0): remove code end
  }

  /**
   * Mode property setter method.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "DC")
  @SimpleProperty()
  public void Mode(String mode) {
    if (!"DC".equals(mode) && !"AC".equals(mode)) {
      // TODO: trigger an error?
      return;
    }
    this.mode = mode;
    // TODO(4.0): add code
    /*
    if (irSeekerSensor != null) {
      irSeekerSensor.setMode(mode);
    }
    */
  }

  /**
   * SignalDetected property getter method
   */
  @SimpleProperty(description = "The SignalDetected property.",
      category = PropertyCategory.BEHAVIOR)
  public boolean SignalDetected() {
    return (irSeekerSensor != null)
        ? irSeekerSensor.signalDetected()
        : false;
  }

  /**
   * Angle property getter method
   */
  @SimpleProperty(description = "The Angle.",
      category = PropertyCategory.BEHAVIOR)
  public double Angle() {
    return (irSeekerSensor != null && irSeekerSensor.signalDetected())
        ? irSeekerSensor.getAngle()
        : 0;
  }

  /**
   * Strength property getter method
   */
  @SimpleProperty(description = "The Strength.",
      category = PropertyCategory.BEHAVIOR)
  public double Strength() {
    return (irSeekerSensor != null && irSeekerSensor.signalDetected())
        ? irSeekerSensor.getStrength()
        : 0;
  }

  private void createIrSeekerSensor() {
    if (ftcSensorMux != null) {
      irSeekerSensor = ftcSensorMux.getHiTechnicDeviceManager().createNxtIrSeekerSensor(
          ftcSensorMux.getSensorMux(), channel);
      // TODO(4.0): add code
      /*
      irSeekerSensor.setMode(mode);
      */
    }
  }

  private void destroyIrSeekerSensor() {
    irSeekerSensor = null;
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroyIrSeekerSensor();
  }

  // FtcSensorMux.Child implementation

  @Override
  public void createChild() {
    createIrSeekerSensor();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    sb.append("irSeekerSensor is ").append((irSeekerSensor == null) ? "null" : "not null").append("\n");
  }

  @Override
  public void destroyChild() {
    destroyIrSeekerSensor();
  }
}
