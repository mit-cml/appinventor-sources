// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

public class YoungAndroidStrokeStylePropertyEditor extends ChoicePropertyEditor {
  private static final Choice[] strokeStyles = new Choice[] {
    new Choice(MESSAGES.strokeStyleSolid(), "1"),
    new Choice(MESSAGES.strokeStyleDashed(), "2"),
    new Choice(MESSAGES.strokeStyleDotted(), "3")
  };

  public YoungAndroidStrokeStylePropertyEditor() {
    super(strokeStyles);
  }
}
