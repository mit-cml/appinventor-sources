// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a RingerMode type used by the Sound component.
 */
public enum RingerMode implements OptionList<String> {
  Silent("Silent", 0),    // AudioManager.RINGER_MODE_SILENT
  Vibrate("Vibrate", 1),  // AudioManager.RINGER_MODE_VIBRATE
  Normal("Normal", 2);    // AudioManager.RINGER_MODE_NORMAL

  private final String value;
  private final int intValue;

  RingerMode(String value, int intValue) {
    this.value = value;
    this.intValue = intValue;
  }

  public String toUnderlyingValue() {
    return value;
  }

  public int toInt() {
    return this.intValue;
  }

  private static final Map<String, RingerMode> lookup = new HashMap<>();

  static {
    for (RingerMode value : RingerMode.values()) {
      lookup.put(value.toUnderlyingValue(), value);
    }
  }

  public static RingerMode fromUnderlyingValue(String value) {
    return lookup.get(value);
  }
}
