// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * Class for handling the UI (view) of the Line Chart for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartView
 */
public class LineChartView extends LineChartViewBase {
  /**
   * Instantiate a new LineChartView in the given context.
   *
   * @param context Context to instantiate view in
   */
  public LineChartView(Form context) {
    super(context);
  }

  @Override
  public ChartDataModel createChartModel() {
    return new LineChartDataModel(data);
  }
}
