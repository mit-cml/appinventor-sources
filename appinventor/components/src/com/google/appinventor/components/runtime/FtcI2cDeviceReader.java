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
import com.qualcomm.robotcore.hardware.I2cDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceReader;

/**
 * A component for an I2C device reader of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_I2C_DEVICE_READER_COMPONENT_VERSION,
    description = "A component for an I2C device reader of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcI2cDeviceReader extends FtcHardwareDevice {

  private volatile I2cDevice i2cDevice;
  private volatile I2cDeviceReader i2cDeviceReader;

  /**
   * Creates a new FtcI2cDeviceReader component.
   */
  public FtcI2cDeviceReader(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Initialize this I2C device reader")
  public void Initialize(int i2cAddress, int memAddress, int length) {
    checkHardwareDevice();
    try {
      if (i2cDeviceReader != null) {
        i2cDevice.deregisterForPortReadyCallback();
        i2cDeviceReader = null;
      }
      if (i2cDevice != null) {
        i2cDeviceReader = new I2cDeviceReader(i2cDevice, i2cAddress, memAddress, length);
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Initialize",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Get a copy of the most recent data read in " +
      "from the I2C device. (byte array)")
  public Object GetReadBuffer() {
    checkHardwareDevice();
    if (i2cDeviceReader != null) {
      try {
        byte[] copy = i2cDeviceReader.getReadBuffer();
        if (copy != null) {
          return copy;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetReadBuffer",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    // We don't instantiate the I2cDeviceReader here because it has the side effect of
    // registering an I2cPortReadyCallback. Instead, we just get the I2cDevice here and in
    // Initialize method, we instantiate the I2cDeviceReader.
    i2cDevice = hardwareMap.i2cDevice.get(getDeviceName());
    return i2cDevice;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("I2cDeviceReader", hardwareMap.i2cDevice);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    if (i2cDeviceReader != null) {
      i2cDevice.deregisterForPortReadyCallback();
      i2cDeviceReader = null;
    }
    i2cDevice = null;
  }
}
