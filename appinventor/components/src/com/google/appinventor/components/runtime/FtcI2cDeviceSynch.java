// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbDeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cController.I2cPortReadyCallback;
import com.qualcomm.robotcore.hardware.I2cDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch.HeartbeatAction;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch.ReadMode;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch.ReadWindow;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchImpl;
import com.qualcomm.robotcore.util.TypeConversion;

/**
 * A component that provides synchronous access for an I2C device of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_I2C_DEVICE_SYNCH_COMPONENT_VERSION,
    description = "A component for an I2C device of an FTC robot, that provides synchronous access.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftcI2cDeviceSynch.png")
@SimpleObject
@UsesLibraries(libraries = "FtcHardware.jar,FtcRobotCore.jar")
public final class FtcI2cDeviceSynch extends FtcHardwareDevice {

  private volatile I2cDevice i2cDevice;
  private volatile I2cDeviceSynch i2cDeviceSynch;
  private volatile I2cDeviceSynchImpl i2cDeviceSynchImpl;

  /**
   * Creates a new FtcI2cDeviceSynch component.
   */
  public FtcI2cDeviceSynch(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * ReadMode_REPEAT property getter.
   */
  @SimpleProperty(description = "The constant for ReadMode_REPEAT.",
      category = PropertyCategory.BEHAVIOR)
  public String ReadMode_REPEAT() {
    return ReadMode.REPEAT.toString();
  }

  /**
   * ReadMode_BALANCED property getter.
   */
  @SimpleProperty(description = "The constant for ReadMode_BALANCED.",
      category = PropertyCategory.BEHAVIOR)
  public String ReadMode_BALANCED() {
    return ReadMode.BALANCED.toString();
  }

  /**
   * ReadMode_ONLY_ONCE property getter.
   */
  @SimpleProperty(description = "The constant for ReadMode_ONLY_ONCE.",
  category = PropertyCategory.BEHAVIOR)
  public String ReadMode_ONLY_ONCE() {
    return ReadMode.ONLY_ONCE.toString();
  }

  @SimpleFunction(description = "Initialize this I2C device")
  public void Initialize(int i2cAddress) {
    checkHardwareDevice();
    try {
      if (i2cDeviceSynchImpl != null) {
        i2cDeviceSynchImpl.close();
        i2cDeviceSynchImpl = null;
      }
      i2cDeviceSynch = null;
      if (i2cDevice != null) {
        i2cDeviceSynchImpl = new I2cDeviceSynchImpl(i2cDevice, i2cAddress, false);
        i2cDeviceSynch = i2cDeviceSynchImpl;
        i2cDeviceSynch.engage();
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "Initialize",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Set the set of registers that we will read and read and read " +
      "again on every hardware cycle.")
  public void SetReadWindow(int start, int count, String readMode) {
    checkHardwareDevice();
    try {
      if (i2cDeviceSynch != null) {
        for (ReadMode readModeValue : ReadMode.values()) {
          if (readModeValue.toString().equalsIgnoreCase(readMode)) {
            ReadWindow readWindow = new ReadWindow(start, count, readModeValue);
            i2cDeviceSynch.setReadWindow(readWindow);
            break;
          }
        }
        form.dispatchErrorOccurredEvent(this, "SetReadWindow",
            ErrorMessages.ERROR_FTC_INVALID_I2C_DEVICE_SYNCH_READ_MODE, readMode);
      }
    } catch (Throwable e) {
      e.printStackTrace();
      form.dispatchErrorOccurredEvent(this, "SetReadWindow",
          ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
    }
  }

  @SimpleFunction(description = "Read the byte at the indicated register.")
  public int Read1Byte(int register, boolean unsigned) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        byte b = i2cDeviceSynch.read8(register);
        if (unsigned) {
          return TypeConversion.unsignedByteToInt(b);
        } else {
          return b;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Read1Byte",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Read a contiguous set of registers and return a byte array.")
  public Object Read(int start, int count) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        byte[] src = i2cDeviceSynch.read(start, count);
        if (src != null) {
          byte[] dest = new byte[src.length];
          System.arraycopy(src, 0, dest, 0, src.length);
          return dest;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Read",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }

  @SimpleFunction(description = "Write a byte to the indicated register.")
  public void Write1Byte(int register, String number, boolean waitForCompletion) {
    // The number parameter is a String, which allows decimal, hexadecimal, and octal numbers to be
    // given, for example "32", "0x20", or "040".
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        byte b = Byte.decode(number);
        i2cDeviceSynch.write8(register, b, waitForCompletion);
      } catch (NumberFormatException e) {
        form.dispatchErrorOccurredEvent(this, "Write1Byte",
            ErrorMessages.ERROR_FTC_INVALID_NUMBER, number);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Write1Byte",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write a byte array to a contiguous set of registers.")
  public void Write(int start, Object byteArray, boolean waitForCompletion) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        if (byteArray instanceof byte[]) {
          byte[] array = (byte[]) byteArray;
          i2cDeviceSynch.write(start, array, waitForCompletion);
        } else {
          form.dispatchErrorOccurredEvent(this, "Write",
              ErrorMessages.ERROR_FTC_INVALID_BYTE_ARRAY, "byteArray");
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "Write",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * HeartbeatInterval property setter.
   */
  @SimpleProperty
  public void HeartbeatInterval(int interval) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        i2cDeviceSynch.setHeartbeatInterval(interval);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "HeartbeatInterval",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * HeartbeatInterval property getter.
   */
  @SimpleProperty(description = "The heartbeat interval to prevent a timeout from occurring.",
      category = PropertyCategory.BEHAVIOR)
  public int HeartbeatInterval() {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        return i2cDeviceSynch.getHeartbeatInterval();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "HeartbeatInterval",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  /**
   * HeartbeatActionRereadLastRead property setter.
   */
  @SimpleProperty
  public void HeartbeatActionRereadLastRead(boolean enable) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        HeartbeatAction newHeartbeatAction;
        HeartbeatAction currentHeartbeatAction = i2cDeviceSynch.getHeartbeatAction();
        if (currentHeartbeatAction != null) {
          newHeartbeatAction = new HeartbeatAction(
              enable /* rereadLastRead */,
              currentHeartbeatAction.rewriteLastWritten,
              currentHeartbeatAction.heartbeatReadWindow);
        } else {
          newHeartbeatAction = new HeartbeatAction(
              enable /* rereadLastRead */,
              false /* rewriteLastWritten */,
              null /* heartbeatReadWindow */);
        }
        i2cDeviceSynch.setHeartbeatAction(newHeartbeatAction);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "HeartbeatActionRereadLastRead",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * HeartbeatActionRereadLastRead property getter.
   */
  @SimpleProperty(description = "Whether to re-issue the last I2C read operation, if possible.",
      category = PropertyCategory.BEHAVIOR)
  public boolean HeartbeatActionRereadLastRead() {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        HeartbeatAction currentHeartbeatAction = i2cDeviceSynch.getHeartbeatAction();
        if (currentHeartbeatAction != null) {
          return currentHeartbeatAction.rereadLastRead;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "HeartbeatActionRereadLastRead",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  /**
   * HeartbeatActionRewriteLastWritten property setter.
   */
  @SimpleProperty
  public void HeartbeatActionRewriteLastWritten(boolean enable) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        HeartbeatAction newHeartbeatAction;
        HeartbeatAction currentHeartbeatAction = i2cDeviceSynch.getHeartbeatAction();
        if (currentHeartbeatAction != null) {
          newHeartbeatAction = new HeartbeatAction(
              currentHeartbeatAction.rereadLastRead,
              enable /* rewriteLastWritten */,
              currentHeartbeatAction.heartbeatReadWindow);
        } else {
          newHeartbeatAction = new HeartbeatAction(
              false /* rereadLastRead */,
              enable /* rewriteLastWritten */,
              null /* heartbeatReadWindow */);
        }
        i2cDeviceSynch.setHeartbeatAction(newHeartbeatAction);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "HeartbeatActionRewriteLastWritten",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * HeartbeatActionRewriteLastWritten property getter.
   */
  @SimpleProperty(description = "Whether to re-issue the last I2C write operation, if possible.",
      category = PropertyCategory.BEHAVIOR)
  public boolean HeartbeatActionRewriteLastWritten() {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        HeartbeatAction currentHeartbeatAction = i2cDeviceSynch.getHeartbeatAction();
        if (currentHeartbeatAction != null) {
          return currentHeartbeatAction.rewriteLastWritten;
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "HeartbeatActionRewriteLastWritten",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Set the heartbeat action read window.")
  public void SetHeartbeatActionReadWindow(int start, int count, String readMode) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        for (ReadMode readModeValue : ReadMode.values()) {
          if (readModeValue.toString().equalsIgnoreCase(readMode)) {
            ReadWindow readWindow = new ReadWindow(start, count, readModeValue);
            HeartbeatAction newHeartbeatAction;
            HeartbeatAction currentHeartbeatAction = i2cDeviceSynch.getHeartbeatAction();
            if (currentHeartbeatAction != null) {
              newHeartbeatAction = new HeartbeatAction(
                  currentHeartbeatAction.rereadLastRead,
                  currentHeartbeatAction.rewriteLastWritten,
                  readWindow);
            } else {
              newHeartbeatAction = new HeartbeatAction(
                  false /* rereadLastRead */,
                  false /* rewriteLastWritten */,
                  readWindow);
            }
            i2cDeviceSynch.setHeartbeatAction(newHeartbeatAction);
            return;
          }
        }
        form.dispatchErrorOccurredEvent(this, "SetHeartbeatActionReadWindow",
            ErrorMessages.ERROR_FTC_INVALID_I2C_DEVICE_SYNCH_READ_MODE, readMode);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetHeartbeatActionReadWindow",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Whether this I2C device client is alive and operational.")
  public boolean IsArmed() {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        return i2cDeviceSynch.isArmed();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsArmed",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  /**
   * MAX_NEW_I2C_ADDRESS property getter.
   */
  @SimpleProperty(description = "The constant for MAX_NEW_I2C_ADDRESS.",
      category = PropertyCategory.BEHAVIOR)
  public int MAX_NEW_I2C_ADDRESS() {
    return ModernRoboticsUsbDeviceInterfaceModule.MAX_NEW_I2C_ADDRESS;
  }

  /**
   * MIN_NEW_I2C_ADDRESS property getter.
   */
  @SimpleProperty(description = "The constant for MIN_NEW_I2C_ADDRESS.",
      category = PropertyCategory.BEHAVIOR)
  public int MIN_NEW_I2C_ADDRESS() {
    return ModernRoboticsUsbDeviceInterfaceModule.MIN_NEW_I2C_ADDRESS;
  }

  /**
   * I2cAddress property setter.
   */
  @SimpleProperty
  public void I2cAddress(int newAddress) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        i2cDeviceSynch.setI2cAddr(newAddress);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "I2cAddress",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  /**
   * I2cAddress property getter.
   */
  @SimpleProperty(description = "The I2C address of the gyro sensor. " + 
      "Not all gyro sensors support this feature.",
      category = PropertyCategory.BEHAVIOR)
  public int I2cAddress() {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        return i2cDeviceSynch.getI2cAddr();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "I2cAddress",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Turn logging on or off.")
  public void SetLogging(boolean enable) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        i2cDeviceSynch.setLogging(enable);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetLogging",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Set the tag to use when logging is on.")
  public void SetLoggingTag(String loggingTag) {
    checkHardwareDevice();
    if (i2cDeviceSynch != null) {
      try {
        i2cDeviceSynch.setLoggingTag(loggingTag);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetLoggingTag",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    // We just get the I2cDevice here and in Initialize method, we instantiate the I2cDeviceSynch.
    i2cDevice = hardwareMap.i2cDevice.get(getDeviceName());
    return i2cDevice;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("I2cDeviceSynch", hardwareMap.i2cDevice);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    if (i2cDeviceSynchImpl != null) {
      i2cDeviceSynchImpl.close();
      i2cDeviceSynchImpl = null;
    }
    i2cDeviceSynch = null;
    i2cDevice = null;
  }
}
