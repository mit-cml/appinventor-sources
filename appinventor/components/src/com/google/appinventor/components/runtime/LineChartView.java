// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.LineChart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;

import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

/**
 * Class for handling the UI (view) of the Line Chart for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartView
 */
public class LineChartView extends LineChartViewBase<LineChartView> {
  /**
   * Creates a new Line Chart View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent  Chart component to link View to
   */
  public LineChartView(Chart chartComponent) {
    super(chartComponent);
  }

  @Override
  public ChartDataModel<Entry, ILineDataSet, LineData, LineChart, LineChartView>
      createChartModel() {
    return new LineChartDataModel(data, this);
  }
}
