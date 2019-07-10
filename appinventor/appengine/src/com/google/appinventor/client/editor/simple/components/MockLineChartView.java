package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.ScatterChart;

public class MockLineChartView extends MockLineChartViewBase {
    /**
     * Creates a new MockLineChartView object instance.
     */
    public MockLineChartView() {
        super();
    }

    @Override
    public MockChartDataModel createDataModel() {
        return new MockLineChartDataModel(chartWidget.getData());
    }
}
