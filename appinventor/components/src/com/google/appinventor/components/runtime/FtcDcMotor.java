// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
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
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcDcMotor extends FtcHardwareDevice {

  private volatile DcMotor dcMotor;

  /**
   * Creates a new FtcDcMotor component.
   */
  public FtcDcMotor(ComponentContainer container) {
    super(container.$form());
  }

  protected DcMotor getDcMotor() {
    return dcMotor;
  }

  /**
   * Direction_FORWARD property getter.
   */
  @SimpleProperty(description = "The constant for Direction_FORWARD.",
      category = PropertyCategory.BEHAVIOR)
  public String Direction_FORWARD() {
    return Direction.FORWARD.toString();
  }

  /**
   * Direction_REVERSE property getter.
   */
  @SimpleProperty(description = "The constant for Direction_REVERSE.",
      category = PropertyCategory.BEHAVIOR)
  public String Direction_REVERSE() {
    return Direction.REVERSE.toString();
  }

  /**
   * Direction property setter.
   */
  @SimpleProperty
  public void Direction(String direction) {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        try {
          int n = Integer.decode(direction);
          if (n == 1) {
            dcMotor.setDirection(Direction.FORWARD);
            return;
          }
          if (n == -1) {
            dcMotor.setDirection(Direction.REVERSE);
            return;
          }
        } catch (NumberFormatException e) {
          // Code below will try to interpret direction as a Direction enum string.
        }
        
        for (Direction directionValue : Direction.values()) {
          if (directionValue.toString().equalsIgnoreCase(direction)) {
            dcMotor.setDirection(directionValue);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "Direction",
            ErrorMessages.ERROR_FTC_INVALID_DIRECTION, direction);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Direction",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * Direction property getter.
   */
  @SimpleProperty(description = "Whether this motor should spin forward or reverse.\n" +
      "Valid values are Direction_FORWARD or Direction_REVERSE.",
      category = PropertyCategory.BEHAVIOR)
  public String Direction() {
    checkHardwareDevice();
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
    return "";
  }

  /**
   * PortNumber property getter.
   */
  @SimpleProperty(description = "The port number.",
      category = PropertyCategory.BEHAVIOR)
  public int PortNumber() {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        return dcMotor.getPortNumber();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "PortNumber",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Power property setter.
   */
  @SimpleProperty
  public void Power(double power) {
    checkHardwareDevice();
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
   * Power property getter.
   */
  @SimpleProperty(description = "The current motor power, between -1 and 1.",
      category = PropertyCategory.BEHAVIOR)
  public double Power() {
    checkHardwareDevice();
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

  @SimpleFunction(description = "Whether the motor is busy.")
  public boolean IsBusy() {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        return dcMotor.isBusy();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsBusy",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Allow the motor to float.")
  public void SetPowerFloat() {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        dcMotor.setPowerFloat();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetPowerFloat",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Is motor power set to float?")
  public boolean GetPowerFloat() {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        return dcMotor.getPowerFloat();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetPowerFloat",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return true;
  }

  /**
   * TargetPosition property setter.
   */
  @SimpleProperty
  public void TargetPosition(int position) {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        dcMotor.setTargetPosition(position);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "TargetPosition",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * TargetPosition property getter.
   */
  @SimpleProperty(description = "The motor target position. If this motor has been set to " +
      "REVERSE, the value will be multiplied by -1.",
      category = PropertyCategory.BEHAVIOR)
  public int TargetPosition() {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        return dcMotor.getTargetPosition();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "TargetPosition",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * CurrentPosition property getter.
   */
  @SimpleProperty(description = "The current encoder value. If this motor has been set to " +
      "REVERSE, the value will be multiplied by -1.",
      category = PropertyCategory.BEHAVIOR)
  public int CurrentPosition() {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        return dcMotor.getCurrentPosition();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "CurrentPosition",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * RunMode_RUN_USING_ENCODERS property getter.
   */
  @SimpleProperty(description = "The constant for RunMode_RUN_USING_ENCODERS.",
      category = PropertyCategory.BEHAVIOR)
  public String RunMode_RUN_USING_ENCODERS() {
    return RunMode.RUN_USING_ENCODERS.toString();
  }

  /**
   * RunMode_RUN_WITHOUT_ENCODERS property getter.
   */
  @SimpleProperty(description = "The constant for RunMode_RUN_WITHOUT_ENCODERS.",
      category = PropertyCategory.BEHAVIOR)
  public String RunMode_RUN_WITHOUT_ENCODERS() {
    return RunMode.RUN_WITHOUT_ENCODERS.toString();
  }

  /**
   * RunMode_RUN_TO_POSITION property getter.
   */
  @SimpleProperty(description = "The constant for RunMode_RUN_TO_POSITION.",
      category = PropertyCategory.BEHAVIOR)
  public String RunMode_RUN_TO_POSITION() {
    return RunMode.RUN_TO_POSITION.toString();
  }

  /**
   * RunMode_RESET_ENCODERS property getter.
   */
  @SimpleProperty(description = "The constant for RunMode_RESET_ENCODERS.",
      category = PropertyCategory.BEHAVIOR)
  public String RunMode_RESET_ENCODERS() {
    return RunMode.RESET_ENCODERS.toString();
  }

  /**
   * Mode property setter.
   */
  @SimpleProperty
  public void Mode(String runMode) {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        for (RunMode runModeValue : RunMode.values()) {
          if (runModeValue.toString().equalsIgnoreCase(runMode)) {
            dcMotor.setMode(runModeValue);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "Mode",
            ErrorMessages.ERROR_FTC_INVALID_DC_MOTOR_RUN_MODE, runMode);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Mode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * Mode property getter.
   */
  @SimpleProperty(description = "The run mode.\n" +
      "Valid values are RunMode_RUN_USING_ENCODERS, RunMode_RUN_WITHOUT_ENCODERS, " +
      "RunMode_RUN_TO_POSITION, or RunMode_RESET_ENCODERS.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode() {
    checkHardwareDevice();
    if (dcMotor != null) {
      try {
        RunMode mode = dcMotor.getMode();
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

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    dcMotor = hardwareMap.dcMotor.get(getDeviceName());
    return dcMotor;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("DcMotor", hardwareMap.dcMotor);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    dcMotor = null;
  }
}
