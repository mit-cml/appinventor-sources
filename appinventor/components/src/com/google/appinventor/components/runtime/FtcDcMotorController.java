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
import com.google.appinventor.components.runtime.collect.Sets;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;

import com.qualcomm.hardware.matrix.MatrixDcMotorController;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbDcMotorController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorController.DeviceMode;
import com.qualcomm.robotcore.hardware.DcMotorController.RunMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

import java.util.Set;

/**
 * A component for a DC motor controller of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_DC_MOTOR_CONTROLLER_COMPONENT_VERSION,
    description = "A component for a DC motor controller of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcHardware.jar,FtcRobotCore.jar")
public final class FtcDcMotorController extends FtcHardwareDevice {

  private volatile DcMotorController dcMotorController;

  /**
   * Creates a new FtcDcMotorController component.
   */
  public FtcDcMotorController(ComponentContainer container) {
    super(container.$form());
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
   * DeviceMode_READ_ONLY property getter.
   */
  @SimpleProperty(description = "The constant for DeviceMode_READ_ONLY.",
      category = PropertyCategory.BEHAVIOR)
  public String DeviceMode_READ_ONLY() {
    return DeviceMode.READ_ONLY.toString();
  }

  /**
   * DeviceMode_WRITE_ONLY property getter.
   */
  @SimpleProperty(description = "The constant for DeviceMode_WRITE_ONLY.",
      category = PropertyCategory.BEHAVIOR)
  public String DeviceMode_WRITE_ONLY() {
    return DeviceMode.WRITE_ONLY.toString();
  }

  /**
   * MotorControllerDeviceMode property setter.
   */
  @SimpleProperty
  public void MotorControllerDeviceMode(String deviceMode) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        for (DeviceMode deviceModeValue : DeviceMode.values()) {
          if (deviceModeValue.toString().equalsIgnoreCase(deviceMode)) {
            dcMotorController.setMotorControllerDeviceMode(deviceModeValue);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "MotorControllerDeviceMode",
            ErrorMessages.ERROR_FTC_INVALID_DC_MOTOR_CONTROLLER_DEVICE_MODE, deviceMode);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "MotorControllerDeviceMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * MotorControllerDeviceMode property getter.
   */
  @SimpleProperty(description = "Set the device into read-only or write-only mode.\n" +
      "Valid values are DeviceMode_READ_ONLY or DeviceMode_WRITE_ONLY.",
      category = PropertyCategory.BEHAVIOR)
  public String MotorControllerDeviceMode() {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        DeviceMode mode = dcMotorController.getMotorControllerDeviceMode();
        if (mode != null) {
          return mode.toString();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "MotorControllerDeviceMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  @SimpleFunction(description = "Set the current channel mode.\n" +
      "Valid values are RunMode_RUN_USING_ENCODERS, RunMode_RUN_WITHOUT_ENCODERS, " +
      "RunMode_RUN_TO_POSITION, or RunMode_RESET_ENCODERS.")
  public void SetMotorChannelMode(int motor, String runMode) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        for (RunMode runModeValue : RunMode.values()) {
          if (runModeValue.toString().equalsIgnoreCase(runMode)) {
            dcMotorController.setMotorChannelMode(motor, runModeValue);
            return;
          }
        }

      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetMotorChannelMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Get the current channel mode.\n" +
      "Valid values are RunMode_RUN_USING_ENCODERS, RunMode_RUN_WITHOUT_ENCODERS, " +
      "RunMode_RUN_TO_POSITION, or RunMode_RESET_ENCODERS.")
  public String GetMotorChannelMode(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        RunMode runMode = dcMotorController.getMotorChannelMode(motor);
        if (runMode != null) {
          return runMode.toString();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetMotorChannelMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  @SimpleFunction(description = "Set the current motor power (from -1.0 to 1.0).")
  public void SetMotorPower(int motor, double power) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        dcMotorController.setMotorPower(motor, power);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetMotorPower",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Get the current motor power.")
  public double GetMotorPower(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        return dcMotorController.getMotorPower(motor);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetMotorPower",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  @SimpleFunction(description = "Is the motor busy?")
  public boolean IsBusy(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        return dcMotorController.isBusy(motor);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsBusy",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Allow motor to float.")
  public void SetMotorPowerFloat(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        dcMotorController.setMotorPowerFloat(motor);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetMotorPowerFloat",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Allow motor to float.")
  public boolean GetMotorPowerFloat(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        return dcMotorController.getMotorPowerFloat(motor);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetMotorPowerFloat",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Set the motor target position.")
  public void SetMotorTargetPosition(int motor, int position) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        dcMotorController.setMotorTargetPosition(motor, position);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetMotorTargetPosition",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Get the current motor target position.")
  public int GetMotorTargetPosition(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        return dcMotorController.getMotorTargetPosition(motor);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetMotorTargetPosition",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Get the current motor position.")
  public int GetMotorCurrentPosition(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        return dcMotorController.getMotorCurrentPosition(motor);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetMotorCurrentPosition",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Set the power for a group of motors, if supported by the controller.")
  public void SetMotorPowerForGroup(YailList listOfFtcDcMotors, int power) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        Set<DcMotor> dcMotors = Sets.newHashSet();
        Object[] array = listOfFtcDcMotors.toArray();
        for (Object o : array) {
          if (o instanceof FtcDcMotor) {
            DcMotor dcMotor = ((FtcDcMotor) o).getDcMotor();
            if (dcMotor != null) {
              if (dcMotor.getController().equals(dcMotorController)) {
                dcMotors.add(dcMotor);
              } else {
                form.dispatchErrorOccurredEvent(this, "SetMotorPowerForGroup",
                    ErrorMessages.ERROR_FTC_INVALID_LIST_OF_FTC_DC_MOTORS);
                return;
              }
            }
          } else {
            form.dispatchErrorOccurredEvent(this, "SetMotorPowerForGroup",
                ErrorMessages.ERROR_FTC_INVALID_LIST_OF_FTC_DC_MOTORS);
            return;
          }
        }
        if (dcMotorController instanceof MatrixDcMotorController) {
          ((MatrixDcMotorController) dcMotorController).setMotorPower(dcMotors, power);
        } else {
          // For other controllers, we just set the power for each motor.
          for (DcMotor dcMotor : dcMotors) {
            dcMotorController.setMotorPower(dcMotor.getPortNumber(), power);
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetMotorPowerForGroup",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * BatteryVoltage property getter.
   */
  @SimpleProperty(description = "Get the battery voltage, if supported by the controller.",
      category = PropertyCategory.BEHAVIOR)
  public double BatteryVoltage() {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        if (dcMotorController instanceof ModernRoboticsUsbDcMotorController) {
          return ((ModernRoboticsUsbDcMotorController) dcMotorController).getVoltage();
        }
        if (dcMotorController instanceof MatrixDcMotorController) {
          return ((MatrixDcMotorController) dcMotorController).getBattery();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "BatteryVoltage",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  @SimpleFunction(description = "Set the gear ratio (from -1.0 to 1.0), if supported by the controller.")
  public void SetGearRatio(int motor, double ratio) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        if (dcMotorController instanceof ModernRoboticsUsbDcMotorController) {
          ((ModernRoboticsUsbDcMotorController) dcMotorController).setGearRatio(motor, ratio);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetGearRatio",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Get the gear ratio, if supported by the controller.")
  public double GetGearRatio(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        if (dcMotorController instanceof ModernRoboticsUsbDcMotorController) {
          return ((ModernRoboticsUsbDcMotorController) dcMotorController).getGearRatio(motor);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetGearRatio",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  @SimpleFunction(description = "Set the differential control loop coefficients, if supported " +
      "by the controller.")
  public void SetDifferentialControlLoopCoefficients(int motor, double p, double i, double d) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        if (dcMotorController instanceof ModernRoboticsUsbDcMotorController) {
          ((ModernRoboticsUsbDcMotorController) dcMotorController)
              .setDifferentialControlLoopCoefficients(motor,
                  new DifferentialControlLoopCoefficients(p, i, d));
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetDifferentialControlLoopCoefficients",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Get the differential control loop coefficient p, if supported " +
      "by the controller.")
  public double GetDifferentialControlLoopCoefficientP(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        if (dcMotorController instanceof ModernRoboticsUsbDcMotorController) {
          DifferentialControlLoopCoefficients pid =
              ((ModernRoboticsUsbDcMotorController) dcMotorController)
                  .getDifferentialControlLoopCoefficients(motor);
          if (pid != null) {
            return pid.p;
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetDifferentialControlLoopCoefficientP",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  @SimpleFunction(description = "Get the differential control loop coefficient i, if supported " +
      "by the controller.")
  public double GetDifferentialControlLoopCoefficientI(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        if (dcMotorController instanceof ModernRoboticsUsbDcMotorController) {
          DifferentialControlLoopCoefficients pid =
              ((ModernRoboticsUsbDcMotorController) dcMotorController)
                  .getDifferentialControlLoopCoefficients(motor);
          if (pid != null) {
            return pid.i;
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetDifferentialControlLoopCoefficientI",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  @SimpleFunction(description = "Get the differential control loop coefficient d, if supported " +
      "by the controller.")
  public double GetDifferentialControlLoopCoefficientD(int motor) {
    checkHardwareDevice();
    if (dcMotorController != null) {
      try {
        if (dcMotorController instanceof ModernRoboticsUsbDcMotorController) {
          DifferentialControlLoopCoefficients pid =
              ((ModernRoboticsUsbDcMotorController) dcMotorController)
                  .getDifferentialControlLoopCoefficients(motor);
          if (pid != null) {
            return pid.d;
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetDifferentialControlLoopCoefficientD",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0.0;
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    dcMotorController = hardwareMap.dcMotorController.get(getDeviceName());
    return dcMotorController;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("DcMotorController", hardwareMap.dcMotorController);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    dcMotorController = null;
  }
}
