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
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController.Mode;

/**
 * A component for a single digital channel of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_DIGITAL_CHANNEL_COMPONENT_VERSION,
    description = "A component for a single digital channel of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcDigitalChannel extends FtcHardwareDevice {

  private volatile DigitalChannel digitalChannel;

  /**
   * Creates a new FtcDigitalChannel component.
   */
  public FtcDigitalChannel(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

  /**
   * Mode_INPUT property getter.
   */
  @SimpleProperty(description = "The constant for Mode_INPUT.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode_INPUT() {
    return Mode.INPUT.toString();
  }

  /**
   * Mode_OUTPUT property getter.
   */
  @SimpleProperty(description = "The constant for Mode_OUTPUT.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode_OUTPUT() {
    return Mode.OUTPUT.toString();
  }

  /**
   * Mode property getter.
   */
  @SimpleProperty(description = "The mode of the digital channel; INPUT or OUTPUT.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode() {
    if (digitalChannel != null) {
      try {
        Mode mode = digitalChannel.getMode();
        if (mode != null) {
          return mode.toString();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Mode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  /**
   * Mode property setter.
   */
  @SimpleProperty
  public void Mode(String modeString) {
    if (digitalChannel != null) {
      try {
        for (Mode mode : Mode.values()) {
          if (mode.toString().equalsIgnoreCase(modeString)) {
            digitalChannel.setMode(mode);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "Mode",
            ErrorMessages.ERROR_FTC_INVALID_DIGITAL_CHANNEL_MODE, modeString);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Mode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * State property getter.
   */
  @SimpleProperty(description = "The state of the digital channel. If it's in OUTPUT mode, " +
      "this will return the output bit. If the channel is in INPUT mode, this will return the " +
      "input bit.",
      category = PropertyCategory.BEHAVIOR)
  public boolean State() {
    if (digitalChannel != null) {
      try {
        return digitalChannel.getState();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "State",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  /**
   * State property setter.
   */
  @SimpleProperty
  public void State(boolean state) {
    if (digitalChannel != null) {
      try {
        digitalChannel.setState(state);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "State",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void initHardwareDevice(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      digitalChannel = hardwareMap.digitalChannel.get(getDeviceName());
      if (digitalChannel == null) {
        deviceNotFound("DigitalChannel", hardwareMap.digitalChannel);
      }
    }
  }

  @Override
  public void clearHardwareDevice() {
    digitalChannel = null;
  }
}
