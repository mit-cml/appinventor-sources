// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.NxtSensorPort;
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

  protected NxtSensorPort port;

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
    return port.toUnderlyingValue();
  }

  // Since different kinds of sensors need to have a different defaultValue for the SensorPort
  // property, the SensorPort property setter method must be defined in each subclass.
  public abstract void SensorPort(String sensorPortLetter);

  protected final void setSensorPort(String sensorPortLetter) {
    String functionName = "SensorPort";
    // Make sure sensorPortLetter is a valid NxtSensorPort.
    NxtSensorPort port = NxtSensorPort.fromUnderlyingValue(sensorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_PORT, sensorPortLetter);
      return;
    }

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
