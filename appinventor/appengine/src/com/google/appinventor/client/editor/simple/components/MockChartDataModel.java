package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.Dataset;

import java.util.List;

public abstract class MockChartDataModel<D extends Dataset> {
    protected D dataSeries;
    protected Data chartData;

    /**
     * Creates a new Mock Chart Model object instance, linking it with
     * the Data object of a specific Chart.
     *
     * @param chartData  Chart Data object to link to
     */
    protected MockChartDataModel(Data chartData) {
        this.chartData = chartData;
    }

    /**
     * Changes the Color of the Data Series.
     *
     * @param color  New Color value in &HAARRGGBB format.
     */
    public abstract void changeColor(String color);

    /**
     * Changes the label of the Data Series.
     *
     * @param text  New text value
     */
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

    /**
     * Removes the Data Series from the Chart.
     */
    public void removeDataSeriesFromChart() {
        chartData.getDatasets().remove(dataSeries);
    }

    /**
     * Converts an ARGB color to RGB hex format.
     *
     * @param color  &HAARRGGBB format color string
     * @return #RRGGBB format color string
     */
    protected String getHexColor(String color) {
        // The idea: Remove &H at the beginning, replace with # and reorder ARGB to RGB
        return "#" + color.substring(4);
    }

    /**
     * Sets the elements of the Data Series from a CSV-formatted String.
     *
     * @param elements String in CSV format
     */
    public abstract void setElements(String elements);

    /**
     * Sets the default data option for a Data Point List (in-place)
     *
     * @param dataPoints  empty list of Data Points to populate.
     */
    protected abstract void setDefaultElements(List<DataPoint> dataPoints);

    /**
     * Sets the default styling properties of the Data Series.
     */
    protected abstract void setDefaultStylingProperties();
}
