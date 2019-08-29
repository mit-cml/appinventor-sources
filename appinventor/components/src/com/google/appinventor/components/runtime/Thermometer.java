// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.hardware.Sensor;

/**
 * Physical world component that can measure the ambient air temperature if
 * supported by the hardware.
 */
@DesignerComponent(version = YaVersion.THERMOMETER_COMPONENT_VERSION,
                   description = "Non-visible component that can measure the ambient temperature.",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/thermometer.png")
@SimpleObject
public class Thermometer extends SingleValueSensor {
  /**
   * Creates a new Thermometer component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public Thermometer(ComponentContainer container) {
    super(container.$form(), Sensor.TYPE_AMBIENT_TEMPERATURE);
  }

  @Override
  protected void onValueChanged(float value) {
    TemperatureChanged(value);
  }
  
  /**
   * Indicates the temperature changed.
   */
  @SimpleEvent
  public void TemperatureChanged(float temperature) {
    EventDispatcher.dispatchEvent(this, "TemperatureChanged", temperature);
  }

  /**
   * Returns the temperature in degrees Celsius.
   * The sensor must be enabled and available 
   * to return meaningful values.
   *
   * @return the temperature in degrees Celsius
   */
  @SimpleProperty
   public float Temperature() {
    return getValue();
  }
}
