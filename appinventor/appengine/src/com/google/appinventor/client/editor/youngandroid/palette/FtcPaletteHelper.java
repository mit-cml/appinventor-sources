// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.palette;

import java.util.ArrayList;
import java.util.List;

/**
 * The palette helper for the FIRST Tech Challenge component category.
 */
class FtcPaletteHelper extends OrderedPaletteHelper {
  private static final List<String> ftcComponentNames = new ArrayList<String>();
  static {
    // First, robot controller.
    ftcComponentNames.add("FtcRobotController");
    // Then, op modes.
    ftcComponentNames.add("FtcOpMode");
    ftcComponentNames.add("FtcLinearOpMode");
    // Then, gamepad.
    ftcComponentNames.add("FtcGamepad");
    // Then, motors.
    ftcComponentNames.add("FtcDcMotor");
    ftcComponentNames.add("FtcDcMotorController");
    ftcComponentNames.add("FtcServo");
    ftcComponentNames.add("FtcServoController");
    // Then, sensors.
    ftcComponentNames.add("FtcAccelerationSensor");
    ftcComponentNames.add("FtcColorSensor");
    ftcComponentNames.add("FtcCompassSensor");
    ftcComponentNames.add("FtcGyroSensor");
    ftcComponentNames.add("FtcIrSeekerSensor");
    ftcComponentNames.add("FtcLED");
    ftcComponentNames.add("FtcLightSensor");
    ftcComponentNames.add("FtcOpticalDistanceSensor");
    ftcComponentNames.add("FtcTouchSensor");
    ftcComponentNames.add("FtcTouchSensorMultiplexer");
    ftcComponentNames.add("FtcUltrasonicSensor");
    ftcComponentNames.add("FtcVoltageSensor");
    // Then, low level components.
    ftcComponentNames.add("FtcAnalogInput");
    ftcComponentNames.add("FtcAnalogOutput");
    ftcComponentNames.add("FtcDigitalChannel");
    ftcComponentNames.add("FtcI2cDeviceReader");
    ftcComponentNames.add("FtcI2cDevice");
    ftcComponentNames.add("FtcPwmOutput");
    ftcComponentNames.add("FtcDeviceInterfaceModule");
    ftcComponentNames.add("FtcLegacyModule");
    ftcComponentNames.add("FtcElapsedTime");
  }

  FtcPaletteHelper() {
    super(ftcComponentNames);
  }
}
