// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

public enum StrokeStyle implements OptionList<Integer> {
  Solid(1),
  Dashed(2),
  Dotted(3);

  private static final Map<Integer, StrokeStyle> lookup = new HashMap<>();

  static {
    for (StrokeStyle style : StrokeStyle.values()) {
      lookup.put(style.toUnderlyingValue(), style);
    }
  }

  private final int value;

  StrokeStyle(int value) {
    this.value = value;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  public static StrokeStyle fromUnderlyingValue(Integer style) {
    return lookup.get(style);
  }

  public static StrokeStyle fromUnderlyingValue(String style) {
    return lookup.get(Integer.parseInt(style));
  }
}
