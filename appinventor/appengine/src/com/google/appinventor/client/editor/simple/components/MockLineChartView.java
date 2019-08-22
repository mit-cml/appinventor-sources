package com.google.appinventor.client.editor.simple.components;

/**
 * Chart View for the Line Chart
 *
 * Responsible for the GUI of the Line Chart.
 */
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
