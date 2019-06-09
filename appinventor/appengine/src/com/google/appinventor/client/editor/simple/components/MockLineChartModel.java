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
        dataSeries.setBorderWidth(1);
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

    @Override
    public void changeColor(String color) {
        // Color value is passed in as follows: &HAARRGGBB
        // But we need a format: #RRGGBBAA
        // TBD: move to utility method
        color = "#" + color.substring(4) + color.substring(2, 4);
        dataSeries.setBackgroundColor(color);
        dataSeries.setPointBackgroundColor(color);
        dataSeries.setBorderColor(color);
    }
}
