package com.google.appinventor.components.runtime;

import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;

public abstract class PointChartView<T extends BarLineChartBase,
    D extends BarLineScatterCandleBubbleData>
    extends ChartView<T, D> {
  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // Since the Chart is stored in a RelativeLayout, settings are
    // needed to fill the Layout.
    chart.setLayoutParams(new ViewGroup.LayoutParams
        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Position X axis to the bottom
    chart.getAxisRight().setDrawLabels(false); // Disable right Y axis so there's only one
  }

  @Override
  public void setGridEnabled(boolean enabled) {
    super.setGridEnabled(enabled);
    chart.getAxisLeft().setDrawGridLines(enabled);
    chart.getAxisRight().setDrawGridLines(enabled);
  }

  @Override
  public View getView() {
    return chart;
  }
}
