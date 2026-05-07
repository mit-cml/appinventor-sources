// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for Chart Line type.
 */
public class YoungAndroidChartLineTypeChoicePropertyEditor extends ChoicePropertyEditor {

  // Chart Point Shape choices
  private static final Choice[] types = new Choice[] {
      new Choice(MESSAGES.lineTypeLinear(), "0"),
      new Choice(MESSAGES.lineTypeCurved(), "1"),
      new Choice(MESSAGES.lineTypeStepped(), "2")
  };

  public YoungAndroidChartLineTypeChoicePropertyEditor() {
    super(types);
  }
}
