// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a NxtRunState type used by the NxtDirectCommands component.
 * 
 * <p>Info attained from:
 * http://kio4.com/b4a/programas/Appendix%202-LEGO%20MINDSTORMS%20NXT%20Direct%20commands.pdf
 * http://www.ni.com/pdf/manuals/372574c.pdf
 * In combination with the documentation here:
 * https://www.mindstorms.rwth-aachen.de/documents/downloads/doc/version-4.07/help/NXT_SetInputMode.html
 */
public enum NxtRunState implements OptionList<Integer> {
  Disabled(0x00),
  @Default
  Running(0x20),
  RampUp(0x10),
  RampDown(0x40);

  private final int value;

  NxtRunState(int state) {
    this.value = state;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  private static final Map<Integer, NxtRunState> lookup = new HashMap<>();

  static {
    for (NxtRunState state : NxtRunState.values()) {
      lookup.put(state.toUnderlyingValue(), state);
    }
  }

  public static NxtRunState fromUnderlyingValue(Integer state) {
    return lookup.get(state);
  }
}
