// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.IntegerPropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for integer values in a given range.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class YoungAndroidIntegerRangePropertyEditor extends IntegerPropertyEditor {

  private int min;
  private int max;

  public YoungAndroidIntegerRangePropertyEditor(int min, int max) {
    this.min = min;
    this.max = max;
  }

  @Override
  protected void validate(String text) throws InvalidTextException {
    super.validate(text);

    int value = Integer.parseInt(text);
    if (value < min || value > max) {
      throw new InvalidTextException(MESSAGES.valueNotInRange(text, Integer.toString(min), Integer.toString(max)));
    }
  }
}
