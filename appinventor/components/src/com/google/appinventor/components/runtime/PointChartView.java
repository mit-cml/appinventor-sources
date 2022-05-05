// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.Entry;

import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;

/**
 * Base class for handling the UI (view) of the Point-based Charts
 * (e.g. Line/Scatter Charts) for the Chart component
 * @see com.google.appinventor.components.runtime.ChartView
 */
public abstract class PointChartView<
    E extends Entry,
    T extends IBarLineScatterCandleBubbleDataSet<E>,
    D extends BarLineScatterCandleBubbleData<T>,
    C extends BarLineChartBase<D>,
    V extends PointChartView<E, T, D, C, V>>
    extends AxisChartView<E, T, D, C, V> {

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
    chart.setLayoutParams(new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

  @Override
  public View getView() {
    return chart;
  }
}
