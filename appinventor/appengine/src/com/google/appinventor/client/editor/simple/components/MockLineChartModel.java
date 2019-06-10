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

            DataPoint dp1 = new DataPoint();
            dp1.setX(1);
            dp1.setY(1 + add);
            DataPoint dp2 = new DataPoint();
            dp2.setX(2);
            dp2.setY(2 + add);

            dataSeries.setDataPoints(dp1, dp2);

            return;
        }

        DataPoint[] dataPoints = new DataPoint[entries.length];

        for (int i = 0; i < entries.length; ++i) {
            dataPoints[i] = new DataPoint();
            dataPoints[i].setX((i+1));
            dataPoints[i].setY((i+1));
        }

        dataSeries.setDataPoints(dataPoints);
    }
}
