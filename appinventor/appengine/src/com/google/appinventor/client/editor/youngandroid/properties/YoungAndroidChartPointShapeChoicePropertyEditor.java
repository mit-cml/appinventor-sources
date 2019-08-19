package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

public class YoungAndroidChartPointShapeChoicePropertyEditor extends ChoicePropertyEditor {

  // Chart Point Shape choices
  private static final Choice[] shapes = new Choice[] {
      new Choice(MESSAGES.lineChartType(), ComponentConstants.CHART_POINT_STYLE_CIRCLE + ""),
      new Choice(MESSAGES.scatterChartType(), ComponentConstants.CHART_POINT_STYLE_SQUARE + ""),
      new Choice(MESSAGES.areaChartType(), ComponentConstants.CHART_POINT_STYLE_TRIANGLE + ""),
      new Choice(MESSAGES.barChartType(), ComponentConstants.CHART_POINT_STYLE_CROSS + "")
  };

  public YoungAndroidChartPointShapeChoicePropertyEditor() {
    super(shapes);
  }
}