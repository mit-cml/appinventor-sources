// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for text alignment.
 *
 */
public class YoungAndroidAlignmentChoicePropertyEditor extends ChoicePropertyEditor {

  // Text alignment choices
  private static final Choice[] textAlignments = new Choice[] {
    new Choice(MESSAGES.leftTextAlignment(), "0"),
    new Choice(MESSAGES.centerTextAlignment(), "1"),
    new Choice(MESSAGES.rightTextAlignment(), "2")
  };

  public YoungAndroidAlignmentChoicePropertyEditor() {
    super(textAlignments);
  }
}
