// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for Chart Point Shape.
 */
public class YoungAndroidChartPointShapeChoicePropertyEditor extends ChoicePropertyEditor {

  // Chart Point Shape choices
  private static final Choice[] shapes = new Choice[] {
      new Choice(MESSAGES.chartCircleShape(), "0"),
      new Choice(MESSAGES.chartSquareShape(), "1"),
      new Choice(MESSAGES.chartTriangleShape(), "2"),
      new Choice(MESSAGES.chartCrossShape(), "3"),
      new Choice(MESSAGES.chartXShape(), "4")
  };

  public YoungAndroidChartPointShapeChoicePropertyEditor() {
    super(shapes);
  }
}
