package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.ScatterChart;

public abstract class MockPointChartView extends MockChartView<ScatterChart> {
  /**
   * Creates a new MockPointChartView object instance.
   */
  protected MockPointChartView() {
    // A ScatterChart widget is used to allow
    // arbitrary insertions of (x, y) points and
    // automatic generation of the X axis labels
    // (as well as automatic scaling)
    chartWidget = new ScatterChart();
    initializeDefaultSettings();
  }
}
