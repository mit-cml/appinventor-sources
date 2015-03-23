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
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Read signed bytes from the device memory map; only works in NXT I2C read mode.")
  public List<Integer> ReadListOfSignedBytesFromI2c(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readI2c(physicalPort);
        return convertByteArrayToYailListSigned(bytes);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadListOfSignedBytesFromI2c",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return Collections.emptyList();
  }

  @SimpleFunction(description =
      "Read unsigned bytes from the device memory map; only works in NXT I2C read mode.")
  public List<Integer> ReadListOfUnsignedBytesFromI2c(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readI2c(physicalPort);
        return convertByteArrayToYailListUnsigned(bytes);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadListOfUnsignedBytesFromI2c",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return Collections.emptyList();
  }

  @SimpleFunction(description =
      "Read a number from the device memory map; only works in NXT I2C read mode.")
  public long ReadNumberFromI2c(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readI2c(physicalPort);
        return convertByteArrayToNumber(bytes);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadNumberFromI2c",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a list of byte values.")
  public void EnableNxtI2cWriteModeWithListOfBytes(int physicalPort, int i2cAddress,
      int memAddress, YailList initialValues) {
    if (legacyModule != null) {
      try {
        byte[] bytes =
            convertYailListToByteArray(initialValues, "EnableNxtI2cWriteModeWithListOfBytes");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteModeWithListOfBytes",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a 1-byte number.")
  public void EnableNxtI2cWriteModeWith1ByteNumber(int physicalPort, int i2cAddress, int memAddress,
      String initialValue) {
    if (legacyModule != null) {
      try {
        byte[] bytes =
            convertNumberTo1ByteArray(initialValue, "EnableNxtI2cWriteModeWith1ByteNumber");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteModeWith1ByteNumber",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a 2-byte number.")
  public void EnableNxtI2cWriteModeWith2ByteNumber(int physicalPort, int i2cAddress, int memAddress,
      String initialValue) {
    if (legacyModule != null) {
      try {
        byte[] bytes =
            convertNumberTo2ByteArray(initialValue, "EnableNxtI2cWriteModeWith2ByteNumber");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteModeWith2ByteNumber",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a 4-byte number.")
  public void EnableNxtI2cWriteModeWith4ByteNumber(int physicalPort, int i2cAddress, int memAddress,
      String initialValue) {
    if (legacyModule != null) {
      try {
        byte[] bytes =
            convertNumberTo4ByteArray(initialValue, "EnableNxtI2cWriteModeWith4ByteNumber");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteModeWith4ByteNumber",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Enable a physical port in NXT I2C write mode, with a 8-byte number.")
  public void EnableNxtI2cWriteModeWith8ByteNumber(int physicalPort, int i2cAddress, int memAddress,
      String initialValue) {
    if (legacyModule != null) {
      try {
        byte[] bytes =
            convertNumberTo8ByteArray(initialValue, "EnableNxtI2cWriteModeWith8ByteNumber");
        if (bytes != null) {
          legacyModule.enableNxtI2ctWriteMode(physicalPort, i2cAddress, memAddress, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableNxtI2cWriteModeWith8ByteNumber",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write to the device memory map; only works in NXT I2C write mode.")
  public void WriteListOfBytesToI2c(int physicalPort, YailList data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertYailListToByteArray(data, "WriteListOfBytesToI2c");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteListOfBytesToI2c",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description =
      "Write a 1-byte number to the device memory map; only works in NXT I2C write mode.")
  public void Write1ByteNumberToI2c(int physicalPort, String data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo1ByteArray(data, "Write1ByteNumberToI2c");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Write1ByteNumberToI2c",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }


  @SimpleFunction(description =
      "Write a 2-byte number to the device memory map; only works in NXT I2C write mode.")
  public void Write2ByteNumberToI2c(int physicalPort, String data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo2ByteArray(data, "Write2ByteNumberToI2c");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Write2ByteNumberToI2c",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }


  @SimpleFunction(description =
      "Write a 4-byte number to the device memory map; only works in NXT I2C write mode.")
  public void Write4ByteNumberToI2c(int physicalPort, String data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo4ByteArray(data, "Write4ByteNumberToI2c");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Write4ByteNumberToI2c",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }


  @SimpleFunction(description =
      "Write a 8-byte number to the device memory map; only works in NXT I2C write mode.")
  public void Write8ByteNumberToI2c(int physicalPort, String data) {
    if (legacyModule != null) {
      try {
        byte[] bytes = convertNumberTo8ByteArray(data, "Write8ByteNumberToI2c");
        if (bytes != null) {
          legacyModule.writeI2c(physicalPort, bytes);
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Write8ByteNumberToI2c",
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
      "Read a 2-byte analog value from a device; only works in analog read mode.")
  public long ReadNumberFromAnalog(int physicalPort) {
    if (legacyModule != null) {
      try {
        byte[] bytes = legacyModule.readAnalog(physicalPort);
        return convertByteArrayToNumber(bytes);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadNumberFromAnalog",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
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

  private byte[] convertYailListToByteArray(YailList list, String functionName) {
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

  private byte[] convertNumberTo1ByteArray(String number, String functionName) {
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

  private byte[] convertNumberTo2ByteArray(String number, String functionName) {
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

  private byte[] convertNumberTo4ByteArray(String number, String functionName) {
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

  private byte[] convertNumberTo8ByteArray(String number, String functionName) {
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

  private List<Integer> convertByteArrayToYailListSigned(byte[] bytes) {
    List<Integer> list = new ArrayList<Integer>();
    for (int i = 0; i < bytes.length; i++) {
      int n = bytes[i];
      list.add(n);
    }
    return list;
  }

  private List<Integer> convertByteArrayToYailListUnsigned(byte[] bytes) {
    List<Integer> list = new ArrayList<Integer>();
    for (int i = 0; i < bytes.length; i++) {
      int n = bytes[i] & 0xFF;
      list.add(n);
    }
    return list;
  }

  private static long convertByteArrayToNumber(byte[] bytes) {
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
