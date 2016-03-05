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

  @SimpleEvent(description = "This event is triggered when an I2C port is ready, " +
      "after the latest data has been read from the I2C Controller.\n" +
      "This event is only enabled if EnableI2cReadMode or EnableI2cWriteMode is used.")
  public void I2cPortIsReady(int port) {
    EventDispatcher.dispatchEvent(this, "I2cPortIsReady", port);
  }

  @SimpleFunction(description = "A byte containing the current logic levels present in the " +
      "D7-D0 channel pins. If a particular pin is in output mode, the current output state will " +
      "be reported.")
  public int GetDigitalInputStateByte() {
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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

  @SimpleFunction(description = "Get the mode of a digital channel.\n" +
      "Valid values are Mode_INPUT or Mode_OUTPUT.")
  public String GetDigitalChannelMode(int channel) {
    checkHardwareDevice();
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

  @SimpleFunction(description = "Set the mode of a digital channel.\n" +
      "Valid values are Mode_INPUT or Mode_OUTPUT.")
  public void SetDigitalChannelMode(int channel, String mode) {
    checkHardwareDevice();
    if (deviceInterfaceModule != null) {
      try {
        for (Mode modeValue : Mode.values()) {
          if (modeValue.toString().equalsIgnoreCase(mode)) {
            deviceInterfaceModule.setDigitalChannelMode(channel, modeValue);
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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

  @SimpleFunction(description = "Set the pulse width output time for this channel.\n" +
      "Typically set to a value between 750 and 2,250 to control a servo.")
  public void SetPulseWidthOutputTime(int port, int time) {
    checkHardwareDevice();
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setPulseWidthOutputTime(port, time);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetPulseWidthOutputTime",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Set the pulse width output period.\n" +
      "Typically set to 20,000 to control servo.")
  public void SetPulseWidthPeriod(int port, int period) {
    checkHardwareDevice();
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setPulseWidthPeriod(port, period);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetPulseWidthPeriod",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Gets the pulse width for the channel output in " +
      "units of 1 microsecond.")
  public int GetPulseWidthOutputTime(int port) {
    checkHardwareDevice();
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

  @SimpleFunction(description = "Gets the pulse repetition period for the " +
      "channel output in units of 1 microsecond.")
  public int GetPulseWidthPeriod(int port) {
    checkHardwareDevice();
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
  public void EnableI2cReadMode(int physicalPort, int i2cAddress, int memAddress, int length) {
    checkHardwareDevice();
    if (deviceInterfaceModule != null) {
      try {
        synchronized (portsRegisteredForPortReadyCallbackLock) {
          if (!portsRegisteredForPortReadyCallback.contains(physicalPort)) {
            deviceInterfaceModule.registerForI2cPortReadyCallback(this, physicalPort);
            portsRegisteredForPortReadyCallback.add(physicalPort);
          }
        }
        deviceInterfaceModule.enableI2cReadMode(physicalPort, i2cAddress, memAddress, length);
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
    if (deviceInterfaceModule != null) {
      try {
        synchronized (portsRegisteredForPortReadyCallbackLock) {
          if (!portsRegisteredForPortReadyCallback.contains(physicalPort)) {
            deviceInterfaceModule.registerForI2cPortReadyCallback(this, physicalPort);
            portsRegisteredForPortReadyCallback.add(physicalPort);
          }
        }
        deviceInterfaceModule.enableI2cWriteMode(physicalPort, i2cAddress, memAddress, length);
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
    if (deviceInterfaceModule != null) {
      try {
        byte[] copy = deviceInterfaceModule.getCopyOfReadBuffer(physicalPort);
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
    if (deviceInterfaceModule != null) {
      try {
        byte[] copy = deviceInterfaceModule.getCopyOfWriteBuffer(physicalPort);
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
    if (deviceInterfaceModule != null) {
      try {
        if (byteArray instanceof byte[]) {
          byte[] array = (byte[]) byteArray;
          deviceInterfaceModule.copyBufferIntoWriteBuffer(physicalPort, array);
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
  
  @SimpleFunction(description = "Get the port action flag; this flag is set if " +
      "the particular port is busy.")
  public boolean IsI2cPortActionFlagSet(int port) {
    checkHardwareDevice();
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

  @SimpleFunction(description = "Read the local cache in from the I2C Controller.\n" +
      "NOTE: unless this method is called the internal cache isn't updated.")
  public void ReadI2cCacheFromController(int port) {
    checkHardwareDevice();
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.readI2cCacheFromController(port);
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
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.writeI2cCacheToController(port);
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
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.writeI2cPortFlagOnlyToController(port);
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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
    checkHardwareDevice();
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

  // FtcHardwareDevice implementation

  @Override
  protected Object initHardwareDeviceImpl() {
    deviceInterfaceModule = hardwareMap.deviceInterfaceModule.get(getDeviceName());
    return deviceInterfaceModule;
  }

  @Override
  protected void dispatchDeviceNotFoundError() {
    dispatchDeviceNotFoundError("DeviceInterfaceModule", hardwareMap.deviceInterfaceModule);
  }

  @Override
  protected void clearHardwareDeviceImpl() {
    synchronized (portsRegisteredForPortReadyCallbackLock) {
      for (Integer port : portsRegisteredForPortReadyCallback) {
        deviceInterfaceModule.deregisterForPortReadyCallback(port);
      }
      portsRegisteredForPortReadyCallback.clear();
    }
    deviceInterfaceModule = null;
  }
}
