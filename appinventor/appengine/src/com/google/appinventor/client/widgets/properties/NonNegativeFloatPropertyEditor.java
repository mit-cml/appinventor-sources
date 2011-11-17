// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.client.widgets.properties;

/**
 * Property editor for non-negative float values.
 *
 */
public class NonNegativeFloatPropertyEditor extends FloatPropertyEditor {
  @Override
  protected boolean validateKeyCode(char keyCode) {
    // Do not allow negative signs.
    if (keyCode == '-') {
      return false;
    }
    return super.validateKeyCode(keyCode);
  }

  // We don't need to override any other methods, since there is
  // no way to enter a negative value without a minus sign.
}
