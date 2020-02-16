// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

/**
 * Base class for handling the UI (view) of the Line-based Charts
 * for the Chart component
 * @see com.google.appinventor.components.runtime.ChartView
 */
public abstract class LineChartViewBase extends PointChartView<LineChart, LineData> {
  /**
   * Creates a new Line Chart Base View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent  Chart component to link View to
   */
  protected LineChartViewBase(Chart chartComponent) {
    super(chartComponent);

    chart = new LineChart(this.form);

    data = new LineData();
    chart.setData(data);

    initializeDefaultSettings();
  }
}
