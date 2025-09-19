// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for choosing the AR configuration type set for the ARView3D component.
 *
 * @author niclarke@mit.edu (Nichole Clarke)
 */
public class YoungAndroidARBehaviorTypePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] trackingTypes = new Choice[] {
      new Choice(MESSAGES.arBehaviorTypeHeavy(), "1"),
      new Choice(MESSAGES.arBehaviorTypeLight(), "2"),
      new Choice(MESSAGES.arBehaviorTypeBouncy(), "3"),
      new Choice(MESSAGES.arBehaviorTypeWet(), "4"),
      new Choice(MESSAGES.arBehaviorTypeSticky(), "5"),
      new Choice(MESSAGES.arBehaviorTypeSlippery(), "6"),
      new Choice(MESSAGES.arBehaviorTypeFloating(), "7"),
      new Choice(MESSAGES.arBehaviorTypeNone(), "8")
  };

  public YoungAndroidARBehaviorTypePropertyEditor() {
    super(trackingTypes);
  }
}
