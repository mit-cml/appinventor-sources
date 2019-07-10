package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;

public class MockAreaChartDataModel extends MockLineChartBaseDataModel {
    public MockAreaChartDataModel(Data chartData) {
        super(chartData);
    }

    @Override
    protected void setDefaultStylingProperties() {
        super.setDefaultStylingProperties();
        dataSeries.setFill(true);
    }

    @Override
    protected void addDataSeriesToChart() {
        if (chartData.getDatasets().size() == 0) {
            chartData.setDatasets(dataSeries);
        } else {
            // Area Chart draw order is reversed in MPAndroidChart,
            // so we have to reverse the order in which we add
            // Data Series in the MockAreaChart component to
            // preserve the drawing order.
            chartData.getDatasets().add(0, dataSeries);
        }
    }
}
