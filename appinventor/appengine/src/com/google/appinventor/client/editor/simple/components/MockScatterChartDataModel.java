package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.ComponentConstants;
import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.enums.PointStyle;

import java.util.ArrayList;
import java.util.Comparator;

public class MockScatterChartDataModel extends MockPointChartDataModel {
  private PointStyle pointStyle = PointStyle.CIRCLE;

  /**
   * Creates a new Mock Scatter Chart Data Model object instance, linking it with
   * the Data object of a specific Chart.
   *
   * @param chartData Chart Data object to link to
   */
  protected MockScatterChartDataModel(Data chartData) {
    super(chartData);
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

      default:
        pointStyle = PointStyle.CIRCLE;
    }

    dataSeries.setPointStyle(pointStyle);
  }
}
