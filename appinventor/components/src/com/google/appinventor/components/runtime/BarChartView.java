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
  private static final float GROUP_SPACE = 0.08f;


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
        int dataSetCount = chart.getData().getDataSetCount();

        if (dataSetCount >= 2) {
          // (BarWidth + BarSpace) * #datasets + groupSpace should equal 1
          // to fit into the granularity of the Chart. The 1f here represents
          // the fixed granularity of the X axis. Since the number of data sets
          // can change, the bar space and bar width should be re-calculated.
          // 10% and 90% of the remainder are given to the Bar Space and the Bar Width,
          // respectively.
          float x = (1f - GROUP_SPACE)/dataSetCount;
          float barSpace = x * 0.1f;
          float barWidth = x * 0.9f;

          // Update the bar width and regroup the bars with the recalculated values
          chart.getData().setBarWidth(barWidth);
          chart.groupBars(START_X_VALUE, GROUP_SPACE, barSpace);
          chart.getXAxis().setAxisMinimum(START_X_VALUE);

          // Determine the maximum number of entries between Bar Data Sets
          int maxEntries = 0;

          for (IBarDataSet dataSet : chart.getData().getDataSets()) {
            maxEntries = Math.max(maxEntries, dataSet.getEntryCount());
          }

          // Set the maximum value for the x axis based on maximum entries and the group
          // width of the grouped bars. The calculation is based directly on the example
          // presented in the MPAndroidChart library example activities.
          chart.getXAxis().setAxisMaximum(START_X_VALUE +
              chart.getData().getGroupWidth(GROUP_SPACE, barSpace) * maxEntries);
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
