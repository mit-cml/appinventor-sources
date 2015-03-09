// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.ftc.FtcHardwareDevice;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.LegacyModule;
import com.qualcomm.robotcore.util.TypeConversion;

import java.util.ArrayList;
import java.util.Collections;
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
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cReadMode",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a list of byte values.")
  public void EnableNxtI2cWriteModeBytes(int physicalPort, int i2cAddress, int memAddress,
      YailList initialValues) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertYailListToBytes(initialValues, "EnableNxtI2cWriteModeBytes");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteModeBytes",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a 1-byte number.")
  public void EnableNxtI2cWriteMode1ByteNumber(int physicalPort, int i2cAddress, int memAddress,
      String initialValue) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo1Byte(initialValue, "EnableNxtI2cWriteMode1ByteNumber");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteMode1ByteNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a 2-byte number.")
  public void EnableNxtI2cWriteMode2ByteNumber(int physicalPort, int i2cAddress, int memAddress,
      String initialValue) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo2Bytes(initialValue, "EnableNxtI2cWriteMode2ByteNumber");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteMode2ByteNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a 4-byte number.")
  public void EnableNxtI2cWriteMode4ByteNumber(int physicalPort, int i2cAddress, int memAddress,
      String initialValue) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo4Bytes(initialValue, "EnableNxtI2cWriteMode4ByteNumber");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteMode4ByteNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a 8-byte number.")
  public void EnableNxtI2cWriteMode8ByteNumber(int physicalPort, int i2cAddress, int memAddress,
      String initialValue) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo8Bytes(initialValue, "EnableNxtI2cWriteMode8ByteNumber");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteMode8ByteNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable a physical port in analog read mode.")
  public void EnableAnalogReadMode(int physicalPort, int i2cAddress) {
    if (legacyModule != null) {
      try {
        legacyModule.enableAnalogReadMode(physicalPort, i2cAddress);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "EnableAnalogReadMode",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable or disable 9V power on a port.")
  public void Enable9v(int physicalPort, boolean enable) {
    if (legacyModule != null) {
      try {
        legacyModule.enable9v(physicalPort, enable);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "Enable9v",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Set the value of digital line 0 or 1 while in analog mode.")
  public void SetDigitalLine(int physicalPort, int line, boolean set) {
    if (legacyModule != null) {
      try {
        legacyModule.setDigitalLine(physicalPort, line, set);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "SetDigitalLine",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Read signed bytes from the device memory map; only works in NXT I2C read mode.")
  public List<Integer> ReadI2cSignedBytes(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readI2c(physicalPort);
        return convertBytesToYailListSigned(bytes);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "ReadI2cSignedBytes",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
    return Collections.emptyList();
  }

  @SimpleFunction(description =
      "Read unsigned bytes from the device memory map; only works in NXT I2C read mode.")
  public List<Integer> ReadI2cUnsignedBytes(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readI2c(physicalPort);
        return convertBytesToYailListUnsigned(bytes);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "ReadI2cUnsignedBytes",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
    return Collections.emptyList();
  }

  @SimpleFunction(description =
      "Read a number from the device memory map; only works in NXT I2C read mode.")
  public long ReadI2cNumber(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readI2c(physicalPort);
        return convertBytesToNumber(bytes);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "ReadI2cNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Write to the device memory map; only works in NXT I2C write mode.")
  public void WriteI2cBytes(int physicalPort, YailList data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertYailListToBytes(data, "WriteI2cBytes");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "WriteI2cBytes",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Write a 1-byte number to the device memory map; only works in NXT I2C write mode.")
  public void WriteI2c1ByteNumber(int physicalPort, String data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo1Byte(data, "WriteI2c1ByteNumber");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "WriteI2c1ByteNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }


  @SimpleFunction(description =
      "Write a 2-byte number to the device memory map; only works in NXT I2C write mode.")
  public void WriteI2c2ByteNumber(int physicalPort, String data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo2Bytes(data, "WriteI2c2ByteNumber");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "WriteI2c2ByteNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }


  @SimpleFunction(description =
      "Write a 4-byte number to the device memory map; only works in NXT I2C write mode.")
  public void WriteI2c4ByteNumber(int physicalPort, String data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo4Bytes(data, "WriteI2c4ByteNumber");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "WriteI2c4ByteNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }


  @SimpleFunction(description =
      "Write a 8-byte number to the device memory map; only works in NXT I2C write mode.")
  public void WriteI2c8ByteNumber(int physicalPort, String data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo8Bytes(data, "WriteI2c8ByteNumber");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "WriteI2c8ByteNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Read an analog value as signed bytes from a device; only works in analog read mode.")
  public List<Integer> ReadAnalogSignedBytes(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readAnalog(physicalPort);
        return convertBytesToYailListSigned(bytes);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "ReadAnalogSignedBytes",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
    return Collections.emptyList();
  }

  @SimpleFunction(description =
      "Read an analog value as unsigned bytes from a device; only works in analog read mode.")
  public List<Integer> ReadAnalogUnsignedBytes(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readAnalog(physicalPort);
        return convertBytesToYailListUnsigned(bytes);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "ReadAnalogUnsignedBytes",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
    return Collections.emptyList();
  }

  @SimpleFunction(description =
      "Read an analog value from a device; only works in analog read mode.")
  public long ReadAnalogNumber(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readAnalog(physicalPort);
        return convertBytesToNumber(bytes);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "ReadAnalogNumber",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Determine if a physical port is ready.")
  public boolean IsPortReady(int physicalPort) {
    if (legacyModule != null) {
      try {
        return legacyModule.isPortReady(physicalPort);
      } catch (Throwable e) {
        form.dispatchErrorOccurredEvent(this, "IsPortReady",
            ErrorMessages.ERROR_FTC_LEGACY_MODULE, e.toString());
      }
    }
    return false;
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public void debugHardwareDevice(StringBuilder sb) {
    sb.append("legacyModule is ").append((legacyModule == null) ? "null" : "not null").append("\n");
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

  private byte[] convertYailListToBytes(YailList list, String functionName) {
    Object[] array = list.toArray();
    byte[] bytes = new byte[array.length];
    for (int i = 0; i < array.length; i++) {
      // We use Object.toString here because the element might be a String or it might be some
      // numeric class.
      Object element = array[i];
      String s = element.toString();
      try {
        bytes[i] = Byte.decode(s);
      } catch (NumberFormatException e) {
        form.dispatchErrorOccurredEvent(this, functionName,
            ErrorMessages.ERROR_FTC_LEGACY_MODULE_COULD_NOT_DECODE_ELEMENT, i + 1, s);
        return null;
      }
    }
    return bytes;
  }

  private byte[] convertNumberTo1Byte(String number, String functionName) {
    byte n;
    try {
      n = Byte.decode(number);
    } catch (NumberFormatException e) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_FTC_LEGACY_MODULE_COULD_NOT_DECODE_NUMBER, number);
      return null;
    }
    return new byte[]{n};
  }

  private byte[] convertNumberTo2Bytes(String number, String functionName) {
    short n;
    try {
      n = Short.decode(number);
    } catch (NumberFormatException e) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_FTC_LEGACY_MODULE_COULD_NOT_DECODE_NUMBER, number);
      return null;
    }
    return TypeConversion.shortToByteArray(n);
  }

  private byte[] convertNumberTo4Bytes(String number, String functionName) {
    int n;
    try {
      n = Integer.decode(number);
    } catch (NumberFormatException e) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_FTC_LEGACY_MODULE_COULD_NOT_DECODE_NUMBER, number);
      return null;
    }
    return TypeConversion.intToByteArray(n);
  }

  private byte[] convertNumberTo8Bytes(String number, String functionName) {
    long n;
    try {
      n = Long.decode(number);
    } catch (NumberFormatException e) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_FTC_LEGACY_MODULE_COULD_NOT_DECODE_NUMBER, number);
      return null;
    }
    return TypeConversion.longToByteArray(n);
  }

  private List<Integer> convertBytesToYailListSigned(byte[] bytes) {
    List<Integer> list = new ArrayList<Integer>();
    for (int i = 0; i < bytes.length; i++) {
      int n = bytes[i];
      list.add(n);
    }
    return list;
  }

  private List<Integer> convertBytesToYailListUnsigned(byte[] bytes) {
    List<Integer> list = new ArrayList<Integer>();
    for (int i = 0; i < bytes.length; i++) {
      int n = bytes[i] & 0xFF;
      list.add(n);
    }
    return list;
  }

  private long convertBytesToNumber(byte[] bytes) {
    switch (bytes.length) {
      case 1:
        return bytes[0];
      case 2:
        return TypeConversion.byteArrayToShort(bytes);
      case 4:
        return TypeConversion.byteArrayToInt(bytes);
      case 8:
      default:
        return TypeConversion.byteArrayToLong(bytes);
    }
  }
}
