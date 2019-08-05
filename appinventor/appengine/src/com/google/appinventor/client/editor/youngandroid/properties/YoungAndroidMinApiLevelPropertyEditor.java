package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for api level type
 *
 */
public class YoungAndroidMinApiLevelPropertyEditor extends ChoicePropertyEditor {


  private static final Choice[] apiOptions = new Choice[] {
      new Choice("API 14", "14"),
      new Choice("API 15", "15"),
      new Choice("API 16", "16"),
      new Choice("API 17", "17"),
      new Choice("API 18", "18"),
      new Choice("API 19", "19"),
      new Choice("API 20", "20"),
      new Choice("API 21", "21")
  };


  public YoungAndroidMinApiLevelPropertyEditor() {
    super(apiOptions);
  }
}
