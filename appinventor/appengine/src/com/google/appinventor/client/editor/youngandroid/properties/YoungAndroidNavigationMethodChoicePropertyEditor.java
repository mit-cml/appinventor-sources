// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for navigation method choice.
 */
public class YoungAndroidNavigationMethodChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] methods = new Choice[] {
    new Choice(MESSAGES.Walking(), MESSAGES.Walking()),
    new Choice(MESSAGES.Driving(), MESSAGES.Driving()),
    new Choice(MESSAGES.Cycling(), MESSAGES.Cycling()),
    new Choice (MESSAGES.Wheelchair(), MESSAGES.Wheelchair())
  };

  public YoungAndroidNavigationMethodChoicePropertyEditor() {
    super(methods);
  }
}
