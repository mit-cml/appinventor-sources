// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines built-in font typeface choices used by text-bearing components.
 */
public enum FontTypeface implements OptionList<String> {
  @Default
  Default("0"),
  SansSerif("1"),
  Serif("2"),
  Monospace("3");

  private final String value;

  FontTypeface(String value) {
    this.value = value;
  }

  public String toUnderlyingValue() {
    return value;
  }

  private static final Map<String, FontTypeface> lookup = new HashMap<>();

  static {
    for (FontTypeface typeface : FontTypeface.values()) {
      lookup.put(typeface.toUnderlyingValue(), typeface);
    }
  }

  public static FontTypeface fromUnderlyingValue(String typeface) {
    return lookup.get(typeface);
  }
}
