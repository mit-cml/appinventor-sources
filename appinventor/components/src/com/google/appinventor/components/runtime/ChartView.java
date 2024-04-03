// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import android.view.View;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;

import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class to represent Chart Views. The class (and subclasses)
 * are used to handle the UI part of the Chart itself, and provide
 * some functionality to generate new data series. Generally, the
 * class should not handle data operations directly, and is created
 * to abstract the view from the data.
 * The subclasses represent each concrete chart (e.g. Line or Bar Views)
 *
 * @param <E> MPAndroidChart class for Entry type.
 * @param <T> MPAndroidChart class for DataSet collection.
 * @param <D> MPAndroidChart class for ChartData series collection.
 * @param <C> MPAndroidChart class for Chart view.
 * @param <V> Type of the view for reflective operations
 */
public abstract class ChartView<
    E extends Entry,
    T extends IDataSet<E>,
    D extends ChartData<T>,
    C extends Chart<D>,
    V extends ChartView<E, T, D, C, V>> {
  // Keep track of the parent Chart component to be able to report
  // detailed errors & warnings.
  protected com.google.appinventor.components.runtime.Chart chartComponent;
  protected Form form;

  protected C chart;
  protected D data;

  protected Handler uiHandler = new Handler(Looper.myLooper());

  /**
   * Creates a new Chart View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent  Chart component to link View to
   */
  protected ChartView(com.google.appinventor.components.runtime.Chart chartComponent) {
    this.chartComponent = chartComponent;
    this.form = chartComponent.$form();
  }

  public Form getForm() {
    return this.form;
  }

  /**
   * Returns the underlying view holding all the necessary Chart Views.
   * The reason this does not return the Chart view straight away is
   * due to some Charts having more than one view (e.g. Pie Chart
   * with rings)
   *
   * @return Chart view
   */
  public abstract View getView();

  /**
   * Sets the background color of the Chart.
   *
   * @param argb background color
   */
  public void setBackgroundColor(int argb) {
    chart.setBackgroundColor(argb);
  }

  /**
   * Sets the description text of the Chart.
   *
   * @param text description text
   */
  public void setDescription(String text) {
    chart.getDescription().setText(text);
  }

  /**
   * Enables or disables the Legend.
   *
   * @param enabled Specifies whether the Legend should be enabled.
   */
  public void setLegendEnabled(boolean enabled) {
    chart.getLegend().setEnabled(enabled);
  }

  /**
   * Creates a new Chart Model object instance.
   *
   * @return Chart Model instance
   */
  public abstract ChartDataModel<E, T, D, C, V> createChartModel();

  /**
   * Sets the necessary default settings for the Chart view.
   */
  protected void initializeDefaultSettings() {
    // Center the Legend
    chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    chart.getLegend().setWordWrapEnabled(true); // Wrap Legend entries in case of many entries
  }

  /**
   * Refreshes the Chart View to react to styling changes.
   */
  public void refresh() {
    chart.notifyDataSetChanged();
    chart.invalidate();
  }

  /**
   * Updates the specified Chart Data Model and refreshes the
   * Chart.
   *
   * @param model Chart Data Model to update & refresh
   */
  public void refresh(final ChartDataModel<E, T, D, C, V> model) {
    // Create a new RefreshTask with the model's current List of Entries
    RefreshTask refreshTask = new RefreshTask(model.getEntries());

    // Execute the RefreshTask with the ChartDataModel argument
    refreshTask.execute(model);
  }

  /**
   * AsyncTask used to refresh the Chart View with new data on the UI thread.
   * Used as a measure to prevent crashes and exceptions by taking in a constant
   * copy of the data, and re-setting it to the currently refreshed Chart Data
   * Model, while also updating the Chart itself and invalidating the View.
   */
  @SuppressLint("StaticFieldLeak")
  private class RefreshTask
      extends AsyncTask<ChartDataModel<E, T, D, C, V>, Void, ChartDataModel<E, T, D, C, V>> {

    // Local copy of latest Chart Entries
    private final List<E> entries;

    public RefreshTask(List<E> entries) {
      // Create a copy of the passed in Entries List.
      this.entries = new ArrayList<>(entries);
    }

    @SafeVarargs
    @Override
    protected final ChartDataModel<E, T, D, C, V> doInBackground(
        ChartDataModel<E, T, D, C, V>... chartDataModels) {
      // All the work should be done on the UI thread; Simply pass the first
      // passed in Chart Data Model (expect non-null, non-empty var args)
      return chartDataModels[0];
    }

    @Override
    protected void onPostExecute(ChartDataModel<E, T, D, C, V> result) {
      // Refresh the Chart and the Data Model with the
      // local Entries List copy. This is done on the UI
      // thread to avoid exceptions (onPostExecute runs
      // on the UI)
      refresh(result, entries);
    }
  }

  /**
   * Sets the specified List of Entries to the specified Chart Data
   * Model and refreshes the local Chart View.
   *
   * <p>To be used after updating a ChartDataModel's entries to display
   * the changes on the Chart itself.
   *
   * <p>Values are overwritten with the specified List of entries.
   *
   * @param model   Chart Data Model to update
   * @param entries List of entries to set to the Chart Data Model
   */
  @SuppressWarnings("unchecked")
  protected void refresh(ChartDataModel<E, T, D, C, V> model, List<E> entries) {
    // Set the specified Entries to the Data Set. This is used to
    // prevent exceptions on quick data changing operations (so that
    // the invalidation/refreshing can keep up and inconsistent states
    // would not be caused by asynchronous operations)
    T dataset = model.getDataset();
    if (dataset instanceof DataSet) {
      ((DataSet<E>) dataset).setValues(entries);
    }

    // Notify the Data component of data changes (needs to be called
    // when Datasets get changed directly)
    chart.getData().notifyDataChanged();

    // Notify the Chart of Data changes (needs to be called
    // when Data objects get changed directly)
    chart.notifyDataSetChanged();

    // Invalidate the Chart view for the changes to take
    // effect.
    chart.invalidate();
  }
}
