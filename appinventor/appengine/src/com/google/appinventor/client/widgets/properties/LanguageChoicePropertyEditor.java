// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for text-to-speech languages.
 *
 */
public class LanguageChoicePropertyEditor extends ChoicePropertyEditor {

  // languages supported by AppInventor's Android 2.2 emulator
  private static final Choice[] languages = new Choice[] {
    new Choice(MESSAGES.defaultText(), ""),
    new Choice("de", "de"),
    new Choice("en", "en"),
    new Choice("es", "es"),
    new Choice("fr", "fr"),
    new Choice("it", "it"),
  };

  public LanguageChoicePropertyEditor() {
    super(languages);
  }
}
