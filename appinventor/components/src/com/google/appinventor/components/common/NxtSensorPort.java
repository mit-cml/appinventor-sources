// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a NxtSensorPort type used by the NxtDirectCommands component.
 */
public enum NxtSensorPort implements OptionList<String> {
  Port1("1", 0),
  Port2("2", 1),
  Port3("3", 2),
  Port4("4", 3);

  private final String value;
  private final int intValue;

  NxtSensorPort(String port, int intPort) {
    this.value = port;
    this.intValue = intPort;
  }

  public String toUnderlyingValue() {
    return value;
  }

  public Integer toInt() {
    return intValue;
  }

  private static final Map<String, NxtSensorPort> lookup = new HashMap<>();

  static {
    for (NxtSensorPort port : NxtSensorPort.values()) {
      lookup.put(port.toUnderlyingValue(), port);
    }
  }

  public static NxtSensorPort fromUnderlyingValue(String port) {
    return lookup.get(port);
  }
}

