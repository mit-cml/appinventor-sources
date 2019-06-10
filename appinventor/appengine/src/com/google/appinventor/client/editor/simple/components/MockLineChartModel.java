package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.ScatterDataset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        // Create new list of Data Points
        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();

        // Since entries come in pairs, we add 2 on each iteration.
        // Beginning from i = 1 instead of 0 to prevent out of bounds
        // accesses.
        for (int i = 1; i < entries.length; i += 2) {
            try {
                DataPoint dataPoint = new DataPoint();
                dataPoint.setX(Double.parseDouble(entries[i-1]));
                dataPoint.setY(Double.parseDouble(entries[i]));
                dataPoints.add(dataPoint);
            } catch (NumberFormatException e) {
                return; // Wrong input. Do not update entries.
            }
        }

        // No data points generated, fallback to default option.
        if (dataPoints.isEmpty()) {
            setDefaultElements(dataPoints);
        } else {
            // Since we are dealing with a Scatter Data Series, sorting
            // is a must, because otherwise, the Chart will not look representative.
            // Consider adding: (1, 2), (5, 3), (2, 5). We want the x = 2
            // value to be continuous on the Line Chart, rather than
            // going outside the Chart.
            dataPoints.sort(Comparator.comparingDouble(DataPoint::getX));
        }

        // Set the data points to the actual Data Series
        dataSeries.setDataPoints(dataPoints);
    }

    @Override
    protected void setDefaultElements(List<DataPoint> dataPoints) {
        int add = chartData.getDatasets().indexOf(dataSeries);

        for (int i = 1; i <= 4; ++i) {
            DataPoint dataPoint = new DataPoint();
            dataPoint.setX(i);
            dataPoint.setY((i + add));
            dataPoints.add(dataPoint);
        }
    }
}
