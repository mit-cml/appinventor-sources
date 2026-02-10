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
 * Property editor for sensor time notification intervals
 *
 * @author abhagi@mit.edu (Anshul Bhagi)
 */
public class YoungAndroidSensorTimeIntervalChoicePropertyEditor extends ChoicePropertyEditor {
  // sensor time interval choices
  private static final Choice[] timeIntervalChoices = new Choice[] {
      new Choice("On change (0 ms)", "0"),
      new Choice("30 seconds", "30000"),
      new Choice("1 minute", "60000"),
      new Choice("5 minutes", "300000"),
      new Choice("10 minutes", "600000"),
  };

  public YoungAndroidSensorTimeIntervalChoicePropertyEditor() {
    super(timeIntervalChoices);
  }
}
