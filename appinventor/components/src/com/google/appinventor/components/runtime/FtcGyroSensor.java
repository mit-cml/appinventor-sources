// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
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
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcGyroSensor extends FtcHardwareDevice {

  private volatile GyroSensor gyroSensor;

  /**
   * Creates a new FtcGyroSensor component.
   */
  public FtcGyroSensor(ComponentContainer container) {
    super(container.$form());
  }

  // Properties

  /**
   * Rotation property getter.
   */
  @SimpleProperty(description = "The Rotation.",
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

  // TODO(lizlooney): some sensors hava a status method, but some don't. Should I have a Status
  // property in app inventor?
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
  protected void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      gyroSensor = hardwareMap.gyroSensor.get(getDeviceName());
    }
  }

  @Override
  protected void clearHardwareDevice() {
    gyroSensor = null;
  }
}
