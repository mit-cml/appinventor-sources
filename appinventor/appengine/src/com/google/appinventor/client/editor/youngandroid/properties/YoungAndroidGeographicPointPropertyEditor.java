// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.TextPropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for the CenterFromString property that validates format and range of
 * latitude and longitude.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class YoungAndroidGeographicPointPropertyEditor extends TextPropertyEditor {

  @Override
  protected boolean validateKeyCode(char keyCode) {
    if (keyCode >= ' ') {
      return keyCode >= '0' && keyCode <= '9' || keyCode == '+' || keyCode == '-' ||
          keyCode == '.' || keyCode == ' ' || keyCode == ',';
    }
    return super.validateKeyCode(keyCode);
  }

  @Override
  protected void validate(String text) throws InvalidTextException {
    super.validate(text);

    String[] parts = text.split(",");
    if (parts.length == 2) {
      validateDouble(parts[0].trim(), -90.0, 90.0);
      validateDouble(parts[1].trim(), -180.0, 180.0);
    } else {
      throw new InvalidTextException(MESSAGES.expectedLatLongPair(MESSAGES.CenterFromStringProperties()));
    }
  }

  private void validateDouble(String text, double min, double max) throws InvalidTextException {
    try {
      double value = Double.parseDouble(text);
      if (value < min || value > max) {
        throw new InvalidTextException(MESSAGES.valueNotInRange(text, Double.toString(min), Double.toString(max)));
      }
    } catch(NumberFormatException e) {
      throw new InvalidTextException(MESSAGES.notAFloat(text));
    }
  }
}
