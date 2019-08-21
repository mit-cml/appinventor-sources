package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

public class YoungAndroidChartPointShapeChoicePropertyEditor extends ChoicePropertyEditor {

  // Chart Point Shape choices
  private static final Choice[] shapes = new Choice[]{
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