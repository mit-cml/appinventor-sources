package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends ChartView<PieChart, PieData> {
  private RelativeLayout rootView;
  private List<PieChart> pieCharts = new ArrayList<PieChart>();
  private Activity activity;

  public PieChartView(Activity context) {
    rootView = new RelativeLayout(context);
    chart = new PieChart(context);

    this.activity = context;

    initializeDefaultSettings();
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();
  }

  @Override
  public View getView() {
    return rootView;
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

    pieChart.setHoleRadius(0);
    pieChart.setTransparentCircleRadius(0);

    rootView.addView(pieChart);

    pieChart.setLayoutParams(new RelativeLayout.LayoutParams
        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    return new PieChartDataModel(pieChart, new PieData());
  }
}
