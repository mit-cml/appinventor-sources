// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
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
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotor.Direction;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorController.RunMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A component for a DC motor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_DC_MOTOR_COMPONENT_VERSION,
    description = "A component for a DC motor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcDcMotor extends FtcHardwareDevice {

  private volatile DcMotor dcMotor;

  /**
   * Creates a new FtcDcMotor component.
   */
  public FtcDcMotor(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

  /**
   * RunMode property getter.
   */
  @SimpleProperty(description = "The run mode.",
      category = PropertyCategory.BEHAVIOR)
  public String RunMode() {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          RunMode runMode = dcMotorController.getMotorChannelMode(dcMotor.getPortNumber());
          if (runMode != null) {
            return runMode.toString();
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "RunMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return RunMode.RUN.toString();
  }

  /**
   * RunMode property setter.
   */
  @SimpleProperty
  public void RunMode(String runModeString) {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          for (RunMode runMode : RunMode.values()) {
            if (runMode.toString().equalsIgnoreCase(runModeString)) {
              dcMotorController.setMotorChannelMode(dcMotor.getPortNumber(), runMode);
              return;
            }
          }

          form.dispatchErrorOccurredEvent(this, "RunMode",
              ErrorMessages.ERROR_FTC_INVALID_DC_MOTOR_RUN_MODE, runModeString);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "RunMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * Direction property getter.
   */
  @SimpleProperty(description = "Whether this motor should spin forward or reverse.",
      category = PropertyCategory.BEHAVIOR)
  public String Direction() {
    if (dcMotor != null) {
      try {
        Direction direction = dcMotor.getDirection();
        if (direction != null) {
          return direction.toString();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Direction",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return Direction.FORWARD.toString();
  }

  /**
   * Direction property setter.
   */
  @SimpleProperty
  public void Direction(String directionString) {
    if (dcMotor != null) {
      try {
        for (Direction direction : Direction.values()) {
          if (direction.toString().equalsIgnoreCase(directionString)) {
            dcMotor.setDirection(direction);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "Direction",
            ErrorMessages.ERROR_FTC_INVALID_DIRECTION, directionString);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Direction",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * Power property getter.
   */
  @SimpleProperty(description = "The current motor power, between -1 and 1.",
      category = PropertyCategory.BEHAVIOR)
  public double Power() {
    if (dcMotor != null) {
      try {
        return dcMotor.getPower();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Power",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  /**
   * Power property setter.
   */
  @SimpleProperty
  public void Power(double power) {
    if (dcMotor != null) {
      try {
        dcMotor.setPower(power);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Power",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
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
      try {
        return dcMotor.getPowerFloat();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "PowerFloat",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
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
      try {
        dcMotor.setPowerFloat();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Float",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  // FtcHardwareDevice implementation

  @Override
  protected void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      dcMotor = hardwareMap.dcMotor.get(getDeviceName());
    }
  }

  @Override
  protected void clearHardwareDevice() {
    if (dcMotor != null) {
      dcMotor.setPowerFloat();
      dcMotor = null;
    }
  }
}
