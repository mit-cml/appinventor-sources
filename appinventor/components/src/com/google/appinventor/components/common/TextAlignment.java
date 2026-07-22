// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines text alignment choices used by text-bearing components.
 */
public enum TextAlignment implements OptionList<Integer> {
  @Default
  Left(0),
  Center(1),
  Right(2);

  private final int value;

  TextAlignment(int value) {
    this.value = value;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, TextAlignment> lookup = new HashMap<>();

  static {
    for (TextAlignment alignment : TextAlignment.values()) {
      lookup.put(alignment.toUnderlyingValue(), alignment);
    }
  }

  public static TextAlignment fromUnderlyingValue(Integer alignment) {
    return lookup.get(alignment);
  }
}
