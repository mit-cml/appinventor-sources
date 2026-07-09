// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a LayoutDimension type used by padding and margin helper blocks.
 */
public enum BoxSide implements OptionList<String> {
  All("all"),
  Top("top"),
  Bottom("bottom"),
  Left("left"),
  Right("right"),
  Leading("leading"),
  Trailing("trailing");

  private final String value;

  BoxSide(String value) {
    this.value = value;
  }

  public String toUnderlyingValue() {
    return value;
  }

  private static final Map<String, BoxSide> lookup = new HashMap<>();

  static {
    for (BoxSide dim : BoxSide.values()) {
      lookup.put(dim.toUnderlyingValue(), dim);
    }
  }

  public static BoxSide fromUnderlyingValue(String dim) {
    return lookup.get(dim);
  }
}