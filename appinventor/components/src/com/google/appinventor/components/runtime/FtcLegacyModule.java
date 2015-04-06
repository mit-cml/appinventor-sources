// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
import com.qualcomm.robotcore.util.TypeConversion;

import java.nio.ByteOrder;

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
@UsesLibraries(libraries = "RobotCore.jar")
public final class FtcLegacyModule extends FtcHardwareDevice {

  private volatile LegacyModule legacyModule;

  /**
   * Creates a new FtcLegacyModule component.
   */
  public FtcLegacyModule(ComponentContainer container) {
    super(container.$form());
  }

  // Functions

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

  @SimpleFunction(description = "Read the device memory map; only works in NXT I2C read mode.")
  public Object ReadLegacyModuleCache(int physicalPort) {
    if (legacyModule != null) {
      try {
        return legacyModule.readLegacyModuleCache(physicalPort);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadLegacyModuleCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }


  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode.")
  public void EnableNxtI2cWriteMode(int physicalPort, int i2cAddress,
      int memAddress, Object initialValues) {
    if (legacyModule != null) {
      try {
        if (initialValues.equals("")) {
          legacyModule.enableNxtI2cWriteMode(physicalPort, i2cAddress, memAddress,
              new byte[0]);
        } else if (initialValues instanceof byte[]) {
          legacyModule.enableNxtI2cWriteMode(physicalPort, i2cAddress, memAddress,
              (byte[]) initialValues);
        } else {
          form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteMode",
              ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, "initialValues is not valid");
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write to the device memory map; only works in NXT I2C write mode.")
  public void WriteLegacyModuleCache(int physicalPort, Object data) {
    if (legacyModule != null) {
      try {
        if (data instanceof byte[]) {
          legacyModule.writeLegacyModuleCache(physicalPort, (byte[]) data);
        } else {
          form.dispatchErrorOccurredEvent(this, "WriteLegacyModuleCache",
              ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, "data is not valid");
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteLegacyModuleCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }


  @SimpleFunction(description = "Enable a physical port in analog read mode.")
  public void EnableAnalogReadMode(int physicalPort, int i2cAddress) {
    if (legacyModule != null) {
      try {
        legacyModule.enableAnalogReadMode(physicalPort, i2cAddress);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableAnalogReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Read an analog value from a device; only works in analog read mode.")
  public Object ReadAnalog(int physicalPort) {
    if (legacyModule != null) {
      try {
        return legacyModule.readAnalog(physicalPort);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadNumberFromAnalog",
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

  @SimpleFunction(description = "Convert a 1-byte number to a byte array.")
  public Object Convert1ByteNumberToByteArray(String number) {
    // The number parameter is a String, which allows decimal, hexadecimal, and octal numbers to be
    // given, for example "32", "0x20", or "040".
    try {
      return new byte[] { Byte.decode(number) };
    } catch (NumberFormatException e) {
      form.dispatchErrorOccurredEvent(this, "Convert1ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_INVALID_NUMBER, number);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Convert1ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new byte[] { 0 };
  }

  @SimpleFunction(description = "Convert a 2-byte number to a byte array.")
  public Object Convert2ByteNumberToByteArray(short number, boolean bigEndian) {
    try {
      return TypeConversion.shortToByteArray(number,
          bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Convert2ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new byte[] { 0, 0 };
  }

  @SimpleFunction(description = "Convert a 4-byte number to a byte array.")
  public Object Convert4ByteNumberToByteArray(int number, boolean bigEndian) {
    try {
      return TypeConversion.intToByteArray(number,
          bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Convert4ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new byte[] { 0, 0, 0, 0 };
  }

  @SimpleFunction(description = "Convert a 8-byte number to a byte array.")
  public Object Convert8ByteNumberToByteArray(long number, boolean bigEndian) {
    try {
      return TypeConversion.longToByteArray(number,
          bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Convert8ByteNumberToByteArray",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
  }

  @SimpleFunction(description = "Convert a 1-byte array to an unsigned number.")
  public int ConvertByteArrayToUnsigned1ByteNumber(Object byteArray) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 1) {
          return TypeConversion.unsignedByteToInt(b[0]);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayToUnsigned1ByteNumber",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayToUnsigned1ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert a 1-byte array to a signed number.")
  public int ConvertByteArrayToSigned1ByteNumber(Object byteArray) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 1) {
          return b[0];
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayToSigned1ByteNumber",
            ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayToSigned1ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }


  @SimpleFunction(description = "Convert a 2-byte array to a number.")
  public short ConvertByteArrayTo2ByteNumber(Object byteArray, boolean bigEndian) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 2) {
          return TypeConversion.byteArrayToShort(b,
              bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo2ByteNumber",
          ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo2ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert a 4-byte array to a number.")
  public int ConvertByteArrayTo4ByteNumber(Object byteArray, boolean bigEndian) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 4) {
          return TypeConversion.byteArrayToInt(b,
              bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo4ByteNumber",
          ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo4ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  @SimpleFunction(description = "Convert a 8-byte array to a number.")
  public long ConvertByteArrayTo8ByteNumber(Object byteArray, boolean bigEndian) {
    try {
      if (byteArray instanceof byte[]) {
        byte[] b = (byte[]) byteArray;
        if (b.length >= 8) {
          return TypeConversion.byteArrayToLong(b,
              bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        }
      }
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo8ByteNumber",
          ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY);
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "ConvertByteArrayTo8ByteNumber",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
    return 0;
  }

  // FtcHardwareDevice implementation

  @Override
  protected void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      legacyModule = hardwareMap.legacyModule.get(getDeviceName());
    }
  }

  @Override
  protected void clearHardwareDevice() {
    legacyModule = null;
  }
}
