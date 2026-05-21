// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a VerticalAlignment used by a variety of classes including Form, HorizontalArrangement,
 * and Marker.
 */
public enum VerticalAlignment implements OptionList<Integer> {
  Top(1),
  Center(2),
  Bottom(3);

  private final int value;

  VerticalAlignment(int value) {
    this.value = value;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, VerticalAlignment> lookup = new HashMap<>();

  static {
    for (VerticalAlignment alignment : VerticalAlignment.values()) {
      lookup.put(alignment.toUnderlyingValue(), alignment);
    }
  }

  public static VerticalAlignment fromUnderlyingValue(Integer alignment) {
    return lookup.get(alignment);
  }
}

