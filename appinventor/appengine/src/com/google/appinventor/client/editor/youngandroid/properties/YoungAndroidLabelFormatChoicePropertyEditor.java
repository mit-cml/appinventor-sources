// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for text extra property.
 * 
 * @author manishkk74@gmail.com (Manish Khanchandani)
 */
public class YoungAndroidLabelFormatChoicePropertyEditor extends ChoicePropertyEditor {

  // Label Format choices
  private static final Choice[] extras = new Choice[] {
    new Choice(MESSAGES.labelTextFormat(), "0"),
    new Choice(MESSAGES.labelHTMLFormat(), "1")
  };

  public YoungAndroidLabelFormatChoicePropertyEditor() {
    super(extras);
  }
}