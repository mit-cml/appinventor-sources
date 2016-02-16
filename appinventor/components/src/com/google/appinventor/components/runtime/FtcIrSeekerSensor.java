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

import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbDeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IrSeekerSensor;
import com.qualcomm.robotcore.hardware.IrSeekerSensor.IrSeekerIndividualSensor;
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
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcIrSeekerSensor extends FtcHardwareDevice {

  private volatile IrSeekerSensor irSeekerSensor;

  /**
   * Creates a new FtcIrSeekerSensor component.
   */
  public FtcIrSeekerSensor(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * Mode_600HZ property getter.
   */
  @SimpleProperty(description = "The constant for Mode_600HZ.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode_600HZ() {
    return Mode.MODE_600HZ.toString();
  }

  /**
   * Mode_1200HZ property getter.
   */
  @SimpleProperty(description = "The constant for Mode_1200HZ.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode_1200HZ() {
    return Mode.MODE_1200HZ.toString();
  }

  /**
   * Mode property setter.
   */
  @SimpleProperty
  public void Mode(String mode) {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        for (Mode modeValue : Mode.values()) {
          if (modeValue.toString().equalsIgnoreCase(mode)) {
            irSeekerSensor.setMode(modeValue);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "Mode",
            ErrorMessages.ERROR_FTC_INVALID_IR_SEEKER_SENSOR_MODE, mode);
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
  @SimpleProperty(description = "The mode of the IR seeker sensor.\n" +
      "Valid values are Mode_600HZ or Mode_1200HZ.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode() {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        Mode mode = irSeekerSensor.getMode();
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

  /**
   * SignalDetectedThreshold property setter.
   */
  @SimpleProperty
  public void SignalDetectedThreshold(double threshold) {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        irSeekerSensor.setSignalDetectedThreshold(threshold);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SignalDetectedThreshold",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * SignalDetectedThreshold property getter.
   */
  @SimpleProperty(description = "The signal detection threshold of the IR seeker sensor.",
      category = PropertyCategory.BEHAVIOR)
  public double SignalDetectedThreshold() {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        return irSeekerSensor.getSignalDetectedThreshold();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SignalDetectedThreshold",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * SignalDetected property getter.
   */
  @SimpleProperty(description = "Whether a signal is detected by the sensor.",
      category = PropertyCategory.BEHAVIOR)
  public boolean SignalDetected() {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        return irSeekerSensor.signalDetected();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SignalDetected",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  /**
   * Angle property getter.
   */
  @SimpleProperty(description = "The angle.",
      category = PropertyCategory.BEHAVIOR)
  public double Angle() {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        if (irSeekerSensor.signalDetected()) {
          return irSeekerSensor.getAngle();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Angle",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Strength property getter.
   */
  @SimpleProperty(description = "The strength.",
      category = PropertyCategory.BEHAVIOR)
  public double Strength() {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        if (irSeekerSensor.signalDetected()) {
          return irSeekerSensor.getStrength();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Strength",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * SensorCount property getter.
   */
  @SimpleProperty(description = "The number of individual IR sensors attached to this seeker.",
      category = PropertyCategory.BEHAVIOR)
  public int IndividualSensorCount() {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        IrSeekerIndividualSensor[] sensors = irSeekerSensor.getIndividualSensors();
        if (sensors != null) {
          return sensors.length;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IndividualSensorCount",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "The angle of the individual IR sensor with the given " +
      "zero-based position.")
  public double IndividualSensorAngle(int position) {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        IrSeekerIndividualSensor[] sensors = irSeekerSensor.getIndividualSensors();
        if (sensors != null) {
          if (position >= 0 && position < sensors.length) {
            return sensors[position].getSensorAngle();
          } else {
            form.dispatchErrorOccurredEvent(this, "SensorAngle",
                ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IndividualSensorAngle",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "The strength of the individual IR sensor with the given " +
      "zero-based position.")
  public double IndividualSensorStrength(int position) {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        IrSeekerIndividualSensor[] sensors = irSeekerSensor.getIndividualSensors();
        if (sensors != null) {
          if (position >= 0 && position < sensors.length) {
            return sensors[position].getSensorStrength();
          } else {
            form.dispatchErrorOccurredEvent(this, "IndividualSensorStrength",
                ErrorMessages.ERROR_FTC_INVALID_POSITION, "position", position);
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IndividualSensorStrength",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * MAX_NEW_I2C_ADDRESS property getter.
   */
  @SimpleProperty(description = "The constant for MAX_NEW_I2C_ADDRESS.",
      category = PropertyCategory.BEHAVIOR)
  public int MAX_NEW_I2C_ADDRESS() {
    return ModernRoboticsUsbDeviceInterfaceModule.MAX_NEW_I2C_ADDRESS;
  }

  /**
   * MIN_NEW_I2C_ADDRESS property getter.
   */
  @SimpleProperty(description = "The constant for MIN_NEW_I2C_ADDRESS.",
      category = PropertyCategory.BEHAVIOR)
  public int MIN_NEW_I2C_ADDRESS() {
    return ModernRoboticsUsbDeviceInterfaceModule.MIN_NEW_I2C_ADDRESS;
  }

  /**
   * I2cAddress property setter.
   */
  @SimpleProperty
  public void I2cAddress(int newAddress) {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        irSeekerSensor.setI2cAddress(newAddress);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "I2cAddress",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * I2cAddress property getter.
   */
  @SimpleProperty(description = "The I2C address of the IR seeker sensor.",
      category = PropertyCategory.BEHAVIOR)
  public int I2cAddress() {
    checkHardwareDevice();
    if (irSeekerSensor != null) {
      try {
        return irSeekerSensor.getI2cAddress();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "I2cAddress",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    irSeekerSensor = hardwareMap.irSeekerSensor.get(getDeviceName());
    return irSeekerSensor;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("IrSeekerSensor", hardwareMap.irSeekerSensor);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    irSeekerSensor = null;
  }
}
