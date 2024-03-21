// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Chart Views (Chart UI) for Charts types that
 * have an axis.
 *
 * @see com.google.appinventor.components.runtime.ChartView
 * @param <E> MPAndroidChart class for Entry type.
 * @param <T> MPAndroidChart class for DataSet collection.
 * @param <D> MPAndroidChart class for ChartData series collection.
 * @param <C> MPAndroidChart class for Chart view.
 * @param <V> Type of the view for reflective operations
 */
public abstract class AxisChartView<
    E extends Entry,
    T extends IBarLineScatterCandleBubbleDataSet<E>,
    D extends BarLineScatterCandleBubbleData<T>,
    C extends BarLineChartBase<D>,
    V extends AxisChartView<E, T, D, C, V>
    > extends ChartView<E, T, D, C, V> {
  // List containing Strings to use for the X Axis of the Axis Chart.
  // The first entry corresponds to an x value of 0, the second to
  // an x value of 1, and so on.
  private List<String> axisLabels = new ArrayList<>();

  /**
   * Creates a new Axis Chart View with the specified Chart component
   * instance as the parent of the View.
   *
   * @param chartComponent Chart component to link View to
   */
  protected AxisChartView(Chart chartComponent) {
    super(chartComponent);
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Position X axis to the bottom
    chart.getAxisRight().setEnabled(false); // Disable right Y axis so there's only one

    // Set the granularities both for the X and the Y axis to 1
    chart.getAxisLeft().setGranularity(1f);
    chart.getXAxis().setGranularity(1f);

    // Set custom value formatter for the X Axis to display custom
    // labels, if they are present.
    chart.getXAxis().setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        // Round the float value to an integer
        int integerValue = Math.round(value);

        // Subtract the (integer previously floored) X Axis minimum value
        // from the integer value. This is needed to start labelling x axis values from
        // the beginning of the axis.
        // For example, consider the x values: -5, -4, -3.
        // The minimum of the x axis would be -5. We subtract -5 to get
        // 0, which corresponds to the index of 0.
        // integerValue -= Math.floor(chart.getXAxis().getAxisMinimum());
        integerValue -= (int) chart.getXAxis().getAxisMinimum();

        // Using the integer as an index (non-negative), check
        // whether the axis labels List is in range for the index
        if (integerValue >= 0 && integerValue < axisLabels.size()) {
          // The Axis Labels has an entry in the index; Use the
          // custom label as the x axis label
          return axisLabels.get(integerValue);
        } else {
          // Custom axis label not present; Use the usual value
          return super.getFormattedValue(value);
        }
      }
    });

    if (chartComponent.XFromZero()) {
      chart.getXAxis().setAxisMaximum(0);
    }
    if (chartComponent.YFromZero()) {
      chart.getAxisLeft().setAxisMinimum(0);
    }
  }

  /**
   * Resets the X and Y axes to their default values.
   */
  public void resetAxes() {
    chart.getXAxis().resetAxisMaximum();
    chart.getXAxis().resetAxisMinimum();
    chart.getAxisLeft().resetAxisMaximum();
    chart.getAxisLeft().resetAxisMinimum();
    chart.invalidate();
  }

  /**
   * Sets whether the X origin should be fixed to zero.
   *
   * @param zero true if the X origin should be zero
   */
  public void setXMinimum(boolean zero) {
    if (zero) {
      chart.getXAxis().setAxisMinimum(0);
    } else {
      chart.getXAxis().resetAxisMinimum();
    }
    chart.invalidate();
  }

  /**
   * Sets whether the Y origin should be fixed to zero.
   *
   * @param zero true if the Y origin should be zero
   */
  public void setYMinimum(boolean zero) {
    if (zero) {
      chart.getAxisLeft().setAxisMinimum(0);
    } else {
      chart.getAxisLeft().resetAxisMinimum();
    }
    chart.invalidate();
  }

  public double[] getXBounds() {
    return new double[] {chart.getXAxis().getAxisMinimum(), chart.getXAxis().getAxisMaximum()};
  }

  public void setXBounds(double minimum, double maximum) {
    chart.getXAxis().setAxisMinimum((float) minimum);
    chart.getXAxis().setAxisMaximum((float) maximum);
    chart.invalidate();
  }

  public double[] getYBounds() {
    return new double[] {
        chart.getAxisLeft().getAxisMinimum(),
        chart.getAxisLeft().getAxisMaximum()
    };
  }

  public void setYBounds(double minimum, double maximum) {
    chart.getAxisLeft().setAxisMinimum((float) minimum);
    chart.getAxisLeft().setAxisMaximum((float) maximum);
    chart.invalidate();
  }

  /**
   * Changes the visibility of the Chart grid.
   *
   * @param enabled Indicates whether the grid should be shown.
   */
  public void setGridEnabled(boolean enabled) {
    // Change the visibilities of the X Axis and the Y (right)
    // axis grids to the specified value.
    chart.getXAxis().setDrawGridLines(enabled);
    chart.getAxisLeft().setDrawGridLines(enabled);
    chart.invalidate();
  }

  /**
   * Changes the List of X Axis labels to use for the Chart
   * to the specified List of Strings.
   *
   * <p>The first entry of the List corresponds to an x value of 0,
   * the second entry to an x value of 1, and so on.
   * If an entry is not present for an x value, a default value
   * (usually the numeric value) is used instead.
   *
   * @param labels List of labels to apply to the X Axis
   */
  public void setLabels(List<String> labels) {
    this.axisLabels = labels;
  }
}
