// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a ScreenOrientation type used by the Form component to specify orientation.
 */
public enum ScreenOrientation implements OptionList<String> {
  Unspecified("unspecified", 4),  // Should be -1, but we match sensor.
  Landscape("landscape", 0),
  Portrait("portrait", 1),
  Sensor("sensor", 4),
  User("user", 2),
  Behind("behind", 3),
  NoSensor("nosensor", 5),
  FullSensor("fullSensor", 10),
  ReverseLandscape("reverseLandscape", 8),
  ReversePortrait("reversePortrait", 9),
  SensorLandscape("sensorLandscape", 6),
  SensorPortrait("sensorPortrait", 7);

  private String value;
  private int orientationConst;

  ScreenOrientation(String val, int orientation) {
    this.value = val;
    this.orientationConst = orientation;
  }

  public String toUnderlyingValue() {
    return value;
  }

  public int getOrientationConstant() {
    return orientationConst;
  }

  private static final Map<String, ScreenOrientation> lookup = new HashMap<>();

  static {
    for (ScreenOrientation orientation : ScreenOrientation.values()) {
      lookup.put(orientation.toUnderlyingValue().toLowerCase(), orientation);
    }
  }

  public static ScreenOrientation fromUnderlyingValue(String orientation) {
    return lookup.get(orientation.toLowerCase());
  }
}
