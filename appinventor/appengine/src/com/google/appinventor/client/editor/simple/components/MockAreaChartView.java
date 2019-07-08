package com.google.appinventor.client.editor.simple.components;

public class MockAreaChartView extends MockLineChartView {
    public MockAreaChartView() {
        super();
    }

    @Override
    public MockChartDataModel createDataModel() {
        return new MockAreaChartDataModel(chartWidget.getData());
    }
}
