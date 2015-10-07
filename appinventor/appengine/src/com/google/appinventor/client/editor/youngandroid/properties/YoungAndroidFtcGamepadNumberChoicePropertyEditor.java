// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for choosing a gamepad number for an FtcGamepad component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidFtcGamepadNumberChoicePropertyEditor extends ChoicePropertyEditor {

  // FTC gamepad number choices
  private static final Choice[] gamepadNumbers = new Choice[] {
    new Choice("1", "1"),
    new Choice("2", "2"),
  };

  public YoungAndroidFtcGamepadNumberChoicePropertyEditor() {
    super(gamepadNumbers);
  }
}
