// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.ScatterData;

/**
 * Class for handling the UI (view) of the Scatter Chart for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartView
 */
public class ScatterChartView extends PointChartView<ScatterChart, ScatterData> {
  /**
   * Creates a new Scatter Chart View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent  Chart component to link View to
   */
  public ScatterChartView(Chart chartComponent) {
    super(chartComponent);

    chart = new ScatterChart(this.form);

    data = new ScatterData();
    chart.setData(data);

    initializeDefaultSettings();
  }

  @Override
  public ChartDataModel createChartModel() {
    return new ScatterChartDataModel(data, this);
  }
}
