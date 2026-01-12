// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a Sensitivity type used by the AccelerometerSensor.
 */
public enum Sensitivity implements OptionList<Integer> {
  Weak(1),
  Moderate(2),
  Strong(3);

  private final int value;

  Sensitivity(int sensitivity) {
    this.value = sensitivity;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, Sensitivity> lookup = new HashMap<>();

  static {
    for (Sensitivity sensitivity : Sensitivity.values()) {
      lookup.put(sensitivity.toUnderlyingValue(), sensitivity);
    }
  }

  public static Sensitivity fromUnderlyingValue(Integer sensitivity) {
    return lookup.get(sensitivity);
  }
}

