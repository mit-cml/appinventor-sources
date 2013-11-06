// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for screen orientation
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidScreenOrientationChoicePropertyEditor extends ChoicePropertyEditor {

  // Screen orientation choices
  private static final Choice[] screenOrientationChoices = new Choice[] {
    // To avoid confusion, we only show a subset of the available screen orientation values.
    new Choice(MESSAGES.unspecifiedScreenOrientation(), "unspecified"),
    new Choice(MESSAGES.portraitScreenOrientation(), "portrait"),
    new Choice(MESSAGES.landscapeScreenOrientation(), "landscape"),
    new Choice(MESSAGES.sensorScreenOrientation(), "sensor"),
    new Choice(MESSAGES.userScreenOrientation(), "user"),
  };

  public YoungAndroidScreenOrientationChoicePropertyEditor() {
    super(screenOrientationChoices);
  }
}
