package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.ChartData;

public abstract class ChartView<C extends Chart, D extends ChartData> {
    protected C chart;
    protected D data;

    /**
     * Returns the underlying Chart view.
     *
     * @return  Chart object instance
     */
    public C getView() {
        return chart;
    }

    /**
     * Sets the background color of the Chart.
     * @param argb  background color
     */
    public void setBackgroundColor(int argb) {
        chart.setBackgroundColor(argb);
    }

    /**
     * Sets the description text of the Chart.
     * @param text  description text
     */
    public void setDescription(String text) {
        chart.getDescription().setText(text);
    }

    /**
     * Refreshes the Chart to react to Data Set changes.
     */
    public void Refresh() {
        // Only refresh data itself if data exists on Chart
        if (chart.getData() != null) {
            chart.getData().notifyDataChanged();
        }

        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    /**
     * Creates a new Chart Model object instance.
     *
     * @return  Chart Model instance
     */
    public abstract ChartDataModel createChartModel();

    /**
     * Sets the necessary default settings for the Chart view.
     */
    protected void initializeDefaultSettings() {
        // Center the Legend
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }
}
