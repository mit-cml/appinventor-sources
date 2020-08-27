package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

public class YoungAndroidFileAccessModePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] CHOICES = new Choice[] {
      new Choice(MESSAGES.fileModeDefault(), "1"),
      new Choice(MESSAGES.fileModeLegacy(), "2"),
      new Choice(MESSAGES.fileModePrivate(), "3")
  };

  public YoungAndroidFileAccessModePropertyEditor() {
    super(CHOICES);
  }
}
