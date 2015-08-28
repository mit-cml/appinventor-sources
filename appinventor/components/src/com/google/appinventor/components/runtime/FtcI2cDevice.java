// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cController.I2cPortReadyCallback;
import com.qualcomm.robotcore.hardware.I2cDevice;

import java.util.concurrent.locks.Lock;

// TODO(lizlooney): What about I2cDeviceReader?

/**
 * A component for an I2C device of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_I2C_DEVICE_COMPONENT_VERSION,
    description = "A component for an I2C device of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcI2cDevice extends FtcHardwareDevice implements I2cPortReadyCallback {

  private volatile I2cDevice i2cDevice;
  private final Object registeredForPortReadyCallbackLock = new Object();
  private volatile boolean registeredForPortReadyCallback;

  /**
   * Creates a new FtcI2cDevice component.
   */
  public FtcI2cDevice(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleEvent(description = "This event is triggered when the I2C port is ready. This event is " +
      "only enabled if EnableI2cReadMode or EnableI2cWriteMode is used.")
  public void I2cPortIsReady() {
    EventDispatcher.dispatchEvent(this, "I2cPortIsReady");
  }

  @SimpleFunction(description = "Enable read mode for the I2C device and enable the " +
      "I2cPortIsReady event.")
  public void EnableI2cReadMode(int i2cAddress, int memAddress, int length) {
    if (i2cDevice != null) {
      try {
        synchronized (registeredForPortReadyCallbackLock) {
          if (!registeredForPortReadyCallback) {
            i2cDevice.registerForI2cPortReadyCallback(this);
            registeredForPortReadyCallback = true;
          }
        }
        i2cDevice.enableI2cReadMode(i2cAddress, memAddress, length);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableI2cReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable write mode for the I2C device and enable the " +
      "I2cPortIsReady event.")
  public void EnableI2cWriteMode(int i2cAddress, int memAddress, int length) {
    if (i2cDevice != null) {
      try {
        synchronized (registeredForPortReadyCallbackLock) {
          if (!registeredForPortReadyCallback) {
            i2cDevice.registerForI2cPortReadyCallback(this);
            registeredForPortReadyCallback = true;
          }
        }
        i2cDevice.enableI2cWriteMode(i2cAddress, memAddress, length);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableI2cWriteMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /*
  @SimpleFunction(description = "Get a copy of the most recent data read in " +
      "from the device. (byte array)")
  public Object GetCopyOfReadBuffer() {
    if (i2cDevice != null) {
      try {
        byte[] copy = i2cDevice.getCopyOfReadBuffer();
        if (copy != null) {
          return copy;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetCopyOfReadBuffer",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }

  @SimpleFunction(description = "Get a copy of the data that is set to be " +
      "written out to the device. (byte array)")
  public Object GetCopyOfWriteBuffer() {
    if (i2cDevice != null) {
      try {
        byte[] copy = i2cDevice.getCopyOfWriteBuffer();
        if (copy != null) {
          return copy;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetCopyOfWriteBuffer",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }

  @SimpleFunction(description = "Copy a byte array into the buffer that is set " +
      "to be written out to the device")
  public void CopyBufferIntoWriteBuffer(Object byteArray) {
    if (i2cDevice != null) {
      try {
        if (byteArray instanceof byte[]) {
          byte[] array = (byte[]) byteArray;
          i2cDevice.copyBufferIntoWriteBuffer(array);
        } else {
          form.dispatchErrorOccurredEvent(this, "CopyBufferIntoWriteBuffer",
              ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "CopyBufferIntoWriteBuffer",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }
  */

  @SimpleFunction(description = "Set the action flag for this I2C port.")
  public void SetI2cPortActionFlag() {
    if (i2cDevice != null) {
      try {
        i2cDevice.setI2cPortActionFlag();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetI2cPortActionFlag",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }
  
  @SimpleFunction(description = "Check whether or not the action flag is set for this I2C port.")
  public boolean IsI2cPortActionFlagSet() {
    if (i2cDevice != null) {
      try {
        return i2cDevice.isI2cPortActionFlagSet();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortActionFlagSet",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Trigger a read of the I2C cache.")
  public void ReadI2cCacheFromModule() {
    if (i2cDevice != null) {
      try {
        i2cDevice.readI2cCacheFromModule();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadI2cCacheFromModule",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Trigger a write of the I2C cache.")
  public void WriteI2cCacheToModule() {
    if (i2cDevice != null) {
      try {
        i2cDevice.writeI2cCacheToModule();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteI2cCacheToModule",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write only the action flag.")
  public void WriteI2cPortFlagOnlyToModule() {
    if (i2cDevice != null) {
      try {
        i2cDevice.writeI2cPortFlagOnlyToModule();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteI2cPortFlagOnlyToModule",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Query whether or not the port is in Read mode.")
  public boolean IsI2cPortInReadMode() {
    if (i2cDevice != null) {
      try {
        return i2cDevice.isI2cPortInReadMode();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortInReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Query whether or not this port is in write mode.")
  public boolean IsI2cPortInWriteMode() {
    if (i2cDevice != null) {
      try {
        return i2cDevice.isI2cPortInWriteMode();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortInWriteMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Query whether or not this I2c port is ready.")
  public boolean IsI2cPortReady() {
    if (i2cDevice != null) {
      try {
        return i2cDevice.isI2cPortReady();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortReady",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  // I2CPortReadyCallback implementation

  @Override
  public void portIsReady(int port) {
    I2cPortIsReady();
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      i2cDevice = hardwareMap.i2cDevice.get(getDeviceName());
      if (i2cDevice == null) {
        deviceNotFound("I2cDevice", hardwareMap.i2cDevice);
      }
    }
    return i2cDevice;
  }

  @Override
  public void clearHardwareDevice() {
    synchronized (registeredForPortReadyCallbackLock) {
      if (registeredForPortReadyCallback) {
        i2cDevice.deregisterForPortReadyCallback();
        registeredForPortReadyCallback = false;
      }
    }
    i2cDevice = null;
  }

  // TODO(lizlooney): remove these
  // The following were deprecated on 2015/08/28.
  @SimpleFunction(description = "GetI2cReadCache", userVisible = false)
  public Object GetI2cReadCache() {
    return new byte[0];
  }
  @SimpleFunction(description = "GetI2cWriteCache", userVisible = false)
  public Object GetI2cWriteCache() {
    return new byte[0];
  }
  @SimpleFunction(description = "SetI2cWriteCache", userVisible = false)
  public void SetI2cWriteCache(Object byteArray) {
  }
}
