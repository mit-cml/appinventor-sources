package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

public class BarChartView extends ChartView<BarChart, BarData> {
  private static final float START_X_VALUE = 0f;
  private static final float GROUP_SPACE = 0.2f;
  private static final float BAR_SPACE = 0.05f;


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
    chart.getXAxis().setCenterAxisLabels(true);
    chart.getXAxis().setGranularity(1f);
    chart.getXAxis().setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value) {
        return String.valueOf((int) value);
      }
    });


    chart.getAxisRight().setDrawLabels(false); // Disable right Y axis so there's only one
  }

  @Override
  protected Runnable getRefreshRunnable() {
    return new Runnable() {
      @Override
      public void run() {
        if (chart.getData().getDataSetCount() >= 2) {
          chart.groupBars(START_X_VALUE, GROUP_SPACE, BAR_SPACE);
          chart.getXAxis().setAxisMinimum(START_X_VALUE);

          int maxEntries = 0;

          for (IBarDataSet dataSet : chart.getData().getDataSets()) {
            maxEntries = Math.max(maxEntries, dataSet.getEntryCount());
          }

          chart.getXAxis().setAxisMaximum(START_X_VALUE + chart.getData().getGroupWidth(GROUP_SPACE, BAR_SPACE) * maxEntries);
        }

        // Notify the Data component of data changes (needs to be called
        // when Datasets get changed directly)
        chart.getData().notifyDataChanged();

        // Notify the Chart of Data changes (needs to be called
        // when Data objects get changed directly)
        chart.notifyDataSetChanged();

        // Invalidate the Chart view for the changes to take
        // effect. NOTE: Most exceptions with regards to data
        // changing too fast occur as a result of calling the
        // invalidate method.
        chart.invalidate();
      }
    };
  }
}
