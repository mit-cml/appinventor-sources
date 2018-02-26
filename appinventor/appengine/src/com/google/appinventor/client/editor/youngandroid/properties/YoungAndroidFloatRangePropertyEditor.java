// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.FloatPropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for validating that a given floating point value is within
 * a valid range.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class YoungAndroidFloatRangePropertyEditor extends FloatPropertyEditor {
  private final double min;
  private final double max;

  public YoungAndroidFloatRangePropertyEditor(double min, double max) {
    this.min = min;
    this.max = max;
  }

  @Override
  protected void validate(String text) throws InvalidTextException {
    super.validate(text);

    double value = Double.parseDouble(text);
    if (value < min || value > max) {
      throw new InvalidTextException(MESSAGES.valueNotInRange(text, Double.toString(min),
          Double.toString(max)));
    }
  }
}
