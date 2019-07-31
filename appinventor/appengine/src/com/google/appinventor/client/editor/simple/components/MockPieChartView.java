package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.PieChart;

public class MockPieChartView extends MockChartView<PieChart> {
  public MockPieChartView() {
    chartWidget = new PieChart();
    initializeDefaultSettings();
  }

  @Override
  public MockChartDataModel createDataModel() {
    return new MockPieChartDataModel(chartWidget.getData());
  }
}
