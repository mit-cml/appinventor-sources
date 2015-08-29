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
  public void Direction(String directionString) {
    if (dcMotor != null) {
      try {
        try {
          int n = Integer.decode(directionString);
          if (n == 1) {
            dcMotor.setDirection(Direction.FORWARD);
            return;
          }
          if (n == -1) {
            dcMotor.setDirection(Direction.REVERSE);
            return;
          }
        } catch (NumberFormatException e) {
          // Code below will try to interpret directionString as a Direction enum string.
        }
        
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
    return "";
  }

  /**
   * PortNumber property getter.
   */
  @SimpleProperty(description = "The port number.",
      category = PropertyCategory.BEHAVIOR)
  public int PortNumber() {
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
  @SimpleProperty(description = "The motor target position.",
      category = PropertyCategory.BEHAVIOR)
  public int TargetPosition() {
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
  @SimpleProperty(description = "The current motor position.",
      category = PropertyCategory.BEHAVIOR)
  public int CurrentPosition() {
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
   * ChannelMode property setter.
   */
  @SimpleProperty
  public void ChannelMode(String modeString) {
    if (dcMotor != null) {
      try {
        for (RunMode mode : RunMode.values()) {
          if (mode.toString().equalsIgnoreCase(modeString)) {
            dcMotor.setChannelMode(mode);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "ChannelMode",
            ErrorMessages.ERROR_FTC_INVALID_DC_MOTOR_RUN_MODE, modeString);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ChannelMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * ChannelMode property getter.
   */
  @SimpleProperty(description = "The channel mode.",
      category = PropertyCategory.BEHAVIOR)
  public String ChannelMode() {
    if (dcMotor != null) {
      try {
        RunMode mode = dcMotor.getChannelMode();
        if (mode != null) {
          return mode.toString();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ChannelMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      dcMotor = hardwareMap.dcMotor.get(getDeviceName());
      if (dcMotor == null) {
        deviceNotFound("DcMotor", hardwareMap.dcMotor);
      }
    }
    return dcMotor;
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    dcMotor = null;
  }
}
