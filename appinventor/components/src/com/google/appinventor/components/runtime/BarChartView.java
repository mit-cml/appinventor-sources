// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.List;

/**
 * Class for handling the UI (view) of the Bar Chart for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartView
 */
public class BarChartView extends AxisChartView<
    BarEntry, IBarDataSet, BarData, BarChart, BarChartView> {
  // Constant for the starting value to group Bar Chart bars from
  private static final float START_X_VALUE = 0f;

  // Constant for the spacing to apply to groups of bars
  private static final float GROUP_SPACE = 0.08f;

  // Local variables to keep track of the current Bar Space and Width.
  private float barSpace = 0f;
  private float barWidth = 0.3f;


  /**
   * Creates a new Bar Chart View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent  Chart component to link View to
   */
  public BarChartView(Chart chartComponent) {
    super(chartComponent);

    // Set the Chart object to a new Bar Chart instance
    chart = new BarChart(this.form);

    // Instantiate new Data object and set it to the Bar Chart
    data = new BarData();
    chart.setData(data);

    initializeDefaultSettings();
  }

  @Override
  public View getView() {
    return chart;
  }

  @Override
  public ChartDataModel<BarEntry, IBarDataSet, BarData, BarChart, BarChartView> createChartModel() {
    // Instantiate a new Bar Chart Data Model with the local
    // BarData object.
    BarChartDataModel model = new BarChartDataModel(data, this);

    // Since the creation a new Bar Chart Data model implies
    // an addition of an additional Data Series, the Bar Space
    // and Width variables have to be recalculated.
    recalculateBarSpaceAndWidth();

    // Return the instantiated model
    return model;
  }

  /**
   * Recalculates the Bar Space and Width of the Bar Chart based on
   * the current number of Data Series attached to the Bar Chart.
   * In the case of only a single Data Series being attached to
   * the Bar Chart, no recalculations are performed.
   */
  private void recalculateBarSpaceAndWidth() {
    // Get the current number of Data Sets
    int dataSetCount = chart.getData().getDataSetCount();

    // If there is more than a single Data Set, the Bar Space and Width
    // have to be recalculated.
    if (dataSetCount > 1) {
      // (BarWidth + BarSpace) * #datasets + groupSpace should equal 1
      // to fit into the x axis grid of the Chart (of granularity 1).
      // The 1f here represents the fixed granularity of the X axis.
      // Since the number of data sets can change, the bar space and
      // bar width should be re-calculated. 10% and 90% of the remainder
      // are given to the Bar Space and the Bar Width, respectively.
      float x = (1f - GROUP_SPACE) / dataSetCount;
      barSpace = x * 0.1f;
      barWidth = x * 0.9f;

      // Update the bar width of the Bar Chart
      chart.getData().setBarWidth(barWidth);
    }

    // If the Data Set count on Bar Space and Width recalculation is
    // 2, that means the 2nd Data Set has been added, and as such,
    // the appropriate settings can be set which apply to Grouped Bar Charts.
    if (dataSetCount == 2) {
      // Axis labels should be centered to align with the grid lines
      chart.getXAxis().setCenterAxisLabels(true);
    }
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // Since the Chart is stored in a RelativeLayout, settings are
    // needed to fill the Layout.
    chart.setLayoutParams(new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    // Granularity should be 1 (bars go from x values of 0, 1, ..., N)
    chart.getXAxis().setGranularity(1f);
  }

  @Override
  protected void refresh(
      ChartDataModel<BarEntry, IBarDataSet, BarData, BarChart, BarChartView> model,
      List<BarEntry> entries) {
    // Update the ChartDataModel's entries
    IBarDataSet dataset = model.getDataset();
    if (dataset instanceof BarDataSet) {
      ((BarDataSet) dataset).setValues(entries);
    }

    // Regroup bars (if appropriate)
    regroupBars();

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

  /**
   * Regroups the Bars of the Bar Chart, while also re-calculating the
   * maximum X axis value to fit all the bars. If there is only a
   * single Data Series in the Bar Chart, no action is taken.
   */
  private void regroupBars() {
    int dataSetCount = chart.getData().getDataSetCount();

    // If there is more than one Data Set in the Bar Chart, the
    // bars have to be regrouped.
    if (dataSetCount > 1) {
      // Group Bar Chart bars with the current parameters
      chart.groupBars(START_X_VALUE, GROUP_SPACE, barSpace);

      // Determine the maximum number of entries between Bar Data Sets.
      // This value is needed to apply the maximum value of the axis.
      int maxEntries = 0;

      for (IBarDataSet dataSet : chart.getData().getDataSets()) {
        maxEntries = Math.max(maxEntries, dataSet.getEntryCount());
      }

      // Update the x axis to start from the start x value
      chart.getXAxis().setAxisMinimum(START_X_VALUE);

      // Set the maximum value for the x axis based on maximum entries and the group
      // width of the grouped bars. The calculation is based directly on the example
      // presented in the MPAndroidChart library example activities.
      chart.getXAxis().setAxisMaximum(START_X_VALUE
          + chart.getData().getGroupWidth(GROUP_SPACE, barSpace) * maxEntries);
    }
  }
}
