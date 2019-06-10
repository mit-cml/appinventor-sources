package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.ScatterDataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

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
        if (elements.equals("")) {
            int add = chartData.getDatasets().indexOf(dataSeries);
            entries = new String[] { "1", (1 + add) + "", "2", (2 + add) + "", "3", (3 + add) + "", "4", (4 + add) + "" };
        }

        // Create new list of Data Points
        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

        // Since entries come in pairs, we add 2 on each iteration.
        // Beginning from i = 1 instead of 0 to privenet out of bounds
        // accesses.
        for (int i = 1; i < entries.length; i += 2) {
            DataPoint dataPoint = new DataPoint();
            dataPoint.setX(Double.parseDouble(entries[i-1]));
            dataPoint.setY(Double.parseDouble(entries[i]));
            dataPoints.add(dataPoint);
        }

        // Since we are dealing with a Scatter Data Series, sorting
        // is a must, because otherwise, the Chart will not look representative.
        // Consider adding: (1, 2), (5, 3), (2, 5). We want the x = 2
        // value to be continuous on the Line Chart, rather than
        // going outside the Chart.
        dataPoints.sort(Comparator.comparingDouble(DataPoint::getX));

        // No data points generated. Return.
        if (dataPoints.isEmpty()) {
            return;
        }

        // Set the data points to the actual Data Series
        dataSeries.setDataPoints(dataPoints);
    }
}
