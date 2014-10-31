// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.util.ErrorMessages;

/**
 * A base class for components that can retrieve data from a sensor on a LEGO
 * MINDSTORMS NXT robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@SimpleObject
public abstract class LegoMindstormsNxtSensor extends LegoMindstormsNxtBase {
  static class SensorValue<T> {
    final boolean valid;
    final T value;
    SensorValue(boolean valid, T value) {
      this.valid = valid;
      this.value = value;
    }
  }

  static final int SENSOR_TYPE_NO_SENSOR = 0x00;
  static final int SENSOR_TYPE_SWITCH = 0x01;
  static final int SENSOR_TYPE_TEMPERATURE = 0x02;
  static final int SENSOR_TYPE_REFLECTION = 0x03;
  static final int SENSOR_TYPE_ANGLE = 0x04;
  static final int SENSOR_TYPE_LIGHT_ACTIVE = 0x05;
  static final int SENSOR_TYPE_LIGHT_INACTIVE = 0x06;
  static final int SENSOR_TYPE_SOUND_DB = 0x07;
  static final int SENSOR_TYPE_SOUND_DBA = 0x08;
  static final int SENSOR_TYPE_CUSTOM = 0x09;
  static final int SENSOR_TYPE_LOWSPEED = 0x0A;
  static final int SENSOR_TYPE_LOWSPEED_9V = 0x0B;

  static final int SENSOR_MODE_RAWMODE = 0x00;
  static final int SENSOR_MODE_BOOLEANMODE = 0x20;
  static final int SENSOR_MODE_TRANSITIONCNTMODE = 0x40;
  static final int SENSOR_MODE_PERIODCOUNTERMODE = 0x60;
  static final int SENSOR_MODE_PCTFULLSCALEMODE = 0x80;
  static final int SENSOR_MODE_CELSIUSMODE = 0xA0;
  static final int SENSOR_MODE_FAHRENHEITMODE = 0xC0;
  static final int SENSOR_MODE_ANGLESTEPMODE = 0xE0;
  static final int SENSOR_MODE_MASK_SLOPE = 0x1F;
  static final int SENSOR_MODE_MASK_MODE = 0xE0;

  private String sensorPortLetter; // "1" - "4"
  protected int port;

  /**
   * Creates a new LegoMindstormsNxtSensor.
   */
  protected LegoMindstormsNxtSensor(ComponentContainer container, String logTag) {
    super(container, logTag);
  }

  /**
   * Returns the sensor port that the sensor is connected to.
   */
  @SimpleProperty(description = "The sensor port that the sensor is connected to.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public String SensorPort() {
    return sensorPortLetter;
  }

  // Since different kinds of sensors need to have a different defaultValue for the SensorPort
  // property, the SensorPort property setter method must be defined in each subclass.
  public abstract void SensorPort(String sensorPortLetter);

  protected final void setSensorPort(String sensorPortLetter) {
    String functionName = "SensorPort";
    int port;
    try {
      port = convertSensorPortLetterToNumber(sensorPortLetter);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_PORT, sensorPortLetter);
      return;
    }

    this.sensorPortLetter = sensorPortLetter;
    this.port = port;
    if (bluetooth != null && bluetooth.IsConnected()) {
      initializeSensor(functionName);
    }
  }

  @Override
  public void afterConnect(BluetoothConnectionBase bluetoothConnection) {
    initializeSensor("Connect");
  }

  protected abstract void initializeSensor(String functionName);
}
