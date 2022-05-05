// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for data models that are used by Charts that
 * use 2-dimensional data (e.g. Line or Scatter charts).
 *
 * @param <T> Chart Dataset parameter (MPAndroidChart class for single data series)
 * @param <D> Chart Data parameter (MPAndroidChart class for collection of all data series)
 * @param <V> (Chart) View that the model is compatible with (ChartView (sub)classes)
 */
public abstract class Chart2DDataModel<
    E extends Entry,
    T extends IDataSet<E>,
    D extends ChartData<T>,
    C extends Chart<D>,
    V extends ChartView<E, T, D, C, V>>
    extends ChartDataModel<E, T, D, C, V> {
  /**
   * Initializes a new Chart2DDataModel object instance.
   *
   * @param data Chart data instance
   * @param view Chart View to link model to
   */
  protected Chart2DDataModel(D data, V view) {
    super(data, view);
  }

  @Override
  protected int getTupleSize() {
    return 2;
  }

  @Override
  public YailList getTupleFromEntry(Entry entry) {
    // Create a list with the X and Y values of the entry, and
    // convert the generic List to a YailList
    List<?> tupleEntries = Arrays.asList(entry.getX(), entry.getY());
    return YailList.makeList(tupleEntries);
  }
}
