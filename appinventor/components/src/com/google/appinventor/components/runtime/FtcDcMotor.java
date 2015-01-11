// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.hitechnic.HiTechnicUsbDcMotorController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotor.Direction;
import com.qualcomm.robotcore.hardware.DcMotorController;

import android.util.Log;

/**
 * A component that provides an interface to a DC motor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_DC_MOTOR_COMPONENT_VERSION,
    description = "A component that provides an interface to a DC motor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "hitechnic.jar,robotcore.jar")
public final class FtcDcMotor extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcDcMotorController.Child {
  private FtcDcMotorController ftcDcMotorController;
  private int portNumber = 1;
  private byte motorChannelMode;
  private Direction direction = Direction.FORWARD;
  private DcMotor dcMotor;

  /**
   * Creates a new FtcDcMotor component.
   */
  public FtcDcMotor(ComponentContainer container) {
    super(container.$form());
  }

  private boolean isAfterEventLoopInit() {
    return (ftcDcMotorController != null)
        ? ftcDcMotorController.isAfterEventLoopInit()
        : false;
  }

  // Properties

  /**
   * FtcDcMotorController property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The FtcDcMotorController component that this DC Motor belongs to.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public FtcDcMotorController FtcDcMotorController() {
    return ftcDcMotorController;
  }

  /**
   * FtcDcMotorController property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_DC_MOTOR_CONTROLLER,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void FtcDcMotorController(FtcDcMotorController ftcDcMotorController) {
    if (this.ftcDcMotorController != null) {
      if (isAfterEventLoopInit()) {
        destroyDcMotor();
      }
      this.ftcDcMotorController.removeChild(this);
      this.ftcDcMotorController = null;
    }

    if (ftcDcMotorController != null) {
      this.ftcDcMotorController = ftcDcMotorController;
      this.ftcDcMotorController.addChild(this);
      if (isAfterEventLoopInit()) {
        createDcMotor();
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
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FTC_DC_MOTOR_PORT_NUMBER,
      defaultValue = "1")
  @SimpleProperty(userVisible = false)
  public void PortNumber(int portNumber) {
    // TODO: make sure the motor is valid. What are the limits?
    this.portNumber = portNumber;
    if (isAfterEventLoopInit()) {
      destroyDcMotor();
      createDcMotor();
    }
  }

  /**
   * Forward property getter.
   */
  @SimpleProperty(description = "Whether this motor should spin forward.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Forward() {
    if (dcMotor != null) {
      return dcMotor.getDirection() == Direction.FORWARD;
    }
    return direction == Direction.FORWARD;
  }

  /**
   * Forward property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Forward(boolean forward) {
    this.direction = forward ? Direction.FORWARD : Direction.REVERSE;
    // TODO(4.0): add code
    /*
    if (dcMotor != null) {
      dcMotor.setDirection(direction);
    }
    */
    // TODO(4.0): remove code begin
    if (isAfterEventLoopInit()) {
      destroyDcMotor();
      createDcMotor();
    }
    // TODO(4.0): remove code end
  }

  /**
   * MotorChannelMode property getter.
   */
  @SimpleProperty(description = "The motor channel mode flags.",
      category = PropertyCategory.BEHAVIOR)
  public int MotorChannelMode() {
    return motorChannelMode;
  }

  /**
   * MotorChannelMode property setter.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "0")
  @SimpleProperty
  public void MotorChannelMode(int motorChannelMode) {
    // TODO(4.0): change this code to work with RunMode enum
    int n = motorChannelMode;
    byte b = (byte) n;
    n = n >> 8;
    if (n != 0 && n != -1) {
      form.dispatchErrorOccurredEvent(this, "MotorChannelMode",
          ErrorMessages.ERROR_FTC_COULD_NOT_FIT_CHANNEL_MODE_IN_BYTE, motorChannelMode);
      return;
    }
    this.motorChannelMode = b;
    // TODO(4.0): add code
    /*
    if (dcMotor != null) {
      dcMotor.setMotorChannelMode(portNumber, motorChannelMode);
    }
    */
    // TODO(4.0): remove code begin
    if (isAfterEventLoopInit()) {
      destroyDcMotor();
      createDcMotor();
    }
    // TODO(4.0): remove code end
  }

  /**
   * Power property getter.
   */
  @SimpleProperty(description = "The current motor power; must be between -1 and 1.",
      category = PropertyCategory.BEHAVIOR)
  public double Power() {
    if (dcMotor != null) {
      return dcMotor.getPower();
    }
    return 0.0;
  }

  /**
   * Power property setter.
   */
  @SimpleProperty
  public void Power(double power) {
    if (power >= -1.0 && power <= 1.0) {
      if (dcMotor != null) {
        dcMotor.setPower(power);
      }
    }
  }

  /**
   * PowerFloat property getter.
   */
  @SimpleProperty(description = "Whether the motor power is set to float",
      category = PropertyCategory.BEHAVIOR)
  public boolean PowerFloat() {
    if (dcMotor != null) {
      return dcMotor.getPowerFloat();
    }
    return true;
  }

  // TODO: add properties MotorTargetPosition, MotorCurrentPosition, GearRatio, DifferentialControlLoopCoefficients

  // Note that this is a function, not a property because there is no parameter. You can make a
  // motor float, but if it is floating, you can't make it NOT float by setting the Float property.
  // You have to use the Power property if you want to make it NOT float.
  @SimpleFunction(description = "Allow the motor to float.")
  public void Float() {
    if (dcMotor != null) {
      dcMotor.setPowerFloat();
    }
  }

  private void createDcMotor() {
    if (ftcDcMotorController != null) {
      DcMotorController dcMotorController = ftcDcMotorController.getDcMotorController();
      dcMotor = new DcMotor(dcMotorController, portNumber, direction);
      // TODO(4.0): motorChannelMode is now an enum called RunMode
      dcMotorController.setMotorChannelMode(portNumber, motorChannelMode);
    }
  }

  private void destroyDcMotor() {
    if (dcMotor != null) {
      dcMotor.setPowerFloat();
      dcMotor = null;
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    destroyDcMotor();
  }

  // FtcDcMotorController.Child overrides

  @Override
  public void createChild() {
    createDcMotor();
  }

  @Override
  public void debugChild(StringBuilder sb) {
    sb.append("dcMotor is ").append((dcMotor == null) ? "null" : "not null").append("\n");
  }

  @Override
  public void destroyChild() {
    destroyDcMotor();
  }
}
