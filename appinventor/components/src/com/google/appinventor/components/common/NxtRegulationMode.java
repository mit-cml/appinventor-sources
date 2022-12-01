// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a NxtRegulationMode type used by the NxtDirectCommands component.
 * 
 * <p>Info attained from:
 * http://kio4.com/b4a/programas/Appendix%202-LEGO%20MINDSTORMS%20NXT%20Direct%20commands.pdf
 * http://www.ni.com/pdf/manuals/372574c.pdf
 * In combination with the documentation here:
 * https://www.mindstorms.rwth-aachen.de/documents/downloads/doc/version-4.07/help/NXT_SetInputMode.html
 * 
 * <p>Note that this property is only important if the motor mode is set to regulated.
 */
public enum NxtRegulationMode implements OptionList<Integer> {
  Disabled(0x00),  // No regulation.
  @Default
  Speed(0x01),  // Speed regulation.
  Synchronization(0x02);  // Enables motor synchronization.

  private final int value;

  NxtRegulationMode(int mode) {
    this.value = mode;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, NxtRegulationMode> lookup = new HashMap<>();

  static {
    for (NxtRegulationMode mode : NxtRegulationMode.values()) {
      lookup.put(mode.toUnderlyingValue(), mode);
    }
  }

  public static NxtRegulationMode fromUnderlyingValue(Integer mode) {
    return lookup.get(mode);
  }
}
