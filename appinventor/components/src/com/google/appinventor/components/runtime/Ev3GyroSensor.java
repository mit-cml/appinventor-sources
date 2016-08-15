// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import android.os.Handler;

/**
 * A component that provides a high-level interface to a gyro sensor on a LEGO
 * MINDSTORMS EV3 robot.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
@DesignerComponent(version = YaVersion.EV3_GYROSENSOR_COMPONENT_VERSION,
                   description = "A component that provides a high-level interface to a gyro sensor on a " +
                                 "LEGO MINDSTORMS EV3 robot.",
                   category = ComponentCategory.LEGOMINDSTORMS,
                   nonVisible = true,
                   iconName = "images/legoMindstormsEv3.png")
@SimpleObject
public class Ev3GyroSensor extends LegoMindstormsEv3Sensor implements Deleteable {
  private static final int DELAY_MILLISECONDS = 50;
  private static final int SENSOR_TYPE = 32;
  private static final int SENSOR_MODE_ANGLE = 0;
  private static final int SENSOR_MODE_RATE = 1;
  private static final String SENSOR_MODE_ANGLE_STRING = "angle";
  private static final String SENSOR_MODE_RATE_STRING = "rate";
  private static final String DEFAULT_SENSOR_MODE_STRING = SENSOR_MODE_ANGLE_STRING;

  private Handler eventHandler;
  private final Runnable sensorValueChecker;
  private int mode = SENSOR_MODE_ANGLE;
  private String modeString = SENSOR_MODE_ANGLE_STRING;
  private double previousValue = -1.0;
  private boolean sensorValueChangedEventEnabled = false;

  /**
   * Creates a new Ev3GyroSensor component.
   */
  public Ev3GyroSensor(ComponentContainer container) {
    super(container, "Ev3GyroSensor");

    eventHandler = new Handler();
    sensorValueChecker = new Runnable() {
      public void run() {
        String functionName = "";

        if (bluetooth != null && bluetooth.IsConnected()) {
          double currentValue = getSensorValue(functionName);

          if (previousValue < 0.0) {
            previousValue = currentValue;
            eventHandler.postDelayed(this, DELAY_MILLISECONDS);
            return;
          }

          // trigger events according to the conditions
          if (mode == SENSOR_MODE_RATE && Math.abs(currentValue) >= 1.0)
            SensorValueChanged(currentValue);
          else if (mode == SENSOR_MODE_ANGLE && Math.abs(currentValue - previousValue) >= 1.0)
            SensorValueChanged(currentValue);

          previousValue = currentValue;
        }

        eventHandler.postDelayed(this, DELAY_MILLISECONDS);
      }
    };
    eventHandler.post(sensorValueChecker);

    Mode(DEFAULT_SENSOR_MODE_STRING);
    SensorValueChangedEventEnabled(false);
  }

  /**
   * Returns the current angle or rotation speed based on current mode,
   * or -1 if the value cannot be read from sensor.
   */
  @SimpleFunction(description = "Returns the current angle or rotation speed based on current mode, " +
                                "or -1 if the value cannot be read from sensor.")
  public double GetSensorValue() {
    String functionName = "";
    return getSensorValue(functionName);
  }

  /**
   * Specifies the mode of the sensor.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_GYRO_SENSOR_MODE,
                    defaultValue = DEFAULT_SENSOR_MODE_STRING)
  @SimpleProperty
  public void Mode(String modeName) {
    String functionName = "Mode";
    try {
      setMode(modeName);
    } catch(IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Returns the mode of the sensor.
   */
  @SimpleProperty(description = "The sensor mode can be a text constant of either \"rate\" or \"angle\", " +
                                "which correspond to SetAngleMode or SetRateMode respectively.",
                  category = PropertyCategory.BEHAVIOR)
  public String Mode() {
    return modeString;
  }

  /**
   * Make the sensor read the angle.
   */
  @SimpleFunction(description = "Measures the orientation of the sensor.")
  public void SetAngleMode() {
    setMode(SENSOR_MODE_ANGLE_STRING);
  }

  /**
   * Make the sensor read the rotation rate.
   */
  @SimpleFunction(description = "Measures the angular velocity of the sensor.")
  public void SetRateMode() {
    setMode(SENSOR_MODE_RATE_STRING);
  }

  /**
   * Returns whether the SensorValueChanged event should fire when the sensor value changed.
   */
  @SimpleProperty(description = "Whether the SensorValueChanged event should fire when the sensor value changed.",
                  category = PropertyCategory.BEHAVIOR)
  public boolean SensorValueChangedEventEnabled() {
    return sensorValueChangedEventEnabled;
  }

  /**
   * Returns whether the SensorValueChanged event should fire when the sensor value changed.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "False")
  @SimpleProperty
  public void SensorValueChangedEventEnabled(boolean enabled) {
    sensorValueChangedEventEnabled = enabled;
  }

  /**
   * Called then the sensor value changed.
   */
  @SimpleEvent(description = "Called then the sensor value changed.")
  public void SensorValueChanged(double sensorValue) {
    EventDispatcher.dispatchEvent(this, "SensorValueChanged", sensorValue);
  }

  private double getSensorValue(String functionName) {
    return readInputSI(functionName,
                       0,
                       sensorPortNumber,
                       SENSOR_TYPE,
                       mode);
  }

  private void setMode(String newModeString) {
    if (SENSOR_MODE_ANGLE_STRING.equals(newModeString))
      mode = SENSOR_MODE_ANGLE;
    else if (SENSOR_MODE_RATE_STRING.equals(newModeString))
      mode = SENSOR_MODE_RATE;
    else
      throw new IllegalArgumentException();

    this.modeString = newModeString;
  }

  // Deleteable implementation
  @Override
  public void onDelete() {
    super.onDelete();
  }
}
