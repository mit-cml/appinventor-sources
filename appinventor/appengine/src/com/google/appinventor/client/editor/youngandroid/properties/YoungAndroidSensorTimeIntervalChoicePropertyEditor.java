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
      new Choice(MESSAGES.timeIntervalOnChange(), "0"),
      new Choice(MESSAGES.timeInterval30Seconds(), "30000"),
      new Choice(MESSAGES.timeInterval1Minute(), "60000"),
      new Choice(MESSAGES.timeInterval5Minutes(), "300000"),
      new Choice(MESSAGES.timeInterval10Minutes(), "600000"),
  };

  public YoungAndroidSensorTimeIntervalChoicePropertyEditor() {
    super(timeIntervalChoices);
  }
}
