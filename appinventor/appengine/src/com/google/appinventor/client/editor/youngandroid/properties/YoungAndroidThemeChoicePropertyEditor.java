// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for text alignment.
 */
public class YoungAndroidThemeChoicePropertyEditor extends ChoicePropertyEditor {

  // Text alignment choices
  private static final Choice[] textAlignments = new Choice[] {
    new Choice(MESSAGES.classicTheme(), "Classic"),
    new Choice(MESSAGES.defaultTheme(), "AppTheme.Light.DarkActionBar"),
    new Choice(MESSAGES.blackTitleTheme(), "AppTheme.Light"),
    new Choice(MESSAGES.darkTheme(), "AppTheme")
  };

  public YoungAndroidThemeChoicePropertyEditor() {
    super(textAlignments);
  }
}
