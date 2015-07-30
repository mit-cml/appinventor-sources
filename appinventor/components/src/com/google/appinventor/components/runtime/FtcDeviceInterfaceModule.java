// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController.Mode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cController.I2cPortReadyCallback;
import com.qualcomm.robotcore.util.SerialNumber;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * A component for a device interface module of an FTC robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.FTC_DEVICE_INTERFACE_MODULE_COMPONENT_VERSION,
    description = "A component for a device interface module of an FTC robot.",
    category = ComponentCategory.FIRSTTECHCHALLENGE,
    nonVisible = true,
    iconName = "images/ftc.png")
@SimpleObject
@UsesLibraries(libraries = "FtcRobotCore.jar")
public final class FtcDeviceInterfaceModule extends FtcHardwareDevice
    implements I2cPortReadyCallback {

  private volatile DeviceInterfaceModule deviceInterfaceModule;
  private final Object portsRegisteredForPortReadyCallbackLock = new Object();
  private final List<Integer> portsRegisteredForPortReadyCallback = Lists.newArrayList();

  /**
   * Creates a new FtcDeviceInterfaceModule component.
   */
  public FtcDeviceInterfaceModule(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleEvent(description = "This event is triggered when an I2C port is ready. This event is " +
      "only enabled if EnableI2cReadMode or EnableI2cWriteMode is used.")
  public void I2cPortIsReady(int port) {
    EventDispatcher.dispatchEvent(this, "I2cPortIsReady", port);
  }

  @SimpleFunction(description = "A byte containing the current logic levels present in the " +
      "D7-D0 channel pins. If a particular pin is in output mode, the current output state will " +
      "be reported.")
  public int GetDigitalInputStateByte() {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getDigitalInputStateByte() & 0xFF;
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetDigitalInputStateByte",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "If a particular bit is set to one, the corresponding channel " +
      "pin will be in output mode; else it will be in input mode.")
  public void SetDigitalIOControlByte(int input) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setDigitalIOControlByte((byte) input);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetDigitalIOControlByte",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Get the digital IO control byte.")
  public int GetDigitalIOControlByte() {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getDigitalIOControlByte() & 0xFF;
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetDigitalIOControlByte",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "If a particular control field bit is set to one, the channel " +
      "pin will be in output mode and will reflect the value of the corresponding field bit.")
  public void SetDigitalOutputByte(int input) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setDigitalOutputByte((byte) input);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetDigitalOutputByte",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "The D7-D0 output set field is a byte containing the required " +
      "I/O output of the D7-D0 channel pins. If the corresponding Dy-D0 I/O control field bit " +
      "is set to one, the channel pin will be in output mode and will reflect the value of the " +
      "corresponding D7-D0 output set field bit.")
  public int GetDigitalOutputStateByte() {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getDigitalOutputStateByte() & 0xFF;
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetDigitalOutputStateByte",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Indicates whether the LED on the given channel is on or not.")
  public boolean GetLEDState(int channel) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getLEDState(channel);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetLEDState",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Turn on or off a particular LED.")
  public void SetLED(int channel, boolean state) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setLED(channel, state);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetLED",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleProperty(description = "Get the USB serial number of this device.",
      category = PropertyCategory.BEHAVIOR)
  public String SerialNumber() {
    if (deviceInterfaceModule != null) {
      try {
        SerialNumber serialNumber = deviceInterfaceModule.getSerialNumber();
        if (serialNumber != null) {
          return serialNumber.toString();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SerialNumber",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  // for DigitalChannelController

  /**
   * Mode_INPUT property getter.
   */
  @SimpleProperty(description = "The constant for Mode_INPUT.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode_INPUT() {
    return Mode.INPUT.toString();
  }

  /**
   * Mode_OUTPUT property getter.
   */
  @SimpleProperty(description = "The constant for Mode_OUTPUT.",
      category = PropertyCategory.BEHAVIOR)
  public String Mode_OUTPUT() {
    return Mode.OUTPUT.toString();
  }

  @SimpleFunction(description = "Get the mode of a digital channel; INPUT or OUTPUT.")
  public String GetDigitalChannelMode(int channel) {
    if (deviceInterfaceModule != null) {
      try {
        Mode mode = deviceInterfaceModule.getDigitalChannelMode(channel);
        if (mode != null) {
          return mode.toString();
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetDigitalChannelMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return "";
  }

  @SimpleFunction(description = "Set the mode of a digital channel; INPUT or OUTPUT.")
  public void SetDigitalChannelMode(int channel, String mode) {
    if (deviceInterfaceModule != null) {
      try {
        for (Mode modeEnum : Mode.values()) {
          if (modeEnum.toString().equalsIgnoreCase(mode)) {
            deviceInterfaceModule.setDigitalChannelMode(channel, modeEnum);
            return;
          }
        }

        form.dispatchErrorOccurredEvent(this, "SetDigitalChannelMode",
            ErrorMessages.ERROR_FTC_INVALID_DIGITAL_CHANNEL_MODE, mode);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetDigitalChannelMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Get the state of a digital channel.\n" +
      "If it's in OUTPUT mode, this will return the output bit.\n" + 
      "If the channel is in INPUT mode, this will return the input bit.")
  public boolean GetDigitalChannelState(int channel) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getDigitalChannelState(channel);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetDigitalChannelState",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Set the state of a digital channel.\n" +
      "The behavior of this method is undefined for digital channels in INPUT mode.")
  public void SetDigitalChannelState(int channel, boolean state) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setDigitalChannelState(channel, state);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetDigitalChannelState",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  // For AnalogInputController

  @SimpleFunction(description = "Return the current ADC results from the A0-A7 channel input pins.")
  public int GetAnalogInputValue(int channel) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getAnalogInputValue(channel);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetAnalogInputValue",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  // for PWMOutputController

  @SimpleFunction(description = "Sets the pulse width for the channel output in units of 1 " +
      "microsecond. Setting a value greater than the output period will result in the output " +
      "being permanently set to 1.")
  public void SetPulseWidthOutputTime(int port, double time) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setPulseWidthOutputTime(port, (int) time);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetPulseWidthOutputTime",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Sets the pulse repetition period for the channel output in " +
      "units of 1 microsecond. If the pwm feature is being used to generate pulses for a " +
      "standard R/C style servo, the output period should be set to 20,000 and the output on " +
      "time should be set within the range 750-2,250.")
  public void SetPulseWidthPeriod(int port, double period) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setPulseWidthPeriod(port, (int) period);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetPulseWidthPeriod",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Gets the pulse width for the channel output in units of 1 " +
      "microsecond.")
  public double GetPulseWidthOutputTime(int port) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getPulseWidthOutputTime(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetPulseWidthOutputTime",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Gets the pulse repetition period for the channel output in " +
      "units of 1 microsecond.")
  public double GetPulseWidthPeriod(int port) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getPulseWidthPeriod(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetPulseWidthPeriod",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  // I2cController

  @SimpleFunction(description = "Enable read mode for a particular I2C device and enable the " +
      "I2cPortIsReady event for the given port.")
  public void EnableI2cReadMode(int port, int i2cAddress, int memAddress, int length) {
    if (deviceInterfaceModule != null) {
      try {
        synchronized (portsRegisteredForPortReadyCallbackLock) {
          if (!portsRegisteredForPortReadyCallback.contains(port)) {
            deviceInterfaceModule.registerForI2cPortReadyCallback(this, port);
            portsRegisteredForPortReadyCallback.add(port);
          }
        }
        deviceInterfaceModule.enableI2cReadMode(port, i2cAddress, memAddress, length);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableI2cReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable write mode for a particular I2C device and enable the " +
      "I2cPortIsReady event for the given port.")
  public void EnableI2cWriteMode(int port, int i2cAddress, int memAddress, int length) {
    if (deviceInterfaceModule != null) {
      try {
        synchronized (portsRegisteredForPortReadyCallbackLock) {
          if (!portsRegisteredForPortReadyCallback.contains(port)) {
            deviceInterfaceModule.registerForI2cPortReadyCallback(this, port);
            portsRegisteredForPortReadyCallback.add(port);
          }
        }
        deviceInterfaceModule.enableI2cWriteMode(port, i2cAddress, memAddress, length);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableI2cWriteMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Get a copy of the contents of the cache that I2C reads will be " +
      "populated into. (byte array)")
  public Object GetI2cReadCache(int port) {
    if (deviceInterfaceModule != null) {
      try {
        Lock lock = deviceInterfaceModule.getI2cReadCacheLock(port);
        lock.lock();
        try {
          byte[] src = deviceInterfaceModule.getI2cReadCache(port);
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

  @SimpleFunction(description = "Get a copy of the contents of the I2C write cache. " +
      "(byte array)")
  public Object GetI2cWriteCache(int port) {
    if (deviceInterfaceModule != null) {
      try {
        Lock lock = deviceInterfaceModule.getI2cWriteCacheLock(port);
        lock.lock();
        try {
          byte[] src = deviceInterfaceModule.getI2cWriteCache(port);
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
    if (deviceInterfaceModule != null) {
      try {
        if (byteArray instanceof byte[]) {
          byte[] src = (byte[]) byteArray;
          Lock lock = deviceInterfaceModule.getI2cWriteCacheLock(port);
          lock.lock();
          try {
            byte[] dest = deviceInterfaceModule.getI2cWriteCache(port);
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

  @SimpleFunction(description = "Set the port action flag; this flag tells the Device Interface " +
      "Module to send the current data in its buffer to the I2C device.")
  public void SetI2cPortActionFlag(int port) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setI2cPortActionFlag(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetI2cPortActionFlag",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }
  
  @SimpleFunction(description = "Get the port action flag; this flag is set if the particular port is busy.")
  public boolean IsI2cPortActionFlagSet(int port) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.isI2cPortActionFlagSet(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortActionFlagSet",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  @SimpleFunction(description = "Read from the Device Interface Module to the I2C read cache.")
  public void ReadI2cCacheFromModule(int port) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.readI2cCacheFromModule(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadI2cCacheFromModule",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write from the I2C write cache to the Device Interface Module.")
  public void WriteI2cCacheToModule(int port) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.writeI2cCacheToModule(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteI2cCacheToModule",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Write just the port action flag in the Device Interface " +
      "Module's cache to the I2C device.")
  public void WriteI2cPortFlagOnlyToModule(int port) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.writeI2cPortFlagOnlyToModule(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteI2cPortFlagOnlyToModule",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Is the port in read mode?")
  public boolean IsI2cPortInReadMode(int port) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.isI2cPortInReadMode(port);
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
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.isI2cPortInWriteMode(port);
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
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.isI2cPortReady(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsI2cPortReady",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }

  // for AnalogOutputController 

  @SimpleFunction(description = "Sets the channel output voltage.\n" +
      "If mode == 0: takes input from -1023-1023, output in the range -4 to +4 volts.\n" +
      "If mode == 1, 2, or 3: takes input from 0-1023, output in the range 0 to 8 volts.")
  public void SetAnalogOutputVoltage(int port, int voltage) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setAnalogOutputVoltage(port, voltage);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetAnalogOutputVoltage",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Sets the channel output frequency in the range 1-5,000 Hz in " +
      "mode 1, 2 or 3.")
  public void SetAnalogOutputFrequency(int port, int frequency) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setAnalogOutputFrequency(port, frequency);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetAnalogOutputFrequency",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Sets the channel operating mode.\n" +
      "Mode 0: Voltage output. Range: -4V - 4V.\n" +
      "Mode 1: Sine wave output. Range: 0 - 8V.\n" +
      "Mode 2: Square wave output. Range: 0 - 8V.\n" +
      "Mode 3: Triangle wave output. Range: 0 - 8V.")
  public void SetAnalogOutputMode(int port, int mode) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setAnalogOutputMode(port, (byte) mode);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetAnalogOutputMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  // I2CPortReadyCallback implementation

  @Override
  public void portIsReady(int port) {
    I2cPortIsReady(port);
  }

  // FtcRobotController.HardwareDevice implementation

  @Override
  public Object initHardwareDeviceImpl(HardwareMap hardwareMap) {
    if (hardwareMap != null) {
      deviceInterfaceModule = hardwareMap.deviceInterfaceModule.get(getDeviceName());
      if (deviceInterfaceModule == null) {
        deviceNotFound("DeviceInterfaceModule", hardwareMap.deviceInterfaceModule);
      }
    }
    return deviceInterfaceModule;
  }

  @Override
  public void clearHardwareDevice() {
    synchronized (portsRegisteredForPortReadyCallbackLock) {
      for (Integer port : portsRegisteredForPortReadyCallback) {
        deviceInterfaceModule.deregisterForPortReadyCallback(port);
      }
      portsRegisteredForPortReadyCallback.clear();
    }
    deviceInterfaceModule = null;
  }

  // TODO(lizlooney): remove these deprecated functions.
  @SimpleFunction(description = "ReadI2c", userVisible = false)
  public Object ReadI2c(int port) {
    return new byte[0];
  }
  @SimpleFunction(description = "WriteI2c", userVisible = false)
  public void WriteI2c(int port, Object byteArray) {
  }
  @SimpleFunction(description = "GetAnalogOutputFrequency", userVisible = false)
  public int GetAnalogOutputFrequency(int port) {
    return 0;
  }
  @SimpleFunction(description = "GetAnalogOutputMode", userVisible = false)
  public int GetAnalogOutputMode(int port) {
    return 0;
  }
  @SimpleFunction(description = "GetAnalogOutputVoltage", userVisible = false)
  public int GetAnalogOutputVoltage(int port) {
    return 0;
  }
  @SimpleFunction(description = "IsPortReady", userVisible = false)
  public boolean IsPortReady(int physicalPort) {
    return false;
  }
  @SimpleFunction(description = "ReadDeviceInterfaceModuleI2cCache", userVisible = false)
  public Object ReadDeviceInterfaceModuleI2cCache(int physicalPort) {
    return new byte[0];
  }
  @SimpleFunction(description = "WriteDeviceInterfaceModuleI2cCache", userVisible = false)
  public void WriteDeviceInterfaceModuleI2cCache(int physicalPort, Object byteArray) {
  }
  @SimpleFunction(description = "WriteI2cPortFlagOnlyFromModule", userVisible = false)
  public void WriteI2cPortFlagOnlyFromModule(int port) {
  }
}
