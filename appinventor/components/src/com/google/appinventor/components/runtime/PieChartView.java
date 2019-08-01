package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends ChartView<PieChart, PieData> {
  private List<PieChart> pieCharts = new ArrayList<PieChart>();
  private Activity activity;

  public PieChartView(Activity context) {
    chart = new PieChart(context);

    this.activity = context;

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
    PieChart pieChart;

    if (pieCharts.isEmpty()) {
      pieChart = chart;
    } else {
      pieChart = new PieChart(activity);
    }

    pieCharts.add(pieChart);

//    pieChart.setLayoutParams(new ViewGroup.LayoutParams
//        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    return new PieChartDataModel(pieChart, new PieData());
  }
}
