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
    new Choice(MESSAGES.zeroDistanceInterval(), "0"),
    new Choice(MESSAGES.oneDistanceInterval(), "1"),
    new Choice(MESSAGES.tenDistanceInterval(), "10"),
    new Choice(MESSAGES.oneHundredDistanceInterval(), "100"),
  };

  public YoungAndroidSensorDistIntervalChoicePropertyEditor() {
    super(distIntervalChoices);
  }
}
