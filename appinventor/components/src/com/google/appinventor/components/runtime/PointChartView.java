// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;

/**
 * Base class for handling the UI (view) of the Point-based Charts
 * (e.g. Line/Scatter Charts) for the Chart component
 * @see com.google.appinventor.components.runtime.ChartView
 */
public abstract class PointChartView<T extends BarLineChartBase,
    D extends BarLineScatterCandleBubbleData>
    extends AxisChartView<T, D> {

  /**
   * Creates a new Point Chart View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent Chart component to link View to
   */
  protected PointChartView(Chart chartComponent) {
    super(chartComponent);
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // Since the Chart is stored in a RelativeLayout, settings are
    // needed to fill the Layout.
    chart.setLayoutParams(new ViewGroup.LayoutParams
        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

  @Override
  public View getView() {
    return chart;
  }
}
