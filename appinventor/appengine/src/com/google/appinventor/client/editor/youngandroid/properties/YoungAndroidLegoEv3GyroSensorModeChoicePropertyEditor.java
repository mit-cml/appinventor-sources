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
 * @author jerry73204@gamil.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
public class YoungAndroidLegoEv3GyroSensorModeChoicePropertyEditor extends ChoicePropertyEditor {


    // Lego Mindstorms EV3 sensor port choices
  private static final Choice[] modes = new Choice[] {
          new Choice(MESSAGES.angleValues(), "angle"),
          new Choice(MESSAGES.rateValues(), "rate"),
  };

  public YoungAndroidLegoEv3GyroSensorModeChoicePropertyEditor() {
    super(modes);
  }
}
