package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends ChartView<PieChart, PieData> {
  private RelativeLayout rootView;
  private List<PieChart> pieCharts = new ArrayList<PieChart>();
  private Activity activity;
  private int pieHoleRadius = 0;

  private List<LegendEntry> legendEntries = new ArrayList<LegendEntry>();

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

    // The Legend must be drawn inside to prevent inner rings from
    // being misaligned on the main Pie Chart.
    chart.getLegend().setDrawInside(true);
    chart.getLegend().setCustom(legendEntries);
  }

  @Override
  public View getView() {
    // Returns the underlying root RelativeLayout view
    // which stores all the Pie Chart rings
    return rootView;
  }

  @Override
  public ChartDataModel createChartModel() {
    PieChart pieChart = createPieChartRing();

    // Return a new Pie Chart Data model
    return new PieChartDataModel(this, pieChart, new PieData());
  }

  /**
   * Creates, initializes & attaches a new Pie Chart ring to add to the
   * Pie Chart root view.
   *
   * To be called upon creating a new PieChartDataModel.
   * @return  created Pie Chart instance
   */
  private PieChart createPieChartRing() {
    PieChart pieChart;

    if (pieCharts.isEmpty()) { // No Pie Charts have been added yet (root Pie Chart)
      pieChart = chart; // Set Pie Chart to root Pie Chart
    } else { // Inner Pie Chart
      pieChart = new PieChart(activity); // Create a new Pie Chart
      pieChart.getDescription().setEnabled(false); // Hide description
      pieChart.getLegend().setEnabled(false); // Hide legend
    };

    // Set the corresponding properties of the Pie Chart view
    // to the newly created Pie Chart
    setPieChartProperties(pieChart);

    // Create RelativeLayout params with MATCH_PARENT height and width and
    // CENTER_IN_PARENT property set to true. A future method call will
    // adjust all the necessary widths & heights.
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
        (ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    pieChart.setLayoutParams(params);

    pieCharts.add(pieChart); // Add new Pie Chart (ring) to the Pie Charts List
    rootView.addView(pieChart); // Add new Pie Chart (ring) to the root View

    // Return the newly constructed Pie Chart
    return pieChart;
  }

  /**
   * Sets the mutually defined styling proeprties to the specified Pie
   * Chart. This method allows having consistency between all the
   * instantiated Pie Chart rings.
   * @param chart  Pie Chart to apply styling settings to
   */
  private void setPieChartProperties(PieChart chart) {
    chart.setDrawEntryLabels(false);
  }

  @Override
  public synchronized void Refresh() {
    // Refresh each Pie Chart (ring) individually
    for (final PieChart pieChart : pieCharts) {
      // Notify the Data component of data changes (needs to be called
      // when Datasets get changed directly)
      pieChart.getData().notifyDataChanged();

      // Notify the Chart of Data changes (needs to be called
      // when Data objects get changed directly)
      pieChart.notifyDataSetChanged();

      // Invalidate the Chart on the UI thread (via the Handler)
      // The invalidate method should only be invoked on the UI thread
      // to prevent exceptions.
      uiHandler.post(new Runnable() {
        @Override
        public void run() {
          pieChart.invalidate();
        }
      });
    }
  }

  /**
   * Resizes all the inner Pie Charts according to the
   * total count of Pie Charts.
   */
  public void resizePieRings() {
    // Store width and height of last Pie Chart (since getHeight() and
    // getWidth() will not return the needed result instantly)
    int lastWidth = 0;
    int lastHeight = 0;

    float reductionFactor = (0.75f + pieHoleRadius/100f) / pieCharts.size();
    float radius = (100f - pieHoleRadius);
    float newRadius = 100f - radius * reductionFactor;

    for (int i = 0; i < pieCharts.size(); ++i) {
      PieChart pieChart = pieCharts.get(i);

      // Pie Chart non-last: expand radius
      if (i != pieCharts.size() - 1) {
        pieChart.setTransparentCircleRadius(newRadius);
        pieChart.setHoleRadius(newRadius);
        pieChart.setDrawHoleEnabled(true);
      } else {
        float setRadius = pieHoleRadius * (1f + Math.abs(newRadius - pieHoleRadius) / 100f);
        pieChart.setTransparentCircleRadius(setRadius);
        pieChart.setHoleRadius(setRadius);

        if (pieHoleRadius == 0) {
          pieChart.setDrawHoleEnabled(false);
        }
      }

      // FP on i != 0 always false
      if (i != 0) { // Inner Chart
        // Get the RelativeLayout parameters of the Pie Chart
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)(pieChart.getLayoutParams());

        float scalingFactor = (newRadius)/100f;

        // Compute new width & height
        lastWidth = (int)(lastWidth * scalingFactor);
        lastHeight = (int)(lastHeight * scalingFactor);

        // Set width & height of the Pie Chart, and update the Layout parameters
        // of the Chart
        params.width = lastWidth;
        params.height = lastHeight;
        pieChart.setLayoutParams(params);
      } else { // Root Chart
        // Set last height & width
        lastHeight = pieChart.getHeight();
        lastWidth = pieChart.getWidth();
      }

      // Since the Legend is drawn inside, an offset is needed
      // so that the rings of the Pie Chart do not overlap with the
      // Legend. The value was chosen as an optimal value (10 is too large,
      // 5 is too small, 7 is a somewhat in the middle option)
      // TODO: This could be improved in the future to (perhaps) dynamically
      // TODO: adjust the bottom offset.
      pieChart.setExtraBottomOffset(7);
    }
  }

  public void addLegendEntry(LegendEntry entry) {
    legendEntries.add(entry);
    pieCharts.get(0).getLegend().setCustom(legendEntries);
  }

  public void removeLegendEntry(LegendEntry entry) {
    legendEntries.remove(entry);
    pieCharts.get(0).getLegend().setCustom(legendEntries);
  }

  public void setPieRadius(int percent) {
    this.pieHoleRadius = 100 - percent;
    resizePieRings();
  }
}
