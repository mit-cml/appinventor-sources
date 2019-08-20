package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;

public abstract class AxisChartView<C extends BarLineChartBase,
    D extends BarLineScatterCandleBubbleData> extends ChartView<C, D> {
  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Position X axis to the bottom
    chart.getAxisRight().setEnabled(false); // Disable right Y axis so there's only one

    // Set the granularities both for the X and the Y axis to 1
    chart.getAxisLeft().setGranularity(1f);
    chart.getXAxis().setGranularity(1f);
  }
}
