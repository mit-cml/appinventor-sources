// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor.Choice;

/**
 * Property editor for sensor distance notification intervals
 *
 * @author abhagi@mit.edu (Anshul Bhagi)
 */
public class YoungAndroidSensorDistIntervalChoicePropertyEditor extends ChoicePropertyEditor {

  // sensor distance interval choices
  private static final Choice[] distIntervalChoices = new Choice[] {
    // To avoid confusion, we only show a subset of the available sensor
    // distance interval values.
    new Choice("0", "0"),
    new Choice("1", "1"),
    new Choice("10", "10"),
    new Choice("100", "100"),
  };

  public YoungAndroidSensorDistIntervalChoicePropertyEditor() {
    super(distIntervalChoices);
  }
}
