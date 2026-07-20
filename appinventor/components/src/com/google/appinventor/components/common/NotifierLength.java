// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines how long Notifier alerts should stay visible.
 */
public enum NotifierLength implements OptionList<Integer> {
  Short(0),
  @Default
  Long(1);

  private final int value;

  NotifierLength(int value) {
    this.value = value;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, NotifierLength> lookup = new HashMap<>();

  static {
    for (NotifierLength length : NotifierLength.values()) {
      lookup.put(length.toUnderlyingValue(), length);
    }
  }

  public static NotifierLength fromUnderlyingValue(Integer length) {
    return lookup.get(length);
  }
}
