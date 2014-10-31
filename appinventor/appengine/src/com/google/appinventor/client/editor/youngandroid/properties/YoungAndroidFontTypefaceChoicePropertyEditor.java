// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
