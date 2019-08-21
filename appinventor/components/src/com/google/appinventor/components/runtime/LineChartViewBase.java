package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;

public abstract class LineChartViewBase extends PointChartView<LineChart, LineData> {
  /**
   * Instantiate a new LineChartViewBase in the given context.
   *
   * @param context Context to instantiate view in
   */
  protected LineChartViewBase(Activity context) {
    chart = new LineChart(context);

    data = new LineData();
    chart.setData(data);

    initializeDefaultSettings();
  }
}
