package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.Dataset;

public abstract class MockChartModel<D extends Dataset> {
    protected D dataSeries;
    protected Data chartData;

    protected MockChartModel(Data chartData) {
        this.chartData = chartData;
    }

    public D getDataSeries() {
        return dataSeries;
    }

    public abstract void addEntry(float x, float y);

    public abstract void changeColor(String color);

    public void changeLabel(String text) {
        dataSeries.setLabel(text);
    }

    /**
     * Adds the data series of this object to the Chart.
     */
    protected void addDataSeriesToChart() {
        // When adding the first Data Series, it should be set
        // to the Chart Data object itself rather then appended,
        // to register the first (new) DataSet List to the Chart data.
        // Subsequent adding of Data Series objects can simply be added
        // to the end of the List.
        if (chartData.getDatasets().size() == 0) {
            chartData.setDatasets(dataSeries);
        } else {
            chartData.getDatasets().add(dataSeries);
        }
    }
}
