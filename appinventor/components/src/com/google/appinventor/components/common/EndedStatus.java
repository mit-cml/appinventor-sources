// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a EndedStatus type used by the PhoneCall component.
 */
public enum EndedStatus implements OptionList<Integer> {
  IncomingRejected(1),
  IncomingEnded(2),
  OutgoingEnded(3);

  private final int value;

  EndedStatus(int status) {
    this.value = status;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, EndedStatus> lookup = new HashMap<>();

  static {
    for (EndedStatus status : EndedStatus.values()) {
      lookup.put(status.toUnderlyingValue(), status);
    }
  }

  public static EndedStatus fromUnderlyingValue(Integer status) {
    return lookup.get(status);
  }
}
