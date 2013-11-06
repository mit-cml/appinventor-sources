// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor.Choice;

/**
 * Property editor for sensor time notification intervals
 *
 * @author abhagi@mit.edu (Anshul Bhagi)
 */
public class YoungAndroidSensorTimeIntervalChoicePropertyEditor extends ChoicePropertyEditor{
  // sensor time interval choices
  private static final Choice[] timeIntervalChoices = new Choice[] {
    // To avoid confusion, we only show a subset of the available 
    // sensor time interval values.
    new Choice("0", "0"),
    new Choice("1000", "1000"),
    new Choice("10000", "10000"),
    new Choice("60000", "60000"),
    new Choice("300000", "300000"),
  };

  public YoungAndroidSensorTimeIntervalChoicePropertyEditor() {
    super(timeIntervalChoices);
  }
}
