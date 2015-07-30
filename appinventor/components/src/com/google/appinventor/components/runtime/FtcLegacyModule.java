// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.LegacyModule;
import com.qualcomm.robotcore.hardware.LegacyModule.PortReadyCallback;

import java.util.List;
import java.util.concurrent.locks.Lock;

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
public final class FtcLegacyModule extends FtcHardwareDevice implements PortReadyCallback{

  private volatile LegacyModule legacyModule;
  private final Object portsRegisteredForPortReadyCallbackLock = new Object();
  private final List<Integer> portsRegisteredForPortReadyCallback = Lists.newArrayList();

  /**
   * Creates a new FtcLegacyModule component.
   */
  public FtcLegacyModule(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleEvent(description = "This event is triggered when an I2C port is ready. This event is " +
      "only enabled if EnableNxtI2cReadMode or EnableNxtI2cWriteMode is used.")
  public void I2cPortIsReady(int port) {
    EventDispatcher.dispatchEvent(this, "I2cPortIsReady", port);
  }

  @SimpleFunction(description = "Enable a physical port in NXT I2C read mode and enable the " +
      "I2cPortIsReady event.")
  public void EnableNxtI2cReadMode(int port, int i2cAddress, int memAddress, int length) {
    if (legacyModule != null) {
      try {
        synchronized (portsRegisteredForPortReadyCallbackLock) {
          if (!portsRegisteredForPortReadyCallback.contains(port)) {
            legacyModule.registerForPortReadyCallback(this, port);
            portsRegisteredForPortReadyCallback.add(port);
          }
        }
        legacyModule.enableNxtI2cReadMode(port, i2cAddress, memAddress, length);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable a physical port in NXT I2C write mode and enable the " +
      "I2cPortIsReady event.")
  public void EnableNxtI2cWriteMode(int port, int i2cAddress, int memAddress, int length) {
    if (legacyModule != null) {
      try {
        synchronized (portsRegisteredForPortReadyCallbackLock) {
          if (!portsRegisteredForPortReadyCallback.contains(port)) {
            legacyModule.registerForPortReadyCallback(this, port);
            portsRegisteredForPortReadyCallback.add(port);
          }
        }
        legacyModule.enableNxtI2cWriteMode(port, i2cAddress, memAddress, length);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable a physical port in analog read mode.")
  public void EnableAnalogReadMode(int port) {
    if (legacyModule != null) {
      try {
        legacyModule.enableAnalogReadMode(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableAnalogReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable or disable 9V power on a port.")
  public void Enable9v(int port, boolean enable) {
    if (legacyModule != null) {
      try {
        legacyModule.enable9v(port, enable);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Enable9v",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Set the value of digital line 0 or 1 while in analog mode.")
  public void SetDigitalLine(int port, int line, boolean set) {
    if (legacyModule != null) {
      try {
        legacyModule.setDigitalLine(port, line, set);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetDigitalLine",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Read an analog value from a device and return a byte array; only works in analog read mode.")
  public Object ReadAnalog(int port) {
    if (legacyModule != null) {
      try {
        byte[] src = legacyModule.readAnalog(port);
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

  @SimpleFunction(description = "Get a copy of the contents of the I2C read cache; " +
      "return a byte array.")
  public Object GetI2cReadCache(int port) {
    if (legacyModule != null) {
      try {
        Lock lock = legacyModule.getI2cReadCacheLock(port);
        lock.lock();
        try {
          byte[] src = legacyModule.getI2cReadCache(port);
          if (src != null) {
            byte[] dest = new byte[src.length];
            System.arraycopy(src, 0, dest, 0, src.length);
            return dest;
          }
        } finally {
          lock.unlock();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetI2cReadCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }

  @SimpleFunction(description = "Get a copy of the contents of the I2C write cache; " +
      "return a byte array.")
  public Object GetI2cWriteCache(int port) {
    if (legacyModule != null) {
      try {
        Lock lock = legacyModule.getI2cWriteCacheLock(port);
        lock.lock();
        try {
          byte[] src = legacyModule.getI2cWriteCache(port);
          if (src != null) {
            byte[] dest = new byte[src.length];
            System.arraycopy(src, 0, dest, 0, src.length);
            return dest;
          }
        } finally {
          lock.unlock();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetI2cWriteCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }

  @SimpleFunction(description = "Set the contents of the I2C write cache.")
  public void SetI2cWriteCache(int port, Object byteArray) {
    if (legacyModule != null) {
      try {
        if (byteArray instanceof byte[]) {
          byte[] src = (byte[]) byteArray;
          Lock lock = legacyModule.getI2cWriteCacheLock(port);
          lock.lock();
          try {
            byte[] dest = legacyModule.getI2cWriteCache(port);
            if (dest != null) {
              System.arraycopy(src, 0, dest, 0, Math.min(src.length, dest.length));
            }
          } finally {
            lock.unlock();
          }
        } else {
          form.dispatchErrorOccurredEvent(this, "SetI2cWriteCache",
              ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetI2cWriteCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Set the port action flag; this flag tells the Legacy " +
      "Module to send the current data in its buffer to the I2C device.")
  public void SetNxtI2cPortActionFlag(int port) {
    if (legacyModule != null) {
      try {
        legacyModule.setNxtI2cPortActionFlag(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetNxtI2cPortActionFlag",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }
  
  @SimpleFunction(description = "Get the port action flag; this flag is set if the particular port is busy.")
  public boolean IsNxtI2cPortActionFlagSet(int port) {
    if (legacyModule != null) {
      try {
        return legacyModule.isNxtI2cPortActionFlagSet(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsNxtI2cPortActionFlagSet",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Read from the Legacy Module to the I2C read cache.")
  public void ReadI2cCacheFromModule(int port) {
    if (legacyModule != null) {
      try {
        legacyModule.readI2cCacheFromModule(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadI2cCacheFromModule",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write from the I2C write cache to the Legacy Module.")
  public void WriteI2cCacheToModule(int port) {
    if (legacyModule != null) {
      try {
        legacyModule.writeI2cCacheToModule(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteI2cCacheToModule",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write just the port action flag in the Legacy Module's cache " +
      "to the I2C device.")
  public void WriteI2cPortFlagOnlyToModule(int port) {
    if (legacyModule != null) {
      try {
        legacyModule.writeI2cPortFlagOnlyToModule(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteI2cPortFlagOnlyToModule",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Is the port in read mode?")
  public boolean IsI2cPortInReadMode(int port) {
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

  // PortReadyCallback implementation

  @Override
  public void portIsReady(int port) {
    I2cPortIsReady(port);
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      legacyModule = hardwareMap.legacyModule.get(getDeviceName());
      if (legacyModule == null) {
        deviceNotFound("LegacyModule", hardwareMap.legacyModule);
      }
    }
    return legacyModule;
  }

  @Override
  public void clearHardwareDevice() {
    synchronized (portsRegisteredForPortReadyCallbackLock) {
      for (Integer port : portsRegisteredForPortReadyCallback) {
        legacyModule.deregisterForPortReadyCallback(port);
      }
      portsRegisteredForPortReadyCallback.clear();
    }
    legacyModule = null;
  }

  // TODO(lizlooney): remove these deprecated functions.
  @SimpleFunction(description = "ReadI2c", userVisible = false)
  public Object ReadI2c(int port) {
    return new byte[0];
  }
  @SimpleFunction(description = "WriteI2c", userVisible = false)
  public void WriteI2c(int port, Object byteArray) {
  }
  @SimpleFunction(description = "FetchLegacyModuleWriteCache", userVisible = false)
  public Object FetchLegacyModuleWriteCache(int physicalPort) {
    return new byte[0];
  }
  @SimpleFunction(description = "IsPortReady", userVisible = false)
  public boolean IsPortReady(int physicalPort) {
    return false;
  }
  @SimpleFunction(description = "ReadLegacyModuleCache", userVisible = false)
  public Object ReadLegacyModuleCache(int physicalPort) {
    return new byte[0];
  }
  @SimpleFunction(description = "WriteLegacyModuleCache", userVisible = false)
  public void WriteLegacyModuleCache(int physicalPort, Object byteArray) {
  }
  @SimpleFunction(description = "WriteI2cPortFlagOnlyFromModule", userVisible = false)
  public void WriteI2cPortFlagOnlyFromModule(int port) {
  }
}
