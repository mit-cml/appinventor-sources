// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for integer values.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class IntegerPropertyEditor extends TextPropertyEditor {

  @Override
  protected boolean validateKeyCode(char keyCode) {
    // First check whether it's a normal character, not a control character.
    if (keyCode >= ' ') {
      if (keyCode >= '0' && keyCode <= '9') {
        return true;
      }
      if (keyCode == '+' || keyCode == '-') {
        return true;
      }
      return false;
    }
    return super.validateKeyCode(keyCode);
  }

  @Override
  protected void validate(String text) throws InvalidTextException {
    super.validate(text);

    // Make sure it's an integer.
    try {
      Integer.parseInt(text);
    } catch (NumberFormatException e) {
      throw new InvalidTextException(MESSAGES.notAnInteger(text));
    }
  }
}
