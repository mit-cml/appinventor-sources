// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a parameter of ListOrientation used by the ListView component.
 */
public enum ListOrientation implements OptionList<Integer> {
  @Default
  Vertical(0),
  Horizontal(1);

  private final int orientation;

  ListOrientation(int value) {
    this.orientation = value;
  }

  public Integer toUnderlyingValue() {
    return orientation;
  }

  private static final Map<Integer, ListOrientation> lookup = new HashMap<>();

  static {
    for(ListOrientation value : ListOrientation.values()) {
      lookup.put(value.toUnderlyingValue(), value);
    }
  }

  public static ListOrientation fromUnderlyingValue(Integer value) {
    return lookup.get(value);
  }

  public static ListOrientation fromUnderlyingValue(String value) {
    return fromUnderlyingValue(Integer.parseInt(value));
  }
}
