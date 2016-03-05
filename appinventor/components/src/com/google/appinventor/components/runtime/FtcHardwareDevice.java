// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.errors.FtcInitHardwareDeviceRuntimeException;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.HardwareMap.DeviceMapping;

import java.util.Map;

/**
 * A base class for components for hardware devices of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@SimpleObject
public abstract class FtcHardwareDevice extends AndroidNonvisibleComponent
    implements Component, OnDestroyListener, Deleteable, FtcRobotController.HardwareDevice {

  private volatile String deviceName = "";
  protected volatile HardwareMap hardwareMap;
  private volatile HardwareDevice hardwareDevice;
  private volatile Throwable initHardwareDeviceException;

  protected FtcHardwareDevice(ComponentContainer container) {
    super(container.$form());
    FtcRobotController.addHardwareDevice(this);
    form.registerForOnDestroy(this);
  }

  /**
   * DeviceName property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The name for the hardware device.",
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

  /**
   * Device property getter.
   */
  @SimpleProperty(description = "The manufacturer and name of the device.",
      category = PropertyCategory.BEHAVIOR)
  public String Device() {
    checkHardwareDevice();
    if (hardwareDevice != null) {
      try {
        String device = hardwareDevice.getDeviceName();
        if (device != null) {
          return device;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Device",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  /**
   * ConnectionInfo property getter.
   */
  @SimpleProperty(description = "The connection information.",
      category = PropertyCategory.BEHAVIOR)
  public String ConnectionInfo() {
    checkHardwareDevice();
    if (hardwareDevice != null) {
      try {
        String connectionInfo = hardwareDevice.getConnectionInfo();
        if (connectionInfo != null) {
          return connectionInfo;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ConnectionInfo",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  /**
   * Version property getter.
   */
  @SimpleProperty(description = "The version.",
      category = PropertyCategory.BEHAVIOR)
  public int Version() {
    checkHardwareDevice();
    if (hardwareDevice != null) {
      try {
        return hardwareDevice.getVersion();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Version",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Return text representing the state of the hardware device.")
  public String ToString() {
    checkHardwareDevice();
    if (hardwareDevice != null) {
      try {
        String s = hardwareDevice.toString();
        if (s != null) {
          return s;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ToString",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    FtcRobotController.removeHardwareDevice(this);
    clearHardwareDevice();
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    FtcRobotController.removeHardwareDevice(this);
    clearHardwareDevice();
  }

  // FtcHardwareDevice implementation

  @Override
  public final void initHardwareDevice(OpMode opMode) {
    try {
      initHardwareDeviceException = null;
      hardwareMap = opMode.hardwareMap;
      Object hardware = initHardwareDeviceImpl();
      hardwareDevice = (hardware instanceof HardwareDevice) ?
          ((HardwareDevice) hardware) : null;

      // hardwareDevice is allowed to be null, but hardware should not be null.
      if (hardware == null) {
        throw new NullPointerException();
      }
    } catch (Throwable e) {
      initHardwareDeviceException = e;
    }
  }

  @Override
  public final void clearHardwareDevice() {
    clearHardwareDeviceImpl();
    hardwareMap = null;
    hardwareDevice = null;
  }

  // protected methods

  protected abstract Object initHardwareDeviceImpl();

  protected abstract void dispatchDeviceNotFoundError();

  protected abstract void clearHardwareDeviceImpl();

  protected final String getDeviceName() {
    return deviceName;
  }

  protected final void checkHardwareDevice() {
    if (initHardwareDeviceException != null) {
      throw new FtcInitHardwareDeviceRuntimeException(initHardwareDeviceException);
    }
  }

  protected final void dispatchDeviceNotFoundError(String type,
      DeviceMapping<? extends Object> deviceMapping) {
    StringBuilder names = new StringBuilder();
    String delimiter = "";
    for (Map.Entry<String, ? extends Object> entry : deviceMapping.entrySet()) {
      names.append(delimiter).append(entry.getKey());
      delimiter = ", ";
    }
    form.dispatchErrorOccurredEvent(this, "", ErrorMessages.ERROR_FTC_INVALID_DEVICE_NAME,
        type, deviceName, names.toString());
  }
}
