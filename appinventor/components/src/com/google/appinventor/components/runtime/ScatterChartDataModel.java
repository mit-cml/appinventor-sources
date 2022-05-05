// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

import com.google.appinventor.components.common.PointStyle;

import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;

/**
 * Handles the data operations & model-specific styling for Scatter
 * Chart data for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartDataModel
 */
public class ScatterChartDataModel extends PointChartDataModel<
    Entry, IScatterDataSet, ScatterData, ScatterChart, ScatterChartView> {
  /**
   * Initializes a new ScatterChartDataModel object instance.
   *
   * @param data Chart data instance
   * @param view Scatter Chart View to link model to
   */
  public ScatterChartDataModel(ScatterData data, ScatterChartView view) {
    super(data, view);
    dataset = new ScatterDataSet(new ArrayList<Entry>(), "");
    this.data.addDataSet(dataset); // Safe add
    setDefaultStylingProperties();
  }

  @Override
  public void addEntryFromTuple(YailList tuple) {
    // Construct an entry from the prvoided tuple
    Entry entry = getEntryFromTuple(tuple);

    // If entry constructed successfully, add it to the Data Series
    if (entry != null) {
      entries.add(entry);
    }
  }

  @Override
  protected void setDefaultStylingProperties() {
    if (dataset instanceof ScatterDataSet) {
      ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.CIRCLE);
    }
  }

  /**
   * Changes the Point Shape of the Scatter Data Series.
   *
   * @param shape the desired point style
   */
  public void setPointShape(PointStyle shape) {
    if (!(dataset instanceof ScatterDataSet)) {
      return;
    }
    switch (shape) {
      case Circle:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        break;

      case Square:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.SQUARE);
        break;

      case Triangle:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.TRIANGLE);
        break;

      case Cross:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.CROSS);
        break;

      case X:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.X);
        break;

      default:
        throw new IllegalArgumentException("Unknown shape type: " + shape);
    }
  }
}
