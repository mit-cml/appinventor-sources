// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

/**
 * A component for an ultrasonic sensor of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_ULTRASONIC_SENSOR_COMPONENT_VERSION,
    description = "A component for an ultrasonic sensor of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcUltrasonicSensor extends FtcHardwareDevice {

  private volatile UltrasonicSensor ultrasonicSensor;

  /**
   * Creates a new FtcUltrasonicSensor component.
   */
  public FtcUltrasonicSensor(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * UltrasonicLevel property getter.
   */
  @SimpleProperty(description = "The ultrasonic level.",
      category = PropertyCategory.BEHAVIOR)
  public double UltrasonicLevel() {
    checkHardwareDevice();
    if (ultrasonicSensor != null) {
      try {
        return ultrasonicSensor.getUltrasonicLevel();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "UltrasonicLevel",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The status.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    checkHardwareDevice();
    if (ultrasonicSensor != null) {
      try {
        String status = ultrasonicSensor.status();
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
  protected Object initHardwareDeviceImpl() {
    ultrasonicSensor = hardwareMap.ultrasonicSensor.get(getDeviceName());
    return ultrasonicSensor;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("UltrasonicSensor", hardwareMap.ultrasonicSensor);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    ultrasonicSensor = null;
  }
}
