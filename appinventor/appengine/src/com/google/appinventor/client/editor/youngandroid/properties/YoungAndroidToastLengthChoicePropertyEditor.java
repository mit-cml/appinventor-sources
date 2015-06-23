// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for toast length choice.
 *
 */
public class YoungAndroidToastLengthChoicePropertyEditor extends ChoicePropertyEditor {

  //Toast length choices
  private static final Choice[] length = new Choice[] {
    new Choice(MESSAGES.shortToastLength(), "0"),
    new Choice(MESSAGES.longToastLength(), "1"),
  };

  public YoungAndroidToastLengthChoicePropertyEditor() {
    super(length);
  }
}
