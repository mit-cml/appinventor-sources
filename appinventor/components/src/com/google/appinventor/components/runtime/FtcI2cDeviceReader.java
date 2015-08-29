// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
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

  private volatile int i2cAddress;
  private volatile int memAddress;
  private volatile int length;
  private volatile I2cDeviceReader i2cDeviceReader;

  /**
   * Creates a new FtcI2cDeviceReader component.
   */
  public FtcI2cDeviceReader(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * I2cAddress property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The I2C address to read from.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public int I2cAddress() {
    return i2cAddress;
  }

  /**
   * I2cAddress property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void I2cAddress(int i2cAddress) {
    this.i2cAddress = i2cAddress;
  }

  /**
   * MemAddress property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The memory address to read from.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public int MemAddress() {
    return memAddress;
  }

  /**
   * MemAddress property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void MemAddress(int memAddress) {
    this.memAddress = memAddress;
  }

  /**
   * Length property getter.
   * Not visible in blocks.
   */
  @SimpleProperty(description = "The length (in bytes) to read.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public int Length() {
    return length;
  }

  /**
   * Length property setter.
   * Can only be set in designer; not visible in blocks.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void Length(int length) {
    this.length = length;
  }

  @SimpleFunction(description = "Get a copy of the most recent data read in " +
      "from the I2C device. (byte array)")
  public Object GetReadBuffer() {
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
  protected Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    I2cDevice i2cDevice = null;
    if (hardwareMap != null) {
      i2cDevice = hardwareMap.i2cDevice.get(getDeviceName());
      if (i2cDevice != null) {
        i2cDeviceReader = new I2cDeviceReader(i2cDevice, i2cAddress, memAddress, length);
      } else {
        deviceNotFound("I2cDeviceReader", hardwareMap.i2cDevice);
      }
    }
    return i2cDevice;
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    i2cDeviceReader = null;
  }
}
