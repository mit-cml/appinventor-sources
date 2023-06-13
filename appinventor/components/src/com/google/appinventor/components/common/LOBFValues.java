package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

public enum LOBFValues implements OptionList<String> {
  CorrCoef("correlation coefficient"),
  @Default
  Slope("slope"),
  Yintercept("Yintercept"),
  Predictions("predictions");

  private String lobfValues;

  LOBFValues(String lobfV) {
    this.lobfValues = lobfV;
  }

  public String toUnderlyingValue() {
    return lobfValues;
  }

  private static final Map<String, LOBFValues> lookup = new HashMap<>();

  static {
    for(LOBFValues value : LOBFValues.values()) {
      lookup.put(value.toUnderlyingValue(), value);
    }
  }

  public static LOBFValues fromUnderlyingValue(String value) {
    return lookup.get(value);
  }
}
