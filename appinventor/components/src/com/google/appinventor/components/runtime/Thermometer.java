// -*- mode: java; c-basic-offset: 2; -*-
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
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Physical world component that can measure the ambient air temperature if
 * supported by the hardware.
 */
@DesignerComponent(version = 1,
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
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that a thermometer is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Available() {
    return isAvailable();
  }

  /**
   * If true, the sensor will generate events.  Otherwise, no events
   * are generated.
   *
   * @return {@code true} indicates that the sensor generates events,
   *         {@code false} that it doesn't
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return enabled;
  }

  /**
   * Specifies whether the sensor should generate events.  If true,
   * the sensor will generate events.  Otherwise, no events are
   * generated.
   *
   * @param enabled  {@code true} enables sensor event generation,
   *                 {@code false} disables it
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    setEnabled(enabled);
  }

  /**
   * Returns the temperature in degrees Celsius by averaging the
   * previous 10 measured values. The sensor must be enabled and available 
   * to return meaningful values.
   *
   * @return the averaged temperature in degrees Celsius
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
   public float Temperature() {
    return getAverageValue();
  }
}
