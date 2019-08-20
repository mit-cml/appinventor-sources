package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AxisChartView<C extends BarLineChartBase,
    D extends BarLineScatterCandleBubbleData> extends ChartView<C, D> {
  private List<String> axisLabels = new ArrayList<String>();

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Position X axis to the bottom
    chart.getAxisRight().setEnabled(false); // Disable right Y axis so there's only one

    // Set the granularities both for the X and the Y axis to 1
    chart.getAxisLeft().setGranularity(1f);
    chart.getXAxis().setGranularity(1f);

    chart.getXAxis().setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        int integerValue = Math.round(value);

        if (integerValue >= 0 && integerValue < axisLabels.size()) {
          return axisLabels.get(integerValue);
        } else {
          return super.getFormattedValue(value);
        }
      }
    });
  }

  public void setGridEnabled(boolean enabled) {
    chart.getXAxis().setDrawGridLines(enabled);
    chart.getAxisLeft().setDrawGridLines(enabled);
  }
}
