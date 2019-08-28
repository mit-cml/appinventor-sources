// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.hardware.Sensor;

/**
 * Physical world component that can measure the ambient air pressure if
 * supported by the hardware.
 */
@DesignerComponent(version = YaVersion.BAROMETER_COMPONENT_VERSION,
                   description = "Non-visible component that can measure the ambient air pressure.",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/barometer.png")
@SimpleObject
public class Barometer extends SingleValueSensor {
  /**
   * Creates a new Barometer component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public Barometer(ComponentContainer container) {
    super(container.$form(), Sensor.TYPE_PRESSURE);
  }

  @Override
  protected void onValueChanged(float value) {
    AirPressureChanged(value);
  }
  
  /**
   * Indicates the air pressure changed.
   */
  @SimpleEvent
  public void AirPressureChanged(float pressure) {
    EventDispatcher.dispatchEvent(this, "AirPressureChanged", pressure);
  }

  /**
   * Returns the atmospheridc pressure in hPa (millibar).
   * The sensor must be enabled and available 
   * to return meaningful values.
   *
   * @return the atmospheric pressure in hPa (millibar)
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
   public float AirPressure() {
      return getValue();
  }
}
