package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.LineDataset;

public class MockLineChartModel extends MockChartModel<LineDataset> {
    public MockLineChartModel() {
        dataSeries = new LineDataset();

        dataSeries.setFill(false);
        dataSeries.setPointBackgroundColor("black");
        dataSeries.setBackgroundColor("black");
        dataSeries.setBorderWidth(1);
        dataSeries.setLineTension(0);
        dataSeries.setLabel("");
    }

    @Override
    public void addDefaultData() {
        dataSeries.setData(1, 2, 3, 4);
    }
}
