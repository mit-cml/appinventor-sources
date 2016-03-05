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
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.LegacyModule;
import com.qualcomm.robotcore.hardware.I2cController.I2cPortReadyCallback;

import java.util.List;

/**
 * A component for a legacy module of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_LEGACY_MODULE_COMPONENT_VERSION,
    description = "A component for a legacy module of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcLegacyModule extends FtcHardwareDevice implements I2cPortReadyCallback {

  private volatile LegacyModule legacyModule;
  private final Object portsRegisteredForPortReadyCallbackLock = new Object();
  private final List<Integer> portsRegisteredForPortReadyCallback = Lists.newArrayList();

  /**
   * Creates a new FtcLegacyModule component.
   */
  public FtcLegacyModule(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleEvent(description = "This event is triggered when an I2C port is ready, " +
      "after the latest data has been read from the I2C Controller.\n" +
      "This event is only enabled if EnableI2cReadMode or EnableI2cWriteMode is used.")
  public void I2cPortIsReady(int port) {
    EventDispatcher.dispatchEvent(this, "I2cPortIsReady", port);
  }

  // I2cController

  @SimpleFunction(description = "Enable read mode for a particular I2C device and enable the " +
      "I2cPortIsReady event for the given port.")
  public void EnableI2cReadMode(int physicalPort, int i2cAddress, int memAddress, int length) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        synchronized (portsRegisteredForPortReadyCallbackLock) {
          if (!portsRegisteredForPortReadyCallback.contains(physicalPort)) {
            legacyModule.registerForI2cPortReadyCallback(this, physicalPort);
            portsRegisteredForPortReadyCallback.add(physicalPort);
          }
        }
        legacyModule.enableI2cReadMode(physicalPort, i2cAddress, memAddress, length);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableI2cReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable write mode for a particular I2C device and enable the " +
      "I2cPortIsReady event for the given port.")
  public void EnableI2cWriteMode(int physicalPort, int i2cAddress, int memAddress, int length) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        synchronized (portsRegisteredForPortReadyCallbackLock) {
          if (!portsRegisteredForPortReadyCallback.contains(physicalPort)) {
            legacyModule.registerForI2cPortReadyCallback(this, physicalPort);
            portsRegisteredForPortReadyCallback.add(physicalPort);
          }
        }
        legacyModule.enableI2cWriteMode(physicalPort, i2cAddress, memAddress, length);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableI2cWriteMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Get a copy of the most recent data read in " +
      "from the device. (byte array)")
  public Object GetCopyOfReadBuffer(int physicalPort) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        byte[] copy = legacyModule.getCopyOfReadBuffer(physicalPort);
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
  public Object GetCopyOfWriteBuffer(int physicalPort) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        byte[] copy = legacyModule.getCopyOfWriteBuffer(physicalPort);
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
      "to be written out to the device.")
  public void CopyBufferIntoWriteBuffer(int physicalPort, Object byteArray) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        if (byteArray instanceof byte[]) {
          byte[] array = (byte[]) byteArray;
          legacyModule.copyBufferIntoWriteBuffer(physicalPort, array);
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

  @SimpleFunction(description = "Set the port action flag; this flag tells the " +
      "controller to send the current data in its buffer to the I2C device.")
  public void SetI2cPortActionFlag(int port) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        legacyModule.setI2cPortActionFlag(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetI2cPortActionFlag",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }
  
  @SimpleFunction(description = "Get the port action flag; this flag is set if " +
      "the particular port is busy.")
  public boolean IsI2cPortActionFlagSet(int port) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        return legacyModule.isI2cPortActionFlagSet(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortActionFlagSet",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Read the local cache in from the I2C Controller.\n" +
      "NOTE: unless this method is called the internal cache isn't updated.")
  public void ReadI2cCacheFromController(int port) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        legacyModule.readI2cCacheFromController(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadI2cCacheFromController",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write the local cache to the I2C Controller.\n" +
      "NOTE: unless this method is called the internal cache isn't updated.")
  public void WriteI2cCacheToController(int port) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        legacyModule.writeI2cCacheToController(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteI2cCacheToController",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write just the port action flag in the local " +
      "cache to the I2C controller.")
  public void WriteI2cPortFlagOnlyToController(int port) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        legacyModule.writeI2cPortFlagOnlyToController(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteI2cPortFlagOnlyToController",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Is the port in read mode?")
  public boolean IsI2cPortInReadMode(int port) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        return legacyModule.isI2cPortInReadMode(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortInReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Is the port in write mode?")
  public boolean IsI2cPortInWriteMode(int port) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        return legacyModule.isI2cPortInWriteMode(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortInWriteMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Determine if a physical port is ready.")
  public boolean IsI2cPortReady(int port) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        return legacyModule.isI2cPortReady(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortReady",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Enable a physical port in analog read mode.")
  public void EnableAnalogReadMode(int physicalPort) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        legacyModule.enableAnalogReadMode(physicalPort);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableAnalogReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable or disable 9V power on a port.")
  public void Enable9v(int physicalPort, boolean enable) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        legacyModule.enable9v(physicalPort, enable);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Enable9v",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Set the value of digital line 0 or 1 while in analog mode.")
  public void SetDigitalLine(int physicalPort, int line, boolean set) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        legacyModule.setDigitalLine(physicalPort, line, set);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetDigitalLine",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Read an analog value from a device and return a byte array; only works in analog read mode.")
  public Object ReadAnalog(int physicalPort) {
    checkHardwareDevice();
    if (legacyModule != null) {
      try {
        byte[] src = legacyModule.readAnalog(physicalPort);
        if (src != null) {
          byte[] dest = new byte[src.length];
          System.arraycopy(src, 0, dest, 0, src.length);
          return dest;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadAnalog",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }

  // I2cPortReadyCallback implementation

  @Override
  public void portIsReady(int port) {
    I2cPortIsReady(port);
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    legacyModule = hardwareMap.legacyModule.get(getDeviceName());
    return legacyModule;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("LegacyModule", hardwareMap.legacyModule);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    synchronized (portsRegisteredForPortReadyCallbackLock) {
      for (Integer port : portsRegisteredForPortReadyCallback) {
        legacyModule.deregisterForPortReadyCallback(port);
      }
      portsRegisteredForPortReadyCallback.clear();
    }
    legacyModule = null;
  }
}
