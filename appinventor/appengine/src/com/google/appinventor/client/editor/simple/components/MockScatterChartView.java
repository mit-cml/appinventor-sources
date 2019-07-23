package com.google.appinventor.client.editor.simple.components;

public class MockScatterChartView extends MockPointChartView {
  /**
   * Creates a new Mock Scatter Chart View object instance
   */
  public MockScatterChartView() {
    super();
  }

  @Override
  public MockChartDataModel createDataModel() {
    return new MockScatterChartDataModel(chartWidget.getData());
  }
}
