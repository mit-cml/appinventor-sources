// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Property editor for Chart Point Shape.
 */
public class YoungAndroidChartPointShapeChoicePropertyEditor extends ChoicePropertyEditor {

  // Chart Point Shape choices
  private static final Choice[] shapes = new Choice[] {
      new Choice(MESSAGES.chartCircleShape(), ComponentConstants.CHART_POINT_STYLE_CIRCLE + ""),
      new Choice(MESSAGES.chartSquareShape(), ComponentConstants.CHART_POINT_STYLE_SQUARE + ""),
      new Choice(MESSAGES.chartTriangleShape(), ComponentConstants.CHART_POINT_STYLE_TRIANGLE + ""),
      new Choice(MESSAGES.chartCrossShape(), ComponentConstants.CHART_POINT_STYLE_CROSS + ""),
      new Choice(MESSAGES.chartXShape(), ComponentConstants.CHART_POINT_STYLE_X + "")
  };

  public YoungAndroidChartPointShapeChoicePropertyEditor() {
    super(shapes);
  }
}
