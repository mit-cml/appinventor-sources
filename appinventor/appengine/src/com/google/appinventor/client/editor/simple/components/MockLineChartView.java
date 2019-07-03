package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.ScatterChart;

public class MockLineChartView extends MockChartViewBase<ScatterChart> {
    public MockLineChartView() {
        chartWidget = new ScatterChart();
        initializeDefaultSettings();
    }

    @Override
    public MockChartDataModel createDataModel() {
        return new MockLineChartDataModel(chartWidget.getData());
    }
}
