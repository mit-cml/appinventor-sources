package com.google.appinventor.components.runtime;

import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;

public abstract class PointChartView<T extends BarLineChartBase,
    D extends BarLineScatterCandleBubbleData>
    extends AxisChartView<T, D> {
  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // Since the Chart is stored in a RelativeLayout, settings are
    // needed to fill the Layout.
    chart.setLayoutParams(new ViewGroup.LayoutParams
        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

  @Override
  public View getView() {
    return chart;
  }
}
