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

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;

/**
 * A component that provides an interface to a digital channel of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_DIGITAL_CHANNEL_COMPONENT_VERSION,
    description = "A component that provides an interface to a digital channel of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "robotcore.jar")
public final class FtcDigitalChannel extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcSensorMux.Child {

  private FtcSensorMux ftcSensorMux;
  private int channel = 1;
  private boolean state = false;
  private DigitalChannel digitalChannel;
  
  /**
   * Creates a new FtcDigitalChannel component.
   */
  public FtcDigitalChannel(ComponentContainer container) {
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
  @SimpleProperty(description = "The FtcSensorMux component that this digital channel belongs to.",
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
        destroyDigitalChannel();
      }
      this.ftcSensorMux.removeChild(this);
      this.ftcSensorMux = null;
    }

    if (ftcSensorMux != null) {
      this.ftcSensorMux = ftcSensorMux;
      this.ftcSensorMux.addChild(this);
      if (isAfterEventLoopInit()) {
        createDigitalChannel();
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
      destroyDigitalChannel();
      createDigitalChannel();
    }
  }

  /**
   * State property getter method
   */
  @SimpleProperty(description = "The state.",
      category = PropertyCategory.BEHAVIOR)
  public boolean State() {
    return (digitalChannel != null)
        ? digitalChannel.getState()
        : state;
  }

  /**
   * State property setter method
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void State(boolean state) {
    this.state = state;
    if (digitalChannel != null) {
      digitalChannel.setState(state);
    }
  }

  private void createDigitalChannel() {
    if (ftcSensorMux != null) {
      digitalChannel = new DigitalChannel(ftcSensorMux.getSensorMux(), channel);
      digitalChannel.setMode(DigitalChannelController.Mode.OUTPUT); // TODO: should mode be a property?
      digitalChannel.setState(state);
    }
  }

  private void destroyDigitalChannel() {
    if (digitalChannel != null) {
      digitalChannel.setState(false);
      digitalChannel = null;
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroyDigitalChannel();
  }

  // FtcSensorMux.Child implementation

  @Override
  public void createChild() {
    createDigitalChannel();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    sb.append("digitalChannel is ").append((digitalChannel == null) ? "null" : "not null").append("\n");
  }

  @Override
  public void destroyChild() {
    destroyDigitalChannel();
  }
}
