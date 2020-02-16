// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.ComponentConstants;
import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.enums.PointStyle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Data Model class for the Scatter Chart.
 * Provides full functionality for data operations and some styling settings
 * for Scatter points.
 */
public class MockScatterChartDataModel extends MockPointChartDataModel<MockScatterChartView> {
  private PointStyle pointStyle = PointStyle.CIRCLE;

  /**
   * Creates a new Mock Scatter Chart Data Model object instance, linking it with
   * the specified Scatter Chart View.
   *
   * @param view Chart View to link to
   */
  protected MockScatterChartDataModel(MockScatterChartView view) {
    super(view);
  }

  @Override
  protected void setDefaultStylingProperties() {
    dataSeries.setShowLine(false);
    dataSeries.setBorderWidth(1);
  }

  @Override
  protected void postDataImportAction() {
    // No data points generated, fallback to default option.
    if (dataSeries.getDataPoints().isEmpty()) {
      setDefaultElements();
    }
  }

  /**
   * Changes the Point Shape of the Data Series.
   * <p>
   * The following values are used:
   * 0 - Circle
   * 1 - Square
   * 2 - Triangle
   * 3 - Cross
   * 4 - X
   *
   * @param shape new Point Shape value (integer)
   */
  @Override
  public void changePointShape(int shape) {
    switch (shape) {
      case ComponentConstants.CHART_POINT_STYLE_CIRCLE:
        pointStyle = PointStyle.CIRCLE;
        break;

      case ComponentConstants.CHART_POINT_STYLE_SQUARE:
        pointStyle = PointStyle.RECT;
        break;

      case ComponentConstants.CHART_POINT_STYLE_TRIANGLE:
        pointStyle = PointStyle.TRIANGLE;
        break;

      case ComponentConstants.CHART_POINT_STYLE_CROSS:
        pointStyle = PointStyle.CROSS;
        break;

      case ComponentConstants.CHART_POINT_STYLE_X:
        pointStyle = PointStyle.CROSS_ROT;
        break;
    }

    dataSeries.setPointStyle(pointStyle);
  }
}
