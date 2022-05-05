// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineData;

/**
 * Handles the data operations & model-specific styling for Line
 * Chart data for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartDataModel
 */
public class LineChartDataModel extends LineChartBaseDataModel<LineChartView> {
  /**
   * Initializes a new LineChartDataModel object instance.
   *
   * @param data Line Chart Data object instance
   * @param view Line Chart View to link model to
   */
  public LineChartDataModel(LineData data, LineChartView view) {
    super(data, view);
  }
}
