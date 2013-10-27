package com.google.appinventor.client.editor.youngandroid.properties;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
/**
 * Property editor for accelerometer sensitivity.
 *
 * 
 */
public class YoungAndroidAccelerometerSensitivityChoicePropertyEditor extends ChoicePropertyEditor {
  // Button shape choices
  private static final Choice[] sensitivity = new Choice[] {
    new Choice(MESSAGES.weakAccelerometerSensitivity(), "0"),    
    new Choice(MESSAGES.moderateAccelerometerSensitivity(), "1"),
    new Choice(MESSAGES.strongAccelerometerSensitivity(), "2")
   
  };
  public YoungAndroidAccelerometerSensitivityChoicePropertyEditor() {
   super(sensitivity);
  }
}