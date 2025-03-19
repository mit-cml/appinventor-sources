// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.LineChart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.appinventor.components.runtime.util.LineWithTrendlineRenderer;

/**
 * Base class for handling the UI (view) of the Line-based Charts
 * for the Chart component.
 *
 * @see com.google.appinventor.components.runtime.ChartView
 */
public abstract class LineChartViewBase<V extends LineChartViewBase<V>> extends PointChartView<
    Entry, ILineDataSet, LineData, LineChart, V> {
  /**
   * Creates a new Line Chart Base View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent  Chart component to link View to
   */
  protected LineChartViewBase(Chart chartComponent) {
    super(chartComponent);

    chart = new LineChart(this.form);
    chart.setRenderer(new LineWithTrendlineRenderer(chart, chart.getAnimator(),
        chart.getViewPortHandler()));

    data = new LineData();
    chart.setData(data);

    initializeDefaultSettings();
  }
}
