// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a UltrasonicSensorUnit type used by the Lego Ev3 ultrasonic sensor.
 */
public enum UltrasonicSensorUnit implements OptionList<String> {
  Centimeters("cm", 0),
  Inches("inch", 1);

  private String value;
  private int intValue;

  UltrasonicSensorUnit(String mode, int intMode) {
    this.value = mode;
    this.intValue = intMode;
  }

  public String toUnderlyingValue() {
    return value;
  }

  public Integer toInt() {
    return this.intValue;
  }

  private static final Map<String, UltrasonicSensorUnit> lookup = new HashMap<>();

  static {
    for (UltrasonicSensorUnit mode : UltrasonicSensorUnit.values()) {
      lookup.put(mode.toUnderlyingValue(), mode);
    }
  }

  public static UltrasonicSensorUnit fromUnderlyingValue(String mode) {
    return lookup.get(mode);
  }
}

