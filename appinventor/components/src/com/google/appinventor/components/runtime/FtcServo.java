// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.qualcomm.robotcore.hardware.Servo;
// TODO(4.0): add code
/*
import com.qualcomm.robotcore.hardware.Servo.Direction;
*/

/**
 * A component that provides an interface to a servo of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_SERVO_COMPONENT_VERSION,
    description = "A component that provides an interface to a servo of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "robotcore.jar")
public final class FtcServo extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcServoController.Child {

  private FtcServoController ftcServoController;
  private int portNumber = 1;
  private Servo servo;
  // TODO(4.0): add code
  /*
  private Direction direction = Direction.FORWARD;
  */
  private double scaleRangeMin = 0;
  private double scaleRangeMax = 1;
  private double position = 0;

  /**
   * Creates a new FtcServo component.
   */
  public FtcServo(ComponentContainer container) {
    super(container.$form());
  }

  private boolean isAfterEventLoopInit() {
    return (ftcServoController != null)
        ? ftcServoController.isAfterEventLoopInit()
        : false;
  }

  // Properties

  /**
   * FtcServoController property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The FtcServoController component that this servo belongs to.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public FtcServoController FtcServoController() {
    return ftcServoController;
  }

  /**
   * FtcServoController property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_SERVO_CONTROLLER,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void FtcServoController(FtcServoController ftcServoController) {
    if (this.ftcServoController != null) {
      if (isAfterEventLoopInit()) {
        destroyServo();
      }
      this.ftcServoController.removeChild(this);
      this.ftcServoController = null;
    }

    if (ftcServoController != null) {
      this.ftcServoController = ftcServoController;
      this.ftcServoController.addChild(this);
      if (isAfterEventLoopInit()) {
        createServo();
      }
    }
  }

  /**
   * PortNumber property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The port number on the DC motor controller.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public int PortNumber() {
    return portNumber;
  }

  /**
   * PortNumber property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_SERVO_PORT_NUMBER,
      defaultValue = "1")
  @SimpleProperty(userVisible = false)
  public void PortNumber(int portNumber) {
    // TODO: make sure the motor is valid. What are the limits?
    this.portNumber = portNumber;
    if (isAfterEventLoopInit()) {
      destroyServo();
      createServo();
    }
  }

  /**
   * Forward property getter.
   */
  @SimpleProperty(description = "Whether this motor should spin forward.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Forward() {
    // TODO(4.0): add code
    /*
    if (servo != null) {
      return servo.getDirection() == Direction.FORWARD;
    }
    return direction == Direction.FORWARD;
    */
    // TODO(4.0): remove code begin
    return true;
    // TODO(4.0): remove code end
  }

  /**
   * Forward property setter.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Forward(boolean forward) {
    // TODO(4.0): add code
    /*
    this.direction = forward ? Direction.FORWARD : Direction.REVERSE;
    if (servo != null) {
      servo.setDirection(direction);
    }
    */
  }

  /**
   * ScaleRangeMinimum property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The scale range minimum motor position; must be between 0 and 1.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public double ScaleRangeMinimum() {
    return scaleRangeMin;
  }

  /**
   * ScaleRangeMinimum property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "0")
  @SimpleProperty(userVisible = false)
  public void ScaleRangeMinimum(double scaleRangeMin) {
    if (scaleRangeMin >= 0.0 && scaleRangeMin <= 1.0) {
      this.scaleRangeMin = scaleRangeMin;
    }
  }

  /**
   * ScaleRangeMaximum property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The scale range maximum motor position; must be between 0 and 1.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public double ScaleRangeMaximum() {
    return scaleRangeMax;
  }

  /**
   * ScaleRangeMaximum property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "1")
  @SimpleProperty(userVisible = false)
  public void ScaleRangeMaximum(double scaleRangeMax) {
    if (scaleRangeMax >= 0.0 && scaleRangeMax <= 1.0) {
      this.scaleRangeMax = scaleRangeMax;
    }
  }

  /**
   * Position property getter.
   */
  @SimpleProperty(description = "The current motor position; must be between 0 and 1.",
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
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "0")
  @SimpleProperty
  public void Position(double position) {
    if (position >= 0.0 && position <= 1.0) {
      this.position = position;
      if (servo != null) {
        if (position >= scaleRangeMin && position <= scaleRangeMax) {
          servo.setPosition(position);
        }
      }
    }
  }

  // private

  private void createServo() {
    if (ftcServoController != null) {
      servo = new Servo(ftcServoController.getServoController(), portNumber);
      if (scaleRangeMin < scaleRangeMax) {
        servo.scaleRange(scaleRangeMin, scaleRangeMax);
      }
      if (position >= scaleRangeMin && position <= scaleRangeMax) {
        servo.setPosition(position);
      }
    }
  }

  private void destroyServo() {
    if (servo != null) {
      servo = null;
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroyServo();
  }

  // FtcServoController.Child overrides

  @Override
  public void createChild() {
    createServo();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    sb.append("servo is ").append((servo == null) ? "null" : "not null").append("\n");
  }

  @Override
  public void destroyChild() {
    destroyServo();
  }
}
