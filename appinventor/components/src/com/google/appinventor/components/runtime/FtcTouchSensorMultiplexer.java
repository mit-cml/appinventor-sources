// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

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
@UsesLibraries(libraries = "FtcRobotCore.jar")
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


  // FtcRobotController.HardwareDevice implementation

  @Override
  public Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      touchSensorMultiplexer = hardwareMap.touchSensorMultiplexer.get(getDeviceName());
      if (touchSensorMultiplexer == null) {
        deviceNotFound("TouchSensorMultiplexer", hardwareMap.touchSensorMultiplexer);
      }
    }
    return touchSensorMultiplexer;
  }

  @Override
  public void clearHardwareDevice() {
    touchSensorMultiplexer = null;
  }
}
