// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the DisplayMode for the Screen (Form).
 * Implements Issue #3596.
 */
public enum DisplayMode implements OptionList<String> {
  
  /**
   * Content stays within safe boundaries (respects cutouts/bars).
   */
  Safe("safe"),
  
  /**
   * Layout extends to the physical edge of the screen, hiding/overlapping bars.
   */
  EdgeToEdge("edge-to-edge"),

  /**
   * Background extends edge-to-edge, but content remains in safe area.
   */
  BackgroundEdgeToEdge("background-edge-to-edge");

  private final String value;

  DisplayMode(String value) {
    this.value = value;
  }

  @Override
  public String toUnderlyingValue() {
    return value;
  }

  private static final Map<String, DisplayMode> lookup = new HashMap<>();

  static {
    for (DisplayMode mode : DisplayMode.values()) {
      lookup.put(mode.toUnderlyingValue(), mode);
    }
  }

  public static DisplayMode fromUnderlyingValue(String value) {
    return lookup.get(value);
  }
}