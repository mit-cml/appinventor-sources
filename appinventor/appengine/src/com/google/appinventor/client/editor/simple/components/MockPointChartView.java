package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.ScatterChart;

public abstract class MockPointChartView extends MockChartView<ScatterChart> {
  /**
   * Creates a new MockPointChartView object instance.
   */
  protected MockPointChartView() {
    /* A ScatterChart widget is used both for the
    * Line Chart based Charts as well as the Scatter Chart.
    * The reason it is used for the Line Chart is because
    * using a Scatter Chart with sorted data points and line
    * showing produces a more accurate representation of the
    * Android implemented Android Chart by automatically generating
    * the appropriate labels and scaling the Chart properly (it behaves
    * more like a Cartesian graph). Using Line Chart requires more
    * manual handling. */
    chartWidget = new ScatterChart();
    initializeDefaultSettings();
  }
}
