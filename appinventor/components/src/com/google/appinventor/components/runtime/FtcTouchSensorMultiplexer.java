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

import com.qualcomm.hardware.hitechnic.HiTechnicNxtTouchSensorMultiplexer;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensorMultiplexer;

/**
 * A component for a touch sensor multiplexer of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_TOUCH_SENSOR_MULTIPLEXER_COMPONENT_VERSION,
    description = "A component for a touch sensor multiplexer of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcHardware.jar,FtcRobotCore.jar")
public final class FtcTouchSensorMultiplexer extends FtcHardwareDevice {

  private volatile TouchSensorMultiplexer touchSensorMultiplexer;

  /**
   * Creates a new FtcTouchSensorMultiplexer component.
   */
  public FtcTouchSensorMultiplexer(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Is the touch sensor pressed?")
  public boolean IsTouchSensorPressed(int channel) {
    checkHardwareDevice();
    if (touchSensorMultiplexer != null) {
      try {
        return touchSensorMultiplexer.isTouchSensorPressed(channel);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsTouchSensorPressed",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Get switches")
  public int GetSwitches() {
    checkHardwareDevice();
    if (touchSensorMultiplexer != null) {
      try {
        return touchSensorMultiplexer.getSwitches();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetSwitches",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * Status property getter.
   */
  @SimpleProperty(description = "The status, if supported by the touch sensor multiplexer.",
      category = PropertyCategory.BEHAVIOR)
  public String Status() {
    checkHardwareDevice();
    if (touchSensorMultiplexer != null) {
      try {
        String status = null;
        if (touchSensorMultiplexer instanceof HiTechnicNxtTouchSensorMultiplexer) {
          status = ((HiTechnicNxtTouchSensorMultiplexer) touchSensorMultiplexer).status();
        }
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
    touchSensorMultiplexer = hardwareMap.touchSensorMultiplexer.get(getDeviceName());
    return touchSensorMultiplexer;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("TouchSensorMultiplexer", hardwareMap.touchSensorMultiplexer);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    touchSensorMultiplexer = null;
  }
}
