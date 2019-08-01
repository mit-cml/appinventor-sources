package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;

public class PieChartView extends ChartView<PieChart, PieData> {
  public PieChartView(Activity context) {
    chart = new PieChart(context);

    data = new PieData();
    // chart.setData(data);

    initializeDefaultSettings();
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // Since the Chart is stored in a RelativeLayout, settings are
    // needed to fill the Layout.
    chart.setLayoutParams(new ViewGroup.LayoutParams
        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

  @Override
  public ChartDataModel createChartModel() {
    return null;
  }
}
