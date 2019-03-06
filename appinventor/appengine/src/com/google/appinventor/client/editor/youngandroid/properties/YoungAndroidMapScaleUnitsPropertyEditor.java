// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for choosing the scale units for the Map component.
 *
 * @author ewpatton@mit.edu (Evan Patton)
 */
public class YoungAndroidMapScaleUnitsPropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] mapScaleUnits = new Choice[] {
      new Choice(MESSAGES.mapScaleUnitsMetric(), "1"),
      new Choice(MESSAGES.mapScaleUnitsImperial(), "2")
  };

  public YoungAndroidMapScaleUnitsPropertyEditor() {
    super(mapScaleUnits);
  }

}
