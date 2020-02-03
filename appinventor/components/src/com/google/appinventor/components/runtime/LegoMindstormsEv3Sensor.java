// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.Ev3Constants;
import com.google.appinventor.components.runtime.util.Ev3BinaryParser;

/**
 * The base class for EV3 sensors.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
@SimpleObject
public class LegoMindstormsEv3Sensor extends LegoMindstormsEv3Base {
  protected static final String DEFAULT_SENSOR_PORT = "1";
  protected int sensorPortNumber;

  protected LegoMindstormsEv3Sensor(ComponentContainer container, String logTag) {
    super(container, logTag);
    SensorPort(DEFAULT_SENSOR_PORT);
  }

  @SimpleProperty(description = "The sensor port that the sensor is connected to.",
                  category = PropertyCategory.BEHAVIOR,
                  userVisible = false)
  public String SensorPort() {
    return portNumberToSensorPortLetter(sensorPortNumber);
  }

  /**
   * Specifies the sensor port that the sensor is connected to.
   * **Must be set in the Designer.**
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_SENSOR_PORT,
                    defaultValue = DEFAULT_SENSOR_PORT)
  @SimpleProperty
  public void SensorPort(String sensorPortLetter) {
    String functionName = "SensorPort";
    setSensorPort(functionName, sensorPortLetter);
  }

  protected final void setSensorPort(String functionName, String sensorPortLetter) {
    try {
      sensorPortNumber = sensorPortLetterToPortNumber(sensorPortLetter);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_SENSOR_PORT, sensorPortLetter);
      return;
    }
  }

  protected final int readInputPercentage(String functionName,
                                          int layer,
                                          int no,
                                          int type,
                                          int mode) {
    if (layer < 0 || layer > 3 || no < 0 || no > 3 || mode < -1 || mode > 7)
      throw new IllegalArgumentException();

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.INPUT_DEVICE,
                                                         true,
                                                         1,
                                                         0,
                                                         "ccccccg",
                                                         Ev3Constants.InputDeviceSubcode.READY_PCT,
                                                         (byte) layer,
                                                         (byte) no,
                                                         (byte) type,
                                                         (byte) mode,
                                                         (byte) 1,
                                                         (byte) 0);

    byte[] reply = sendCommand(functionName, command, true);
    if (reply != null && reply.length == 2 && reply[0] == Ev3Constants.DirectReplyType.DIRECT_REPLY) {
      return (int) reply[1];
    } else {                    // error
      return -1;
    }
  }

  protected final double readInputSI(String functionName,
                                     int layer,
                                     int no,
                                     int type,
                                     int mode) {
    if (layer < 0 || layer > 3 || no < 0 || no > 3 || mode < -1 || mode > 7)
      throw new IllegalArgumentException();
    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.INPUT_DEVICE,
                                                         true,
                                                         4,
                                                         0,
                                                         "ccccccg",
                                                         Ev3Constants.InputDeviceSubcode.READY_SI,
                                                         (byte) layer,
                                                         (byte) no,
                                                         (byte) type,
                                                         (byte) mode,
                                                         (byte) 1,
                                                         (byte) 0); //index always be 0

    byte[] reply = sendCommand(functionName, command, true);

    if (reply != null && reply.length == 5 && reply[0] == Ev3Constants.DirectReplyType.DIRECT_REPLY) {
      Object[] values = Ev3BinaryParser.unpack("xf", reply);
      return (double) ((Float) values[0]);

    } else {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_INVALID_REPLY);
      return -1.0;
    }
  }
}
