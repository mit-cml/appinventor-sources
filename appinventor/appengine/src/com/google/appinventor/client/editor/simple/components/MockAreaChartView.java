package com.google.appinventor.client.editor.simple.components;

public class MockAreaChartView extends MockLineChartViewBase {
    public MockAreaChartView() {
        super();
    }

    @Override
    protected void initializeDefaultSettings() {
        super.initializeDefaultSettings();

        // Due to differing drawing orders of the Android and the
        // Mock Chart implementations, the Legend has to be reversed
        // in the Mock Area Chart and the Data Series have to be added
        // in reverse order as well to represent the Android data series
        // accurately.
        chartWidget.getOptions().getLegend().setReverse(true);
    }

    @Override
    public MockChartDataModel createDataModel() {
        return new MockAreaChartDataModel(chartWidget.getData());
    }
}
