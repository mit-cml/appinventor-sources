// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a NxtMotorMode type used by the NxtDirectCommands component.
 *
 * <p>Info attained from:
 * http://kio4.com/b4a/programas/Appendix%202-LEGO%20MINDSTORMS%20NXT%20Direct%20commands.pdf
 * http://www.ni.com/pdf/manuals/372574c.pdf
 * In combination with the documentation here:
 * https://www.mindstorms.rwth-aachen.de/documents/downloads/doc/version-4.07/help/NXT_SetInputMode.html
 */
public enum NxtMotorMode implements OptionList<Integer> {
  On(0x01),
  Brake(0x02),  // Allows for electronic breaking, which improves accuracy of motor output.
  @Default
  Regulated(0x04),  // Allows regulation based on the regulationMode property.
  Coast(0x00);  // Motors will rotate freely.

  private final int value;

  NxtMotorMode(int mode) {
    this.value = mode;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, NxtMotorMode> lookup = new HashMap<>();

  static {
    for (NxtMotorMode mode : NxtMotorMode.values()) {
      lookup.put(mode.toUnderlyingValue(), mode);
    }
  }

  public static NxtMotorMode fromUnderlyingValue(Integer mode) {
    return lookup.get(mode);
  }
}
