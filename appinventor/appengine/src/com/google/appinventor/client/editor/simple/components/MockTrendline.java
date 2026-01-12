// -*- mode: java -*-; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.gwt.user.client.ui.Image;

public class MockTrendline extends MockComponent implements MockChart.MockChartClient {
  public static final String TYPE = "Trendline";

  private static final String PROPERTY_NAME_CHART_DATA = "ChartData";
  private static final String PROPERTY_NAME_COLOR = "Color";
  private static final String PROPERTY_NAME_EXTEND = "Extend";
  private static final String PROPERTY_NAME_MODEL = "Model";
  private static final String PROPERTY_NAME_STROKE_WIDTH = "StrokeWidth";
  private static final String PROPERTY_NAME_STROKE_STYLE = "StrokeStyle";

  protected MockChart chart;
  protected MockChartDataModel<?, ?> chartDataModel;
  protected MockChartData2D chartData2D;
  private final Image iconWidget;
  private boolean extend = true;
  private String method = "Linear";

  /**
   * Creates a new MockTrendline component.
   *
   * @param editor the editor that contains the Trendline
   */
  public MockTrendline(SimpleEditor editor) {
    super(editor, TYPE, new Image(images.trendline()));

    iconWidget = new Image(images.trendline());
    iconWidget.setHeight("60");
    iconWidget.setWidth("60");

    initComponent(iconWidget);
  }

  @Override
  public boolean isVisibleComponent() {
    return true;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (chartDataModel == null) {
      return;
    }

    if (propertyName.equals(PROPERTY_NAME_CHART_DATA)) {
      chartData2D = (MockChartData2D) editor.getComponents().get(propertyName);
      refreshChart();
    } else if (propertyName.equals(PROPERTY_NAME_COLOR)) {
      chartDataModel.changeColor(newValue);
      refreshChart();
    } else if (propertyName.equals(PROPERTY_NAME_EXTEND)) {
      extend = "True".equals(newValue);
      refreshChart();
    } else if (propertyName.equals(PROPERTY_NAME_MODEL)) {
      method = newValue;
      refreshChart();
    } else if (propertyName.equals(PROPERTY_NAME_STROKE_WIDTH)) {
//      chartDataModel.setStrokeWidth(getDouble(propertyName));
      refreshChart();
    } else if (propertyName.equals(PROPERTY_NAME_STROKE_STYLE)) {
//      chartDataModel.setStrokeStyle(getStrokeStyle());
      refreshChart();
    }
  }

  @Override
  int getWidthHint() {
    return 0;
  }

  @Override
  int getHeightHint() {
    return 0;
  }

  public void addToChart(MockChart chart) {
    iconWidget.setVisible(false);
    iconWidget.setHeight("0");
    iconWidget.setWidth("0");

    this.chart = chart;
    this.chartDataModel = chart.createDataModel();

    setDataSeriesProperties();
    changeStylingPropertiesVisibility();

    refreshChart();
  }

  protected void setDataSeriesProperties() {
    for (EditableProperty property : properties) {
      onPropertyChange(property.getName(), property.getValue());
    }
  }

  private void changeStylingPropertiesVisibility() {

  }

  protected void refreshChart() {
    chart.refreshChart();
  }
}
