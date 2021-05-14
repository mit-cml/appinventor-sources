// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a Direction type used by the Sprite component (and subclasses).
 */
public enum Direction implements OptionList<Integer> {
  North(1),
  Northeast(2),
  East(3),
  Southeast(4),
  South(-1),
  Southwest(-2),
  West(-3),
  Northwest(-4);

  private final int value;

  Direction(int dir) {
    this.value = dir;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, Direction> lookup = new HashMap<>();

  static {
    for (Direction dir : Direction.values()) {
      lookup.put(dir.toUnderlyingValue(), dir);
    }
  }

  public static Direction fromUnderlyingValue(Integer dir) {
    return lookup.get(dir);
  }
}
