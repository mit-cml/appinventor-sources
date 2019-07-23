package com.google.appinventor.components.runtime;

import android.app.Activity;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.ScatterData;

public class ScatterChartView extends PointChartView<ScatterChart, ScatterData> {
  /**
   * Instantiate a new ScatterChartView in the given context.
   *
   * @param context Context to instantiate view in
   */
  public ScatterChartView(Activity context) {
    chart = new ScatterChart(context);

    data = new ScatterData();
    chart.setData(data);

    initializeDefaultSettings();
  }

  @Override
  public ChartDataModel createChartModel() {
    return new ScatterChartDataModel(data);
  }
}
