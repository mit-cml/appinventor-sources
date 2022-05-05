// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import java.util.List;

import org.pepstock.charba.client.ScatterChart;
import org.pepstock.charba.client.callbacks.TickCallback;
import org.pepstock.charba.client.configuration.Axis;
import org.pepstock.charba.client.data.ScatterDataset;

/**
 * Chart View for the Point-Based Charts for the Mock Chart component.
 * Handles the UI of Point-based Charts (e.g. Line/Scatter)
 */
public abstract class MockPointChartView<V extends MockPointChartView<V>>
    extends MockAxisChartView<ScatterDataset, ScatterChart, V> {
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

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // Set the custom x and y axis to the Chart
    chartWidget.getOptions().getScales().setXAxes(xAxis);
    chartWidget.getOptions().getScales().setYAxes(yAxis);

    xAxis.getTicks().setCallback(new TickCallback() {
      @Override
      public String onCallback(Axis axis, double value, int index, List<Double> values) {
        // Round the double value to an integer to get the index representation of the value
        // The reason we do not use the index parameter is due to the fact that the
        // Chart X Axis is indexed with negative numbers. For example, if the minimum
        // value is -5, then the index of 0 would apply to the -5 value. Due to
        // limited support of this in the MPAndroidChart library, we use our own
        // system of 0-value based indexing.
        // int indexValue = (int) Math.round(value);

        // Check if the index value is within the bound of the labels array.
        // If that is the case, then use the value from the labels array.
        if (index >= 0 && index < labels.length) {
          return labels[index];
        } else {
          // Otherwise, use the value itself.
          return value + "";
        }
      }
    });
  }
}
