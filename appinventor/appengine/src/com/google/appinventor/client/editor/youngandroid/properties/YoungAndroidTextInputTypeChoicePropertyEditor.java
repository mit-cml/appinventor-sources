// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for text extra property.
 * 
 * @author manishkk74@gmail.com (Manish Khanchandani)
 */
public class YoungAndroidTextInputTypeChoicePropertyEditor extends ChoicePropertyEditor {

  // Button shape choices
  private static final Choice[] extras = new Choice[] {
    new Choice(MESSAGES.defaultTextInputType(), "0"),
    new Choice(MESSAGES.emailTextInputType(), "1"),
    new Choice(MESSAGES.phoneTextInputType(), "2"),
    new Choice(MESSAGES.addressTextInputType(), "3"),
    new Choice(MESSAGES.timeTextInputType(), "4"),
    new Choice(MESSAGES.dateTextInputType(), "5"),
    new Choice(MESSAGES.datetimeTextInputType(), "6"),
    new Choice(MESSAGES.textPersonNameTextInputType(), "7")
  };

  public YoungAndroidTextInputTypeChoicePropertyEditor() {
    super(extras);
  }
}
