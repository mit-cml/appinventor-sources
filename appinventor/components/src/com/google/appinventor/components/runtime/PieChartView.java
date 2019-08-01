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
    // Instantiate the Root View layout and the Root Chart
    rootView = new RelativeLayout(context);
    chart = new PieChart(context);

    // Store the activity variable locally
    this.activity = context;

    // Initialize default settings for the Root Chart
    initializeDefaultSettings();
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();
  }

  @Override
  public View getView() {
    // Returns the underlying root RelativeLayout view
    // which stores all the Pie Chart rings
    return rootView;
  }

  @Override
  public ChartDataModel createChartModel() {
    PieChart pieChart;

    if (pieCharts.isEmpty()) { // No Pie Charts have been added yet
      pieChart = chart; // Set Pie Chart to root Pie Chart
    } else {
      pieChart = new PieChart(activity); // Create a new Pie Chart
    }

    // Default settings
    // TODO: Move to separate method
    pieChart.setHoleRadius(0);
    pieChart.setTransparentCircleRadius(0);

    // Add the Pie Chart (ring) to the Pie Charts List and
    // to the root layout
    pieCharts.add(pieChart);
    rootView.addView(pieChart);

    // Match height & width of parent
    // TODO: Alter width & height accordingly
    pieChart.setLayoutParams(new RelativeLayout.LayoutParams
        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    // Return a new Pie Chart Data model
    return new PieChartDataModel(pieChart, new PieData());
  }
}
