// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A base class for components for hardware devices of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@SimpleObject
public abstract class FtcHardwareDevice extends AndroidNonvisibleComponent
    implements Component, Deleteable, FtcRobotController.HardwareDevice {

  private volatile String deviceName = "";
  private volatile HardwareMap hardwareMap;

  protected FtcHardwareDevice(ComponentContainer container) {
    super(container.$form());
    FtcRobotController.addHardwareDevice(form, this);
  }

  // Properties

  /**
   * DeviceName property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The name for the hardware device",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String DeviceName() {
    return deviceName;
  }

  /**
   * DeviceName property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void DeviceName(String deviceName) {
    if (hardwareMap != null) {
      clearHardwareDevice();
    }
    this.deviceName = deviceName;
    if (hardwareMap != null) {
      initHardwareDevice();
    }
  }

  final String getDeviceName() {
    return deviceName;
  }

  final HardwareMap getHardwareMap() {
    return hardwareMap;
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    FtcRobotController.removeHardwareDevice(form, this);
    clearHardwareDevice();
    hardwareMap = null;
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void setHardwareMap(HardwareMap hardwareMap) {
    if (this.hardwareMap != null) {
      clearHardwareDevice();
    }
    this.hardwareMap = hardwareMap;
    if (this.hardwareMap != null) {
      initHardwareDevice();
    }
  }

  // abstract methods

  abstract void initHardwareDevice();

  abstract void clearHardwareDevice();
}
