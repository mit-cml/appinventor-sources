// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.BarChart;
import org.pepstock.charba.client.data.BarDataset;
import org.pepstock.charba.client.data.Dataset;


/**
 * Chart View for the Bar Chart. Responsible for the GUI of the Bar Chart.
 *
 * <p>The Bar Chart has two important properties:
 * 1.The entries in each Data Series of the Bar Chart are indexed by
 * x value, meaning that the first entry has an x value of 0, the second
 * has an x value of 1, and so on. The x value essentially acts as an index.
 *
 * <p>2.Following from the 1st property, the entries are sorted by the x value (in
 * ascending order), and the difference between neighboring entries' x values are
 * at most 1.
 *
 * @see com.google.appinventor.client.editor.simple.components.MockChartView
 */
public class MockBarChartView extends MockAxisChartView<BarDataset, BarChart, MockBarChartView> {
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
    yAxis.getTicks().setSuggestedMin(0);

    // Set the custom x and y axis to the Chart
    // chartWidget.getOptions().getScales().setXAxes(xAxis);
    chartWidget.getOptions().getScales().setYAxes(yAxis);
  }

  @Override
  public MockChartDataModel<BarDataset, MockBarChartView> createDataModel() {
    return new MockBarChartDataModel(this);
  }

  /**
   * Updates the labels of the Bar Chart Data (affects X axis).
   * The labels are integer values from 0 to #dataSetCount - 1 (0, 1, ..., N - 1).
   * Since the Bar Chart's x axis is not scaled automatically upon adding entries,
   * the process has to be done manually.
   *
   * <p>To be invoked whenever a Data Series changes.
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
      // If a value for the current index is available
      // in the local Labels array, use that value instead.
      // Otherwise, use the default index value.
      if (i < this.labels.length) {
        labels[i] = this.labels[i];
      } else {
        labels[i] = i + "";
      }
    }

    // Set the constructed labels
    chartWidget.getData().setLabels(labels);
  }

  @Override
  public void updateLabels(String[] labels) {
    super.updateLabels(labels);
    updateLabels();
  }
}
