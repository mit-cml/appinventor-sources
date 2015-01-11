// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for choosing a port number for an FtcServo component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidFtcServoPortNumberChoicePropertyEditor extends ChoicePropertyEditor {

  // FTC port number choices
  private static final Choice[] portNumbers = new Choice[] {
    new Choice("1", "1"),
    new Choice("2", "2"),
    new Choice("3", "3"),
    new Choice("4", "4"),
    new Choice("5", "5"),
    new Choice("6", "6"),
  };

  public YoungAndroidFtcServoPortNumberChoicePropertyEditor() {
    super(portNumbers);
  }
}
