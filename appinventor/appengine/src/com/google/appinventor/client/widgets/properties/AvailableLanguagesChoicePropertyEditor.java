// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 Google, All Rights reserved
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

/**
 * Property editor for available countries.
 *
 */
public class AvailableLanguagesChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] languages = new Choice[] {
    // to be added
  };

  public AvailableLanguagesChoicePropertyEditor() {
    super(languages);
  }
}
