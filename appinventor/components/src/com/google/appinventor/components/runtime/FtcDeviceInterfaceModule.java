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

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;

import android.util.Log;

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
public final class FtcDeviceInterfaceModule extends FtcHardwareDevice {

  private volatile DeviceInterfaceModule deviceInterfaceModule;

  /**
   * Creates a new FtcDeviceInterfaceModule component.
   */
  public FtcDeviceInterfaceModule(ComponentContainer container) {
    super(container.$form());
  }

  // Fuctions

  @SimpleFunction(description = "Enable a physical port in I2C read mode.")
  public void EnableI2cReadMode(int physicalPort, int i2cAddress, int memAddress, int memLength) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.enableI2cReadMode(physicalPort, i2cAddress, memAddress, memLength);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableI2cReadMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Enable a physical port in I2C write mode.")
  public void EnableI2cWriteMode(int physicalPort, int i2cAddress, int memAddress, Object initialValues) {
    if (deviceInterfaceModule != null) {
      try {
        if (initialValues.equals("")) {
          deviceInterfaceModule.enableI2cWriteMode(physicalPort, i2cAddress, memAddress,
              new byte[0]);
        } else if (initialValues instanceof byte[]) {
          deviceInterfaceModule.enableI2cWriteMode(physicalPort, i2cAddress, memAddress,
              (byte[]) initialValues);
        } else {
          form.dispatchErrorOccurredEvent(this, "EnableI2cWriteMode",
              ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, "initialValues is not valid");
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "EnableI2cWriteMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }


  @SimpleFunction(description = "The lower two bits of the \"setting\" byte indicate the values " +
      "of the blue (0) and red (1) LEDs.")
  public int GetLEDSetting() {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getLEDSetting();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetLEDSetting",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Lower two bits of the \"setting\" byte turn on the blue (0) and " +
      "red (1) LEDs.")
  public void SetLED(int setting) {
    if (deviceInterfaceModule != null) {
      try {
        deviceInterfaceModule.setLED((byte) setting);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "SetLED",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

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

  @SimpleFunction(description = "A byte containing the current logic levels present in the " +
      "D7-D0 channel pins. If a particular pin is in output mode, the current output state will " +
      "be reported.")
  public int GetDigitalInputStateByte() {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getDigitalInputStateByte();
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
  public int GetDigitalOutputByte() {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getDigitalOutputByte();
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetDigitalOutputByte",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Returns the channel output voltage.")
  public int GetAnalogOutputVoltage(int port) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getAnalogOutputVoltage(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetAnalogOutputVoltage",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Sets the channel output voltage. " +
      "If mode == 0: takes input from -1023-1023, output in the range -4 to +4 volts. " +
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

  @SimpleFunction(description = "Returns the channel output frequency in the range of 1-5,000 Hz.")
  public int GetAnalogOutputFrequency(int port) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getAnalogOutputFrequency(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetAnalogOutputFrequency",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Sets the channel output frequency in the range 1-5,000 Hz in " +
      "mode 1, 2 or 3. If mode 0 is selected, this field will be over-written to 0.")
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


  @SimpleFunction(description = "Returns the channel operating mode.")
  public int GetAnalogOutputMode(int port) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.getAnalogOutputMode(port);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "GetAnalogOutputMode",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Sets the channel operating mode. " +
      "Mode 0: Voltage output. Range: -4V - 4V. " +
      "Mode 1: Sine wave output. Range: 0 - 8V. " +
      "Mode 2: Square wave output. Range: 0 - 8V. " +
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

  @SimpleFunction(description = "Returns the pulse width for the chanel output in units of 1 " +
      "microsecond.")
  public int GetPulseWidthOutputTime(int port) {
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

  @SimpleFunction(description = "Sets the pulse width for the channel output in units of 1 " +
      "microsecond. Setting a value greater than the output period will result in the output " +
      "being permanently set to 1.")
  public void SetPulseWidthOutputTime(int port, int time) {
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

  @SimpleFunction(description = "Returns the pulse repetition period for the channel output in " +
      "units of 1 microsecond.")
  public int GetPulseWidthPeriod(int port) {
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

  @SimpleFunction(description = "Sets the pulse repetition period for the channel output in " +
      "units of 1 microsecond. If the pwm feature is being used to generate pulses for a " +
      "standard R/C style servo, the output period should be set to 20,000 and the output on " +
      "time should be set within the range 750-2,250.")
  public void SetPulseWidthPeriod(int port, int period) {
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

  @SimpleFunction(description = "Read the device memory map; only works in I2C read mode.")
  public Object ReadDeviceInterfaceModuleI2cCache(int physicalPort) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.readDeviceInterfaceModuleI2cCache(physicalPort);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "ReadDeviceInterfaceModuleI2cCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return new byte[0];
  }


  @SimpleFunction(description = "Write to the device memory map; only works in I2C write mode.")
  public void WriteDeviceInterfaceModuleI2cCache(int physicalPort, Object data) {
    if (deviceInterfaceModule != null) {
      try {
        if (data instanceof byte[]) {
          deviceInterfaceModule.writeDeviceInterfaceModuleI2cCache(physicalPort, (byte[]) data);
        } else {
          form.dispatchErrorOccurredEvent(this, "WriteDeviceInterfaceModuleI2cCache",
              ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, "data is not valid");
        }
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "WriteDeviceInterfaceModuleI2cCache",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
  }

  @SimpleFunction(description = "Determine if a physical port is ready.")
  public boolean IsPortReady(int physicalPort) {
    if (deviceInterfaceModule != null) {
      try {
        return deviceInterfaceModule.isPortReady(physicalPort);
      } catch (Throwable e) {
        e.printStackTrace();
        form.dispatchErrorOccurredEvent(this, "IsPortReady",
            ErrorMessages.ERROR_FTC_UNEXPECTED_ERROR, e.toString());
      }
    }
    return false;
  }


  // FtcHardwareDevice implementation

  @Override
  protected void initHardwareDevice() {
    HardwareMap hardwareMap = getHardwareMap();
    if (hardwareMap != null) {
      deviceInterfaceModule = hardwareMap.deviceInterfaceModule.get(getDeviceName());
      if (deviceInterfaceModule == null) {
        Log.e("FtcDeviceInterfaceModule", "Could not find a DeviceInterfaceModule named " + getDeviceName());
      }
    }
  }

  @Override
  protected void clearHardwareDevice() {
    deviceInterfaceModule = null;
  }
}
