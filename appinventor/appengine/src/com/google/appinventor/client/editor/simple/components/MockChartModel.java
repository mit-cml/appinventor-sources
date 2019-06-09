package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.data.Dataset;

public abstract class MockChartModel<D extends Dataset> {
    protected D dataSeries;

    protected MockChartModel() {

    }

    protected MockChartModel(D dataSeries) {
        this.dataSeries = dataSeries;
    }

    public D getDataSeries() {
        return dataSeries;
    }

    public abstract void addEntry(float x, float y);

    public abstract void changeColor(String color);

    public void changeLabel(String text) {
        dataSeries.setLabel(text);
    }
}
