package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.ScatterChart;

public class MockLineChartView extends MockChartView<ScatterChart> {
    /**
     * Creates a new MockLineChartView object instance.
     */
    public MockLineChartView() {
        // A ScatterChart widget is used to allow
        // arbitrary insertions of (x, y) points and
        // automatic generation of the X axis labels
        // (as well as automatic scaling)
        chartWidget = new ScatterChart();
        initializeDefaultSettings();
    }

    @Override
    public MockChartDataModel createDataModel() {
        return new MockLineChartDataModel(chartWidget.getData());
    }
}
