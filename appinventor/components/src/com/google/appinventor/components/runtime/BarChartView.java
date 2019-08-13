package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;

public class BarChartView extends ChartView<BarChart, BarData> {
  public BarChartView(Activity context) {
    chart = new BarChart(context);

    data = new BarData();
    chart.setData(data);

    initializeDefaultSettings();
  }

  @Override
  public View getView() {
    return chart;
  }

  @Override
  public ChartDataModel createChartModel() {
    return new BarChartDataModel(data);
  }

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
}
