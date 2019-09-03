// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0


package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.hardware.Sensor;

/**
 * Physical world component that can measure the relative ambient air 
 * humidity if supported by the hardware.
 */
@DesignerComponent(version = YaVersion.HYGROMETER_COMPONENT_VERSION,
    description = "Non-visible component that can measure the ambient humidity.",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/hygrometer.png")
@SimpleObject
public class Hygrometer extends SingleValueSensor {
  /**
   * Creates a new Hygrometer component.
   *
   * @param container ignored (because this is a non-visible component)
   */
  public Hygrometer(ComponentContainer container) {
    super(container.$form(), Sensor.TYPE_RELATIVE_HUMIDITY);
  }

  @Override
  protected void onValueChanged(float value) {
    HumidityChanged(value);
  }
  
  /**
   * Indicates the relative humidity changed.
   *
   * @pram the new relative humidity
   */
  @SimpleEvent
  public void HumidityChanged(float humidity) {
    EventDispatcher.dispatchEvent(this, "HumidityChanged", humidity);
  }

  /**
   * Returns the relative ambient humidity as a percentage.
   * The sensor must be enabled and available 
   * to return meaningful values.
   *
   * @return the relative ambient humidity as a percentage
   */
  @SimpleProperty
   public float Humidity() {
      return getValue();
  }
}
