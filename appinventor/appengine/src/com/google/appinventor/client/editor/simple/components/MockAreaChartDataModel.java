package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;

public class MockAreaChartDataModel extends MockLineChartDataModel {
    public MockAreaChartDataModel(Data chartData) {
        super(chartData);
    }

    @Override
    protected void setDefaultStylingProperties() {
        super.setDefaultStylingProperties();
        dataSeries.setFill(true);
    }
}
