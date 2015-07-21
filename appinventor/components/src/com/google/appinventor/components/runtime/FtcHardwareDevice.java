// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.HardwareMap.DeviceMapping;

import android.util.Log;

import java.util.Map;

/**
 * A base class for components for hardware devices of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@SimpleObject
public abstract class FtcHardwareDevice<DEVICE_TYPE> extends AndroidNonvisibleComponent
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
    this.deviceName = deviceName;
  }

  protected final String getDeviceName() {
    return deviceName;
  }

  protected final HardwareMap getHardwareMap() {
    return hardwareMap;
  }

  protected final void deviceNotFound(String type, DeviceMapping<DEVICE_TYPE> deviceMapping) {
    StringBuilder names = new StringBuilder();
    String delimiter = "";
    for (Map.Entry<String, DEVICE_TYPE> entry : deviceMapping.entrySet()) {
      names.append(delimiter).append(entry.getKey());
      delimiter = ", ";
    }
    form.dispatchErrorOccurredEvent(this, "", ErrorMessages.ERROR_FTC_INVALID_DEVICE_NAME,
        type, getDeviceName(), names.toString());
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
  }

  public abstract void initHardwareDevice();

  public abstract void clearHardwareDevice();
}
