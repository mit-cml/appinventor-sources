// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

public enum ChartType implements OptionList<Integer> {
  Line(0),
  Scatter(1),
  Area(2),
  Bar(3),
  Pie(4);

  private static final Map<Integer, ChartType> LOOKUP = new HashMap<>();

  static {
    for (ChartType type : ChartType.values()) {
      LOOKUP.put(type.toUnderlyingValue(), type);
    }
  }

  private final int value;

  ChartType(int value) {
    this.value = value;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  public static ChartType fromUnderlyingValue(Integer type) {
    return LOOKUP.get(type);
  }

  public static ChartType fromUnderlyingValue(String type) {
    return fromUnderlyingValue(Integer.parseInt(type));
  }
}
