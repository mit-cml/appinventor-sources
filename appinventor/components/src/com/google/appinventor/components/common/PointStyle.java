// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

public enum PointStyle implements OptionList<Integer> {
  Circle(0),
  Square(1),
  Triangle(2),
  Cross(3),
  X(4);

  private static final Map<Integer, PointStyle> LOOKUP = new HashMap<>();

  static {
    for (PointStyle style : values()) {
      LOOKUP.put(style.toUnderlyingValue(), style);
    }
  }

  private final int value;

  PointStyle(int value) {
    this.value = value;
  }

  @Override
  public Integer toUnderlyingValue() {
    return value;
  }

  public static PointStyle fromUnderlyingValue(Integer value) {
    return LOOKUP.get(value);
  }

  public static PointStyle fromUnderlyingValue(String value) {
    return fromUnderlyingValue(Integer.parseInt(value));
  }
}
