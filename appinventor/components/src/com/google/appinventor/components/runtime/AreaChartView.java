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
 * Class for handling the UI (view) of the Area Chart for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartView
 */
public class AreaChartView extends LineChartViewBase<AreaChartView> {
  /**
   * Creates a new Area Chart View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent  Chart component to link View to
   */
  public AreaChartView(Chart chartComponent) {
    super(chartComponent);

    // In order for the fill under the Chart to work on SDK < 18,
    // hardware acceleration has to be disabled.
    chart.setHardwareAccelerationEnabled(false);
  }

  @Override
  public ChartDataModel<Entry, ILineDataSet, LineData, LineChart, AreaChartView>
      createChartModel() {
    return new AreaChartDataModel(data, this);
  }
}
