// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a ColorSensorMode type used by the Lego Ev3 color sensor.
 */
public enum ColorSensorMode implements OptionList<String> {
  Reflected("reflected", 0),
  Ambient("ambient", 1),
  Color("color", 2);

  private final String value;
  private final int intValue;

  ColorSensorMode(String mode, int intMode) {
    this.value = mode;
    this.intValue = intMode;
  }

  public String toUnderlyingValue() {
    return value;
  }

  public Integer toInt() {
    return this.intValue;
  }

  private static final Map<String, ColorSensorMode> lookup = new HashMap<>();

  static {
    for (ColorSensorMode mode : ColorSensorMode.values()) {
      lookup.put(mode.toUnderlyingValue(), mode);
    }
  }

  public static ColorSensorMode fromUnderlyingValue(String mode) {
    return lookup.get(mode);
  }
}
