// Copyright 2008 Google Inc. All Rights Reserved.

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
