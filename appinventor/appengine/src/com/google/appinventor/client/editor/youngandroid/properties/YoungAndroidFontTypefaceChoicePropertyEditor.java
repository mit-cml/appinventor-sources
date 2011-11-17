// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for font typeface of text.
 *
 */
public class YoungAndroidFontTypefaceChoicePropertyEditor extends ChoicePropertyEditor {

  // Font typeface choices
  private static final Choice[] fontTypefaces = new Choice[] {
    new Choice(MESSAGES.defaultFontTypeface(), "0"),
    new Choice(MESSAGES.sansSerifFontTypeface() , "1"),
    new Choice(MESSAGES.serifFontTypeface(), "2"),
    new Choice(MESSAGES.monospaceFontTypeface(), "3")
  };

  public YoungAndroidFontTypefaceChoicePropertyEditor() {
    super(fontTypefaces);
  }
}
