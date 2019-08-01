package com.google.appinventor.components.runtime;

import android.os.Handler;
import android.view.View;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.ChartData;

public abstract class ChartView<C extends Chart, D extends ChartData> {
    protected C chart;
    protected D data;

    /**
     * Returns the underlying view holding all the necessary Chart Views.
     * The reason this does not return the Chart view straight away is
     * due to some Charts having more than one view (e.g. Pie Chart
     * with rings)
     * @return  Chart view
     */
    public abstract View getView();

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

    private Handler uiHandler = new Handler();

    /**
     * Refreshes the Chart to react to Data Set changes.
     *
     * The method is made asynchronous since multiple Data Sets
     * may attempt to refresh the Chart at the same time.
     */
    public synchronized void Refresh() {
        // Notify the Data component of data changes (needs to be called
        // when Datasets get changed directly)
        // TODO: Possibly move to ChartDataBase?
        chart.getData().notifyDataChanged();

        // Notify the Chart of Data changes (needs to be called
        // when Data objects get changed directly)
        chart.notifyDataSetChanged();

        // Invalidate the Chart on the UI thread (via the Handler)
        // The invalidate method should only be invoked on the UI thread
        // to prevent exceptions.
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                chart.invalidate();
            }
        });
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
