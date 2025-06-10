// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.google.appinventor.components.runtime.util.ScatterWithTrendlineRenderer;

/**
 * Class for handling the UI (view) of the Scatter Chart for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartView
 */
public class ScatterChartView extends PointChartView<
    Entry, IScatterDataSet, ScatterData, ScatterChart, ScatterChartView> {
  /**
   * Creates a new Scatter Chart View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent  Chart component to link View to
   */
  public ScatterChartView(Chart chartComponent) {
    super(chartComponent);

    chart = new ScatterChart(this.form);
    chart.setRenderer(new ScatterWithTrendlineRenderer(chart, chart.getAnimator(),
        chart.getViewPortHandler()));

    data = new ScatterData();
    chart.setData(data);

    initializeDefaultSettings();
  }

  @Override
  public ChartDataModel<Entry, IScatterDataSet, ScatterData, ScatterChart, ScatterChartView>
      createChartModel() {
    return new ScatterChartDataModel(data, this);
  }
}
