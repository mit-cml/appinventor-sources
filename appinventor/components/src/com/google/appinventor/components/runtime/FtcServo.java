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

  private volatile Direction direction = Direction.FORWARD;
  private volatile double scaleRangeMin = 0;
  private volatile double scaleRangeMax = 1;
  private volatile double position = 0;
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
    return direction.toString();
  }

  /**
   * Direction property setter.
   */
  @SimpleProperty
  public void Direction(String directionString) {
    for (Direction iDirection : Direction.values()) {
      if (directionString.equalsIgnoreCase(iDirection.toString())) {
        direction = iDirection;
        setDirection();
        return;
      }
    }

    form.dispatchErrorOccurredEvent(this, "Direction",
        ErrorMessages.ERROR_FTC_INVALID_DIRECTION, directionString);
  }

  /**
   * ScaleRangeMinimum property getter.
   */
  @SimpleProperty(
      description = "The scale range minimum motor position; must be between 0.0 and 1.0.",
      category = PropertyCategory.BEHAVIOR)
  public double ScaleRangeMinimum() {
    return scaleRangeMin;
  }

  /**
   * ScaleRangeMinimum property setter.
   */
  @SimpleProperty
  public void ScaleRangeMinimum(double scaleRangeMin) {
    if (scaleRangeMin >= 0.0 && scaleRangeMin <= 1.0) {
      this.scaleRangeMin = scaleRangeMin;
    }
  }

  /**
   * ScaleRangeMaximum property getter.
   */
  @SimpleProperty(
      description = "The scale range maximum motor position; must be between 0.0 and 1.0.",
      category = PropertyCategory.BEHAVIOR)
  public double ScaleRangeMaximum() {
    return scaleRangeMax;
  }

  /**
   * ScaleRangeMaximum property setter.
   */
  @SimpleProperty
  public void ScaleRangeMaximum(double scaleRangeMax) {
    if (scaleRangeMax >= 0.0 && scaleRangeMax <= 1.0) {
      this.scaleRangeMax = scaleRangeMax;
    }
  }

  /**
   * Position property getter.
   */
  @SimpleProperty(description = "The current motor position; must be between 0.0 and 1.0.",
      category = PropertyCategory.BEHAVIOR)
  public double Position() {
    if (servo != null) {
      position = servo.getPosition();
    }
    return position;
  }

  /**
   * Position property setter.
   */
  @SimpleProperty
  public void Position(double position) {
    if (position >= 0.0 && position <= 1.0) {
      this.position = position;
      setPosition();
      return;
    }

    form.dispatchErrorOccurredEvent(this, "Position",
        ErrorMessages.ERROR_FTC_INVALID_POSITION, position);
  }

  private void setDirection() {
    if (servo != null) {
      servo.setDirection(direction);
    }
  }

  private void setScaleRange() {
    if (servo != null) {
      servo.scaleRange(scaleRangeMin, scaleRangeMax);
    }
  }

  private void setPosition() {
    if (servo != null) {
      servo.setPosition(position);
    }
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void debugHardwareDevice(StringBuilder sb) {
    sb.append("servo is ").append((servo == null) ? "null" : "not null").append("\n");
  }

  // FtcHardwareDevice implementation

  @Override
  void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      servo = hardwareMap.servo.get(getDeviceName());
      setDirection();
      setScaleRange();
      setPosition();
    }
  }

  @Override
  void clearHardwareDevice() {
    servo = null;
  }
}
