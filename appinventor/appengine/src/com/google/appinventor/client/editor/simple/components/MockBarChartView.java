package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.BarChart;
import org.pepstock.charba.client.configuration.CartesianLinearAxis;
import org.pepstock.charba.client.data.Dataset;

public class MockBarChartView extends MockChartView<BarChart> {
  /**
   * Creates a new MockBarChartView object instance.
   */
  public MockBarChartView() {
    chartWidget = new BarChart();
    initializeDefaultSettings();
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // In order to not have the y values cut off (the y axis being started
    // at the minimum value), set the suggested minimum to be 0 (on negative
    // values, the minimum becomes lower)
    CartesianLinearAxis axis = new CartesianLinearAxis(chartWidget);
    axis.getTicks().setSuggestedMin(0);
    chartWidget.getOptions().getScales().setYAxes(axis);
  }

  @Override
  public MockChartDataModel createDataModel() {
    return new MockBarChartDataModel(this);
  }

  /**
   * Updates the labels of the Bar Chart Data (affects X axis).
   * The labels are integer values from 0 to #dataSetCount - 1 (0, 1, ..., N - 1).
   * Since the Bar Chart's x axis is not scaled automatically upon adding entries,
   * the process has to be done manually.
   *
   * To be invoked whenever a Data Series changes.
   */
  public void updateLabels() {
    int labelCount = 0;

    // Iterate through all Data Series to find the maximum size of the
    // Data Sets. Here we rely on the property that the maximum x value
    // is equal to size - 1.
    for (Dataset dataset : chartWidget.getData().getDatasets()) {
      labelCount = Math.max(dataset.getData().size(), labelCount);
    }

    // Construct a new array of Strings, where entries are
    // integers from 0 to labelCount - 1 (0, 1, ..., labelCount - 1)
    // Here we label each tick of the X axis, since it is not done
    // automatically upon adding entries.
    String[] labels = new String[labelCount];

    for (int i = 0; i < labelCount; ++i) {
      labels[i] = i + "";
    }

    // Set the constructed labels
    chartWidget.getData().setLabels(labels);
  }
}
