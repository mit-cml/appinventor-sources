// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

public enum BestFitModel implements OptionList<String> {
  Linear("Linear"),
  Quadratic("Quadratic"),
  // Cubic("Cubic"),
  Exponential("Exponential"),
  Logarithmic("Logarithmic");

  private static final Map<String, BestFitModel> lookup = new HashMap<>();

  static {
    for (BestFitModel model : BestFitModel.values()) {
      lookup.put(model.toUnderlyingValue(), model);
    }
  }

  private final String value;

  BestFitModel(String value) {
    this.value = value;
  }

  public String toUnderlyingValue() {
    return value;
  }

  public static BestFitModel fromUnderlyingValue(String model) {
    return lookup.get(model);
  }
}
