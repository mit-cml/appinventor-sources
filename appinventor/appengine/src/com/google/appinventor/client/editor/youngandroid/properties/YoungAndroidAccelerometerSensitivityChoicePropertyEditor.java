// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for accelerometer sensitivity.
 *
 *
 */
public class YoungAndroidAccelerometerSensitivityChoicePropertyEditor extends ChoicePropertyEditor {

  // Accelerometer sensitivity choices
  private static final Choice[] sensitivity = new Choice[] {
    new Choice(MESSAGES.weakAccelerometerSensitivity(), "1"),
    new Choice(MESSAGES.moderateAccelerometerSensitivity(), "2"),
    new Choice(MESSAGES.strongAccelerometerSensitivity(), "3")
  };

  public YoungAndroidAccelerometerSensitivityChoicePropertyEditor() {
   super(sensitivity);
  }
}
