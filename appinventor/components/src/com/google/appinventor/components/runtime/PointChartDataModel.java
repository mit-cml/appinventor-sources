// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;

/**
 * Handles the data operations & model-specific styling for point-based
 * Chart data (e.g. Scatter or Line data) for the Chart component.
 *
 * @param <T>  Point Chart Data Set object to be specialized by subclasses
 *             (MPAndroidChart BarLineScatterCandleBubbleDataSet)
 * @param <D>  Point Chart Data object to be specialized by subclasses
 *             (MPAndroidChart BarLineScatterCandleBubbleData)
 * @param <V>  Point Chart View type to be specialized by subclasses.
 * @see com.google.appinventor.components.runtime.ChartDataModel
 */
public abstract class PointChartDataModel<
    E extends Entry,
    T extends IBarLineScatterCandleBubbleDataSet<E>,
    D extends BarLineScatterCandleBubbleData<T>,
    C extends BarLineChartBase<D>,
    V extends PointChartView<E, T, D, C, V>>
    extends Chart2DDataModel<E, T, D, C, V> {
  /**
   * Initializes a new PointChartDataModel object instance.
   *
   * @param data Chart data instance
   * @param view Chart View to link model to
   */
  protected PointChartDataModel(D data, V view) {
    super(data, view);
  }

  @Override
  public Entry getEntryFromTuple(YailList tuple) {
    try {
      // Tuple is expected to have at least 2 entries.
      // The first entry is assumed to be the x value, and
      // the second is assumed to be the y value.
      String xValue = tuple.getString(0);
      String yValue = tuple.getString(1);

      try {
        // Attempt to parse the x and y value String representations
        float x = Float.parseFloat(xValue);
        float y = Float.parseFloat(yValue);

        return new Entry(x, y);
      } catch (NumberFormatException e) {
        // Nothing happens: Do not add entry on NumberFormatException
        this.view.getForm().dispatchErrorOccurredEvent(this.view.chartComponent,
            "GetEntryFromTuple",
            ErrorMessages.ERROR_INVALID_CHART_ENTRY_VALUES,
            xValue, yValue);
      }
    } catch (NullPointerException e) {
      this.view.getForm().dispatchErrorOccurredEvent(this.view.chartComponent,
          "GetEntryFromTuple",
          ErrorMessages.ERROR_NULL_CHART_ENTRY_VALUES);
    } catch (IndexOutOfBoundsException e) {
      this.view.getForm().dispatchErrorOccurredEvent(this.view.chartComponent,
          "GetEntryFromTuple",
          ErrorMessages.ERROR_INSUFFICIENT_CHART_ENTRY_VALUES,
          getTupleSize(), tuple.size());
    }

    return null;
  }
}
