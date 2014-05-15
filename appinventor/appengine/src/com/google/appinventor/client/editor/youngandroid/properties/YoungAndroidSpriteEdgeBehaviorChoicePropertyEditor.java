package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for sprite bounding choice.
 */
public class YoungAndroidSpriteEdgeBehaviorChoicePropertyEditor extends ChoicePropertyEditor {

  // Sprite bounding choices
  private static final Choice[] choices = new Choice[] {
    new Choice(MESSAGES.spriteEdgeBehaviorBounded(), "0"),
    new Choice(MESSAGES.spriteEdgeBehaviorUnbounded(), "1")
  };

  public YoungAndroidSpriteEdgeBehaviorChoicePropertyEditor() {
    super(choices);
  }
}
