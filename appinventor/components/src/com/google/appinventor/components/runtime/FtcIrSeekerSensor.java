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
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IrSeekerSensor;
import com.qualcomm.robotcore.hardware.IrSeekerSensor.Mode;

/**
 * A component for an IR seeker sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_IR_SEEKER_SENSOR_COMPONENT_VERSION,
    description = "A component for an IR seeker sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcIrSeekerSensor extends FtcHardwareDevice {

  private volatile Mode mode = Mode.DC;
  private volatile IrSeekerSensor irSeekerSensor;

  /**
   * Creates a new FtcIrSeekerSensor component.
   */
  public FtcIrSeekerSensor(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

  /**
   * Mode property getter.
   */
  @SimpleProperty(description = "The mode; DC or AC.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode() {
    return mode.toString();
  }

  /**
   * Mode property setter.
   */
  @SimpleProperty
  public void Mode(String modeString) {
    for (Mode iMode : Mode.values()) {
      if (modeString.equalsIgnoreCase(iMode.toString())) {
        mode = iMode;
        if (irSeekerSensor != null) {
          irSeekerSensor.setMode(mode);
        }
        return;
      }
    }

    form.dispatchErrorOccurredEvent(this, "Mode",
        ErrorMessages.ERROR_FTC_INVALID_IR_SEEKER_SENSOR_MODE, modeString);
  }

  /**
   * SignalDetected property getter.
   */
  @SimpleProperty(description = "Whether a signal is detected by the sensor.",
      category = PropertyCategory.BEHAVIOR)
  public boolean SignalDetected() {
    return (irSeekerSensor != null)
        ? irSeekerSensor.signalDetected()
        : false;
  }

  /**
   * Angle property getter.
   */
  @SimpleProperty(description = "The Angle.",
      category = PropertyCategory.BEHAVIOR)
  public double Angle() {
    return (irSeekerSensor != null && irSeekerSensor.signalDetected())
        ? irSeekerSensor.getAngle()
        : 0;
  }

  /**
   * Strength property getter.
   */
  @SimpleProperty(description = "The Strength.",
      category = PropertyCategory.BEHAVIOR)
  public double Strength() {
    return (irSeekerSensor != null && irSeekerSensor.signalDetected())
        ? irSeekerSensor.getStrength()
        : 0;
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void debugHardwareDevice(StringBuilder sb) {
    sb.append("irSeekerSensor is ").append((irSeekerSensor == null) ? "null" : "not null").append("\n");
  }

  // FtcHardwareDevice implementation

  @Override
  void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      irSeekerSensor = hardwareMap.irSeekerSensor.get(getDeviceName());
    }
  }

  @Override
  void clearHardwareDevice() {
    irSeekerSensor = null;
  }
}
