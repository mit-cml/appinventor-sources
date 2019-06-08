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
}
