// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for choosing the map tile set for the Map component.
 *
 * @author ewpatton@mit.edu (Evan Patton)
 */
public class YoungAndroidMapTypePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] mapTypes = new Choice[] {
    new Choice(MESSAGES.mapTypeRoads(), "1"),
    new Choice(MESSAGES.mapTypeAerial(), "2"),
    new Choice(MESSAGES.mapTypeTerrain(), "3"),
    new Choice(MESSAGES.mapTypeCustom(), "4")
  };

  public YoungAndroidMapTypePropertyEditor() {
    super(mapTypes);
  }

}
