// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a TransportMethod type used by the Navigation component.
 */
public enum TransportMethod implements OptionList<String> {
  Foot("foot-walking"),
  Car("driving-car"),
  Bicycle("cycling-regular"),
  Wheelchair("wheelchair");

  private final String value;

  TransportMethod(String value) {
    this.value = value;
  }

  public String toUnderlyingValue() {
    return value;
  }

  private static final Map<String, TransportMethod> lookup = new HashMap<>();

  static {
    for (TransportMethod method : TransportMethod.values()) {
      lookup.put(method.toUnderlyingValue(), method);
    }
  }

  public static TransportMethod fromUnderlyingValue(String method) {
    return lookup.get(method);
  }
}
