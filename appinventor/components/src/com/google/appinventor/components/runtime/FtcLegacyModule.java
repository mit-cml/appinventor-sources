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
import com.qualcomm.robotcore.hardware.LegacyModule;

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
public final class FtcLegacyModule extends FtcHardwareDevice<LegacyModule> {

  private volatile LegacyModule legacyModule;

  /**
   * Creates a new FtcLegacyModule component.
   */
  public FtcLegacyModule(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Enable a physical port in NXT I2C read mode.")
  public void EnableNxtI2cReadMode(int physicalPort, int i2cAddress, int memAddress, int memLength) {
    if (legacyModule != null) {
      try {
        legacyModule.enableNxtI2cReadMode(physicalPort, i2cAddress, memAddress, memLength);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Read the device memory map and return a byte array; only works in NXT I2C read mode.")
  public Object ReadLegacyModuleCache(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] data = legacyModule.readLegacyModuleCache(physicalPort);
        if (data != null) {
          return data;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadLegacyModuleCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }


  @SimpleFunction(description = "Enable a physical port in NXT I2C write mode.")
  public void EnableNxtI2cWriteMode(int physicalPort, int i2cAddress, int memAddress,
      Object byteArray) {
    if (legacyModule != null) {
      try {
        if (byteArray instanceof byte[]) {
          legacyModule.enableNxtI2cWriteMode(physicalPort, i2cAddress, memAddress,
              (byte[]) byteArray);
        } else {
          form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteMode",
              ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write to the device memory map; only works in NXT I2C write mode.")
  public void WriteLegacyModuleCache(int physicalPort, Object byteArray) {
    if (legacyModule != null) {
      try {
        if (byteArray instanceof byte[]) {
          legacyModule.writeLegacyModuleCache(physicalPort, (byte[]) byteArray);
        } else {
          form.dispatchErrorOccurredEvent(this, "WriteLegacyModuleCache",
              ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteLegacyModuleCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }


  @SimpleFunction(description = "Enable a physical port in analog read mode.")
  public void EnableAnalogReadMode(int physicalPort) {
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

  @SimpleFunction(description =
      "Read an analog value from a device and return a byte array; only works in analog read mode.")
  public Object ReadAnalog(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] data = legacyModule.readAnalog(physicalPort);
        if (data != null) {
          return data;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadNumberFromAnalog",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }

  @SimpleFunction(description = "Fetch the cache that is about to be written.")
  public Object FetchLegacyModuleWriteCache(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] data = legacyModule.fetchLegacyModuleWriteCache(physicalPort);
        if (data != null) {
          return data;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "FetchLegacyModuleWriteCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }

  @SimpleFunction(description = "Set the value of digital line 0 or 1 while in analog mode.")
  public void SetDigitalLine(int physicalPort, int line, boolean set) {
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

  @SimpleFunction(description = "Enable or disable 9V power on a port.")
  public void Enable9v(int physicalPort, boolean enable) {
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

  @SimpleFunction(description = "Determine if a physical port is ready.")
  public boolean IsPortReady(int physicalPort) {
    if (legacyModule != null) {
      try {
        return legacyModule.isPortReady(physicalPort);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsPortReady",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  // HardwareDevice implementation

  @Override
  public void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      legacyModule = hardwareMap.legacyModule.get(getDeviceName());
      if (legacyModule == null) {
        deviceNotFound("LegacyModule", hardwareMap.legacyModule);
      }
    }
  }

  @Override
  public void clearHardwareDevice() {
    legacyModule = null;
  }
}
