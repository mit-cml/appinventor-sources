// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the shapes supported by button-like components.
 */
public enum ButtonShape implements OptionList<Integer> {
  @Default
  Default(0),
  Rounded(1),
  Rectangular(2),
  Oval(3);

  private final int value;

  ButtonShape(int value) {
    this.value = value;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, ButtonShape> lookup = new HashMap<>();

  static {
    for (ButtonShape shape : ButtonShape.values()) {
      lookup.put(shape.toUnderlyingValue(), shape);
    }
  }

  public static ButtonShape fromUnderlyingValue(Integer shape) {
    return lookup.get(shape);
  }
}
