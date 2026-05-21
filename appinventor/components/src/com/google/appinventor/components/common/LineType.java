// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

public enum LineType implements OptionList<Integer> {
  Linear(0),
  Curved(1),
  Stepped(2);

  private static final Map<Integer, LineType> LOOKUP = new HashMap<>();

  static {
    for (LineType type : values()) {
      LOOKUP.put(type.toUnderlyingValue(), type);
    }
  }

  private final int value;

  LineType(int value) {
    this.value = value;
  }

  @Override
  public Integer toUnderlyingValue() {
    return value;
  }

  public static LineType fromUnderlyingValue(Integer value) {
    return LOOKUP.get(value);
  }

  public static LineType fromUnderlyingValue(String value) {
    return fromUnderlyingValue(Integer.parseInt(value));
  }
}
