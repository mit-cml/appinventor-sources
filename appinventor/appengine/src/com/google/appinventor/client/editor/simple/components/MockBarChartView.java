package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.BarChart;

public class MockBarChartView extends MockChartView<BarChart> {
  public MockBarChartView() {
    chartWidget = new BarChart();
    initializeDefaultSettings();
  }

  @Override
  public MockChartDataModel createDataModel() {
    return new MockBarChartDataModel(chartWidget.getData());
  }
}
