// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
