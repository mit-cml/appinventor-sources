// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;

/**
 * Class for handling the UI (view) of the Area Chart for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartView
 */
public class AreaChartView extends LineChartViewBase {
  /**
   * Creates a new Area Chart View instance with the given context
   *
   * @param context  Context to create view in
   */
  public AreaChartView(Activity context) {
    super(context);

    // In order for the fill under the Chart to work on SDK < 18,
    // hardware acceleration has to be disabled.
    chart.setHardwareAccelerationEnabled(false);
  }

  @Override
  public ChartDataModel createChartModel() {
    return new AreaChartDataModel(data);
  }
}
