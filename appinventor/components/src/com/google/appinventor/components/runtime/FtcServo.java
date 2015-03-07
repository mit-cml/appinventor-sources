// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.ftc.FtcHardwareDevice;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.Servo.Direction;

/**
 * A component for a servo of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_SERVO_COMPONENT_VERSION,
    description = "A component for a servo of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcServo extends FtcHardwareDevice {

  private volatile Servo servo;

  /**
   * Creates a new FtcServo component.
   */
  public FtcServo(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

  /**
   * Direction property getter.
   */
  @SimpleProperty(description = "Whether this servo should spin forward or reverse.",
      category = PropertyCategory.BEHAVIOR)
  public String Direction() {
    if (servo != null) {
      return servo.getDirection().toString();
    }
    return Direction.FORWARD.toString();
  }

  /**
   * Direction property setter.
   */
  @SimpleProperty
  public void Direction(String directionString) {
    for (Direction direction : Direction.values()) {
      if (directionString.equalsIgnoreCase(direction.toString())) {
        if (servo != null) {
          servo.setDirection(direction);
        }
        return;
      }
    }

    form.dispatchErrorOccurredEvent(this, "Direction",
        ErrorMessages.ERROR_FTC_INVALID_DIRECTION, directionString);
  }

  /**
   * Position property getter.
   */
  @SimpleProperty(description = "The current motor position, between 0.0 and 1.0.",
      category = PropertyCategory.BEHAVIOR)
  public double Position() {
    if (servo != null) {
      return servo.getPosition();
    }
    return 0.0;
  }

  /**
   * Position property setter.
   */
  @SimpleProperty
  public void Position(double position) {
    if (servo != null) {
      servo.setPosition(position);
    }
  }

  // Functions

  @SimpleFunction(description = "Sets the scale range of this servo.")
  public void ScaleRange(double scaleRangeMin, double scaleRangeMax) {
    if (servo != null) {
      try {
        servo.scaleRange(scaleRangeMin, scaleRangeMax);
      } catch (IllegalArgumentException e) {
        form.dispatchErrorOccurredEvent(this, "ScaleRange",
            ErrorMessages.ERROR_FTC_INVALID_SCALE_RANGE, scaleRangeMin, scaleRangeMin);
      }
    }
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void debugHardwareDevice(StringBuilder sb) {
    sb.append("servo is ").append((servo == null) ? "null" : "not null").append("\n");
  }

  // FtcHardwareDevice implementation

  @Override
  protected void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      servo = hardwareMap.servo.get(getDeviceName());
    }
  }

  @Override
  protected void clearHardwareDevice() {
    servo = null;
  }
}
