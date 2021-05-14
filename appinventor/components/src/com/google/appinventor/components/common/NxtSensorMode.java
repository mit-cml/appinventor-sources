// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a NxtSensorMode type used by the NxtDirectCommands component.
 * 
 * <p>Info attained from the "LEGO MINDSTORMS NXT Direct Command" document.
 * http://kio4.com/b4a/programas/Appendix%202-LEGO%20MINDSTORMS%20NXT%20Direct%20commands.pdf
 * http://www.ni.com/pdf/manuals/372574c.pdf
 * In combination with the documentation here:
 * https://www.mindstorms.rwth-aachen.de/documents/downloads/doc/version-4.07/help/NXT_SetInputMode.html
 */
public enum NxtSensorMode implements OptionList<Integer> {
  Raw(0x00),
  Boolean(0x20),  // 1 if > 45% else 0.
  TransitionCount(0x60),  // Count transitions of boolean mode.
  PeriodCount(0x60),  // Count periods of boolean mode.
  Percentage(0x80),  // Range [0, 100].
  RcxCelsius(0xA0),  // Range [-200, 700]. Readings in .1 degrees Celsius.
  RcxFahrenheit(0xC0),  // Range [-400, 1580]. Readings in .1 degrees Fahrenheit.
  RcxAngleSteps(0xE0);

  private final int value;

  NxtSensorMode(int mode) {
    this.value = mode;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, NxtSensorMode> lookup = new HashMap<>();

  static {
    for (NxtSensorMode mode : NxtSensorMode.values()) {
      lookup.put(mode.toUnderlyingValue(), mode);
    }
  }

  public static NxtSensorMode fromUnderlyingValue(Integer mode) {
    return lookup.get(mode);
  }
}

