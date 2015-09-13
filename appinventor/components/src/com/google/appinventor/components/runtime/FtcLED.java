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

import com.qualcomm.robotcore.hardware.LED;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A component for an LED of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_LED_COMPONENT_VERSION,
    description = "A component for an LED of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcLED extends FtcHardwareDevice {

  private volatile LED led;

  /**
   * Creates a new FtcLED component.
   */
  public FtcLED(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Turn on or turn off the LED light.")
  public void Enable(boolean set) {
    if (led != null) {
      try {
        led.enable(set);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Enable",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      led = hardwareMap.led.get(getDeviceName());
      if (led == null) {
        deviceNotFound("LED", hardwareMap.led);
      }
    }
    return led;
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    led = null;
  }
}
