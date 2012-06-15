// Copyright 2012 MIT. All Rights Reserved.
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
    new Choice(MESSAGES.zeroTimeInterval(), "0"),
    new Choice(MESSAGES.oneThousandTimeInterval(), "1000"),
    new Choice(MESSAGES.tenThousandTimeInterval(), "10000"),
    new Choice(MESSAGES.sixtyThousandTimeInterval(), "60000"),
    new Choice(MESSAGES.threeHundredThousandTimeInterval(), "300000"),
  };

  public YoungAndroidSensorTimeIntervalChoicePropertyEditor() {
    super(timeIntervalChoices);
  }
}
