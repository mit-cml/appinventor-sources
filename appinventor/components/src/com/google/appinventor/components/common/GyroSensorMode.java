// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a GyroSensorMode type used by the Lego Ev3 gyro sensor.
 */
public enum GyroSensorMode implements OptionList<String> {
  Angle("angle", 0),
  Rate("rate", 1);

  private final String value;
  private final int intValue;

  GyroSensorMode(String mode, int intMode) {
    this.value = mode;
    this.intValue = intMode;
  }

  public String toUnderlyingValue() {
    return value;
  }

  public Integer toInt() {
    return this.intValue;
  }

  private static final Map<String, GyroSensorMode> lookup = new HashMap<>();

  static {
    for (GyroSensorMode mode : GyroSensorMode.values()) {
      lookup.put(mode.toUnderlyingValue(), mode);
    }
  }

  public static GyroSensorMode fromUnderlyingValue(String mode) {
    return lookup.get(mode);
  }
}

