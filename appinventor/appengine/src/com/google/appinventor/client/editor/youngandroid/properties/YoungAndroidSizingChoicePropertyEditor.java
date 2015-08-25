// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for Sizing property of Screen1
 */

public class YoungAndroidSizingChoicePropertyEditor extends ChoicePropertyEditor {

  // Accelerometer sensitivity choices
  private static final Choice[] sizing = new Choice[] {
    new Choice(MESSAGES.fixedSizing(), "Fixed"),
    new Choice(MESSAGES.responsiveSizing(), "Responsive")
  };

  public YoungAndroidSizingChoicePropertyEditor() {
   super(sizing);
  }
}
