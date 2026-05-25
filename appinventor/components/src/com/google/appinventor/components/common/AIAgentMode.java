// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the AI agent permission mode for a project.
 * Set on Screen1's Form to control what the AI assistant can do.
 */
public enum AIAgentMode implements OptionList<String> {
  Off("Off"),
  Advisor("Advisor"),
  ScreenEditor("ScreenEditor"),
  ProjectEditor("ProjectEditor");

  private final String value;

  AIAgentMode(String value) {
    this.value = value;
  }

  public String toUnderlyingValue() {
    return value;
  }

  private static final Map<String, AIAgentMode> lookup = new HashMap<>();

  static {
    for (AIAgentMode mode : AIAgentMode.values()) {
      lookup.put(mode.toUnderlyingValue(), mode);
    }
  }

  public static AIAgentMode fromUnderlyingValue(String mode) {
    return lookup.get(mode);
  }
}
