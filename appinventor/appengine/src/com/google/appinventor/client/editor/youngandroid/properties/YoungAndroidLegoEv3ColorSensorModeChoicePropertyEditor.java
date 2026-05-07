// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for choosing an sensor port for a sensor on a Lego Mindstorms
 * EV3 robot.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
public class YoungAndroidLegoEv3ColorSensorModeChoicePropertyEditor extends ChoicePropertyEditor {

  // Lego Mindstorms EV3 sensor port choices
  private static final Choice[] modes = new Choice[] {
    new Choice(MESSAGES.reflectedValues(), "reflected"),
    new Choice(MESSAGES.ambientValues(), "ambient"),
    new Choice(MESSAGES.colorValues(), "color")
  };

  public YoungAndroidLegoEv3ColorSensorModeChoicePropertyEditor() {
    super(modes);
  }
}
