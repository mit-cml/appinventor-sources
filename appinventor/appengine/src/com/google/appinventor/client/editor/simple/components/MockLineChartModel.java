package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.Chart;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.LineDataset;

import java.util.ArrayList;

public class MockLineChartModel extends MockChartModel<LineDataset> {

    public MockLineChartModel() {
        dataSeries = new LineDataset();

        dataSeries.setFill(false);
        dataSeries.setPointBackgroundColor("black");
        dataSeries.setBackgroundColor("black");
        dataSeries.setBorderWidth(2);
        dataSeries.setLineTension(0);
        dataSeries.setLabel("");
    }

    @Override
    public void addEntry(float x, float y) {
        DataPoint point = new DataPoint();
        point.setX(x);
        point.setY(y);
        dataSeries.getDataPoints(true).add(point);
    }
}
