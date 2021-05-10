// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a StartedStatus type used by the PhoneCall component.
 */
public enum StartedStatus implements OptionList<Integer> {
  Incoming(1),
  Outgoing(2);

  private final int value;

  StartedStatus(int status) {
    this.value = status;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, StartedStatus> lookup = new HashMap<>();

  static {
    for (StartedStatus status : StartedStatus.values()) {
      lookup.put(status.toUnderlyingValue(), status);
    }
  }

  public static StartedStatus fromUnderlyingValue(Integer status) {
    return lookup.get(status);
  }
}
