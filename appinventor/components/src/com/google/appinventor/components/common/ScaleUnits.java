// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a ScaleUnits type used by the Map component.
 */
public enum ScaleUnits implements OptionList<Integer> {
  Metric(1),
  Imperial(2);

  private final Integer value;

  ScaleUnits(Integer value) {
    this.value = value;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, ScaleUnits> lookup = new HashMap<>();

  static {
    for (ScaleUnits unit : ScaleUnits.values()) {
      lookup.put(unit.toUnderlyingValue(), unit);
    }
  }

  public static ScaleUnits fromUnderlyingValue(Integer unit) {
    return lookup.get(unit);
  }
}
