// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a parameter of ListViewLayout used by the ListView component.
 */
public enum LayoutType implements OptionList<Integer> {
  @Default
  MainText(0),
  MainText_DetailText_Vertical(1),
  MainText_DetailText_Horizontal(2),
  Image_MainText(3),
  Image_MainText_DetailText_Vertical(4),
  ImageTop_MainText_DetailText(5);

  private final int layout;

  LayoutType(int value) {
    this.layout = value;
  }

  public Integer toUnderlyingValue() {
    return layout;
  }

  private static final Map<Integer, LayoutType> lookup = new HashMap<>();

  static {
    for (LayoutType value : LayoutType.values()) {
      lookup.put(value.toUnderlyingValue(), value);
    }
  }

  public static LayoutType fromUnderlyingValue(Integer value) {
    return lookup.get(value);
  }
}
