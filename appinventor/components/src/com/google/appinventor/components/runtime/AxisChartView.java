package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public abstract class AxisChartView<C extends BarLineChartBase,
    D extends BarLineScatterCandleBubbleData> extends ChartView<C, D> {
  // List containing Strings to use for the X Axis of the Axis Chart.
  // The first entry corresponds to an x value of 0, the second to
  // an x value of 1, and so on.
  private List<String> axisLabels = new ArrayList<String>();

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
  }

  /**
   * Changes the visibility of the Chart grid.
   * @param enabled  Indicates whether the grid should be shown.
   */
  public void setGridEnabled(boolean enabled) {
    // Change the visibilities of the X Axis and the Y (right)
    // axis grids to the specified value.
    chart.getXAxis().setDrawGridLines(enabled);
    chart.getAxisLeft().setDrawGridLines(enabled);
  }

  /**
   * Changes the List of X Axis labels to use for the Chart
   * to the specified List of Strings.
   *
   * The first entry of the List corresponds to an x value of 0,
   * the second entry to an x value of 1, and so on.
   * If an entry is not present for an x value, a default value
   * (usually the numeric value) is used instead.
   *
   * @param labels  List of labels to apply to the X Axis
   */
  public void setLabels(List<String> labels) {
    this.axisLabels = labels;
  }
}
