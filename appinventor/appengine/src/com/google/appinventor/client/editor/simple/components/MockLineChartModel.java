package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.ScatterDataset;

import java.util.ArrayList;
import java.util.Arrays;

public class MockLineChartModel extends MockChartModel<ScatterDataset> {

    public MockLineChartModel(Data chartData) {
        super(chartData);

        // Create the Data Series object
        dataSeries = new ScatterDataset();

        dataSeries.setFill(false);
        dataSeries.setBorderWidth(1);
        dataSeries.setLineTension(0);
        dataSeries.setShowLine(true);

        // Adds the Data Series to the Chart.
        addDataSeriesToChart();
    }

    @Override
    public void changeColor(String color) {
        color = getHexColor(color);
        dataSeries.setBackgroundColor(color);
        dataSeries.setPointBackgroundColor(color);
        dataSeries.setBorderColor(color);
    }

    @Override
    public void setElements(String elements) {
        String[] entries = elements.split(",");

        // Default data
        if (entries.length == 0 || elements.equals("")) {
            int add = chartData.getDatasets().indexOf(dataSeries);
            entries = new String[] { (1 + add) + "", (2 + add) + "", (3 + add) + "", (4 + add) + "" };
        }

        DataPoint[] dataPoints = new DataPoint[entries.length];

        for (int i = 0; i < entries.length; ++i) {
            dataPoints[i] = new DataPoint();
            dataPoints[i].setX((i+1));
            dataPoints[i].setY(Double.parseDouble(entries[i]));
        }

        dataSeries.setDataPoints(dataPoints);
    }
}
