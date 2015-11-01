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

import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A component for a gyro sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_GYRO_SENSOR_COMPONENT_VERSION,
    description = "A component for a gyro sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcGyroSensor extends FtcHardwareDevice {

  private volatile GyroSensor gyroSensor;

  /**
   * Creates a new FtcGyroSensor component.
   */
  public FtcGyroSensor(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Calibrate the gyro. " +
      "Not all gyro sensors support this feature. " +
      "For the Modern Robotics device this will reset the Z axis heading.")
  public void Calibrate() {
    if (gyroSensor != null) {
      try {
        gyroSensor.calibrate();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Calibrate",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Is the gyro performing a calibration operation? " +
      "Not all gyro sensors support this feature.")
  public boolean IsCalibrating() {
    if (gyroSensor != null) {
      try {
        return gyroSensor.isCalibrating();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsCalibrating",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  /**
   * Heading property getter.
   */
  @SimpleProperty(description = "The integrated Z axis as a cartesian heading, as a numeric " +
      "value between 0 and 360. " +
      "Not all gyro sensors support this feature.",
      category = PropertyCategory.BEHAVIOR)
  public int Heading() {
    if (gyroSensor != null) {
      try {
        return gyroSensor.getHeading();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Heading",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Rotation property getter.
   */
  @SimpleProperty(description = "The rotation of this sensor. " +
      "Not all gyro sensors support this feature.",
      category = PropertyCategory.BEHAVIOR)
  public double Rotation() {
    if (gyroSensor != null) {
      try {
        return gyroSensor.getRotation();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Rotation",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * RawX property getter.
   */
  @SimpleProperty(description = "The gyro sensor's raw X value. " +
      "Not all gyro sensors support this feature.",
      category = PropertyCategory.BEHAVIOR)
  public int RawX() {
    if (gyroSensor != null) {
      try {
        return gyroSensor.rawX();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "RawX",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * RawY property getter.
   */
  @SimpleProperty(description = "The gyro sensor's raw Y value. " +
      "Not all gyro sensors support this feature.",
      category = PropertyCategory.BEHAVIOR)
  public int RawY() {
    if (gyroSensor != null) {
      try {
        return gyroSensor.rawY();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "RawY",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * RawZ property getter.
   */
  @SimpleProperty(description = "The gyro sensor's raw Z value. " +
      "Not all gyro sensors support this feature.",
      category = PropertyCategory.BEHAVIOR)
  public int RawZ() {
    if (gyroSensor != null) {
      try {
        return gyroSensor.rawZ();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "RawZ",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Set the integrated Z axis to zero. " +
      "Not all gyro sensors support this feature.")
  public void ResetZAxisIntegrator() {
    if (gyroSensor != null) {
      try {
        gyroSensor.resetZAxisIntegrator();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ResetZAxisIntegrator",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The Status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    if (gyroSensor != null) {
      try {
        String status = gyroSensor.status();
        if (status != null) {
          return status;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Status",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      try {
        gyroSensor = hardwareMap.gyroSensor.get(getDeviceName());
        if (gyroSensor == null) {
          deviceNotFound("GyroSensor", hardwareMap.gyroSensor);
        }
      } catch (Throwable e) {
        deviceNotFound("GyroSensor", hardwareMap.gyroSensor);
      }
    }
    return gyroSensor;
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    gyroSensor = null;
  }
}
