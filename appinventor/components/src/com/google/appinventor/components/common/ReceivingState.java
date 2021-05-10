// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a ReceivingState type used by the Texting component.
 */
public enum ReceivingState implements OptionList<Integer> {
  Off(1),
  Foreground(2),
  Always(3);

  private final int value;

  ReceivingState(int status) {
    this.value = status;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, ReceivingState> lookup = new HashMap<>();

  static {
    for (ReceivingState status : ReceivingState.values()) {
      lookup.put(status.toUnderlyingValue(), status);
    }
  }

  public static ReceivingState fromUnderlyingValue(Integer status) {
    return lookup.get(status);
  }
}
