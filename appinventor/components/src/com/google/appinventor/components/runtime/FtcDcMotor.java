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
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

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
   * ChannelMode property getter.
   */
  @SimpleProperty(description = "The channel mode.",
      category = PropertyCategory.BEHAVIOR)
  public String ChannelMode() {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          // TODO(lizlooney): remove controller code and update
          // RunMode mode = dcMotor.getChannelMode();
          RunMode mode = dcMotorController.getMotorChannelMode(dcMotor.getPortNumber());
          if (mode != null) {
            return mode.toString();
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ChannelMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    // TODO(lizlooney): update
    // return RunMode.RUN_USING_ENCODERS.toString();
    return RunMode.RUN.toString();
  }

  /**
   * ChannelMode property setter.
   */
  @SimpleProperty
  public void ChannelMode(String modeString) {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          for (RunMode mode : RunMode.values()) {
            if (mode.toString().equalsIgnoreCase(modeString)) {
              // TODO(lizlooney): remove controller code and update
              // dcMotor.setChannelMode(mode);
              dcMotorController.setMotorChannelMode(dcMotor.getPortNumber(), mode);
              return;
            }
          }

          form.dispatchErrorOccurredEvent(this, "ChannelMode",
              ErrorMessages.ERROR_FTC_INVALID_DC_MOTOR_RUN_MODE, modeString);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ChannelMode",
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

  /**
   * TargetPosition property getter.
   */
  @SimpleProperty(description = "The motor target position, where 1.0 is one full rotation.",
      category = PropertyCategory.BEHAVIOR)
  public double TargetPosition() {
    if (dcMotor != null) {
      try {
        // TODO(lizlooney): update
        // return dcMotor.getTargetPosition();
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          return dcMotorController.getMotorTargetPosition(dcMotor.getPortNumber());
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "MotorTargetPosition",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  /**
   * TargetPosition property setter.
   */
  @SimpleProperty
  public void TargetPosition(double position) {
    if (dcMotor != null) {
      try {
        // TODO(lizlooney): update
        // return dcMotor.setTargetPosition(position);
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          dcMotorController.setMotorTargetPosition(dcMotor.getPortNumber(), position);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "MotorTargetPosition",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * CurrentPosition property getter.
   */
  @SimpleProperty(description = "The current motor position, where 1.0 is one full rotation.",
      category = PropertyCategory.BEHAVIOR)
  public double CurrentPosition() {
    if (dcMotor != null) {
      try {
        // TODO(lizlooney): update
        // return dcMotor.getCurrentPosition(position);
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          return dcMotorController.getMotorCurrentPosition(dcMotor.getPortNumber());
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "MotorCurrentPosition",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  /**
   * GearRatio property getter.
   */
  @SimpleProperty(description = "The gear ratio, from -1.0 to 1.0.",
      category = PropertyCategory.BEHAVIOR)
  public double GearRatio() {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          return dcMotorController.getGearRatio(dcMotor.getPortNumber());
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GearRatio",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  /**
   * GearRatio property setter.
   */
  @SimpleProperty
  public void GearRatio(double ratio) {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          dcMotorController.setGearRatio(dcMotor.getPortNumber(), ratio);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GearRatio",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * DifferentialControlLoopCoefficientP property getter.
   */
  @SimpleProperty(description = "The differential control loop coefficient P.",
      category = PropertyCategory.BEHAVIOR)
  public double DifferentialControlLoopCoefficientP() {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          DifferentialControlLoopCoefficients differentialControlLoopCoefficients =
              dcMotorController.getDifferentialControlLoopCoefficients(dcMotor.getPortNumber());
          if (differentialControlLoopCoefficients != null) {
            return differentialControlLoopCoefficients.p;
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "DifferentialControlLoopCoefficientP",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  /**
   * DifferentialControlLoopCoefficientI property getter.
   */
  @SimpleProperty(description = "The differential control loop coefficient I.",
      category = PropertyCategory.BEHAVIOR)
  public double DifferentialControlLoopCoefficientI() {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          DifferentialControlLoopCoefficients differentialControlLoopCoefficients =
              dcMotorController.getDifferentialControlLoopCoefficients(dcMotor.getPortNumber());
          if (differentialControlLoopCoefficients != null) {
            return differentialControlLoopCoefficients.i;
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "DifferentialControlLoopCoefficientI",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  /**
   * DifferentialControlLoopCoefficientD property getter.
   */
  @SimpleProperty(description = "The differential control loop coefficient D.",
      category = PropertyCategory.BEHAVIOR)
  public double DifferentialControlLoopCoefficientD() {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          DifferentialControlLoopCoefficients differentialControlLoopCoefficients =
              dcMotorController.getDifferentialControlLoopCoefficients(dcMotor.getPortNumber());
          if (differentialControlLoopCoefficients != null) {
            return differentialControlLoopCoefficients.d;
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "DifferentialControlLoopCoefficientD",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  @SimpleFunction(description = "Set the differential control loop coefficients.")
  public void SetDifferentialControlLoopCoefficients(double p, double i, double d) {
    if (dcMotor != null) {
      try {
        DcMotorController dcMotorController = dcMotor.getController();
        if (dcMotorController != null) {
          dcMotorController.setDifferentialControlLoopCoefficients(dcMotor.getPortNumber(),
              new DifferentialControlLoopCoefficients(p, i, d));
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetDifferentialControlLoopCoefficients",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

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
