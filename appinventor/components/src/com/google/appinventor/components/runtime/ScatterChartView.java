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
   * Instantiate a new ScatterChartView in the given context.
   *
   * @param context Context to instantiate view in
   */
  public ScatterChartView(Form context) {
    chart = new ScatterChart(context);

    data = new ScatterData();
    chart.setData(data);

    initializeDefaultSettings();
  }

  @Override
  public ChartDataModel createChartModel() {
    return new ScatterChartDataModel(data);
  }
}
