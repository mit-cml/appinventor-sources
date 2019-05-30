package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;

import java.util.HashSet;

public abstract class ChartDataBase<T extends ChartData> extends AndroidNonvisibleComponent {

    protected T chartData;
    protected HashSet<ChartBase> charts;

    /**
     * Creates a new ChartDataBase component.
     *
     * @param form the container that this component will be placed in
     */
    protected ChartDataBase(Form form) {
        super(form);

        charts = new HashSet<ChartBase>();
    }

    /**
     * Getter method for the underlying data object of the ChartData component
     *
     * @return - ChartData object
     */
    public T getChartData() {
        return chartData;
    }

    /**
     * Add Chart component to observe by this Chart Data component.
     *
     * @param chart  Chart component
     */
    public void addChart(ChartBase chart) {
        charts.add(chart);
    }

    /**
     * Refreshes all the Charts that use this Chart Data component.
     * Called whenever there are changes to the underlying Chart data object.
     */
    protected void refreshCharts() {
        for (ChartBase chart : charts) {
            if (chart != null) {
                chart.Refresh();
            }
        }
    }
}
