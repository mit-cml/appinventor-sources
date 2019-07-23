package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;

import java.util.ArrayList;
import java.util.Comparator;

public class MockScatterChartDataModel extends MockPointChartDataModel {
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
  public void setElements(String elements) {
    super.setElements(elements);

    // No data points generated, fallback to default option.
    if (dataSeries.getDataPoints().isEmpty()) {
      setDefaultElements();
    }
  }
}
