package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.ScatterDataset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Chart Data Model for Mock Line Chart based views.
 */
public abstract class MockLineChartBaseDataModel extends MockChartDataModel<ScatterDataset>  {
    /**
     * Creates a new MockLineChartBaseDataModel instance.
     * @param chartData  Data object of the Chart View.
     */
    public MockLineChartBaseDataModel(Data chartData) {
        super(chartData);

        // Create the Data Series object
        dataSeries = new ScatterDataset();

        // Set the default style properties for the Data Series
        setDefaultStylingProperties();

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
            // going outside the Chart, which would happen since we
            // are using a Scatter Chart.
            dataPoints.sort(Comparator.comparingDouble(DataPoint::getX));
        }

        // Set the generated data points to the Data Series
        dataSeries.setDataPoints(dataPoints);
    }

    @Override
    protected void setDefaultElements(List<DataPoint> dataPoints) {
        final int points = 4; // Number of points to add

        // TBD: Might change this in the future.
        // Generally, this should not cause performance issues because typically there are not
        // that many data points.
        Optional<DataPoint> maxYPoint = chartData.getDatasets() // Get all the data sets
                .stream() // Create a stream
                .flatMap(l -> ((ScatterDataset)l).getDataPoints().stream()) // Flatten the nested lists to a List of data points
                .max(Comparator.comparing(DataPoint::getY)); // Get the maximum data point value

        // Get the maximum data point Y value. We take the maximum to ensure
        // that our newly added default data does not overlap existing lines.
        double yVal = maxYPoint.map(DataPoint::getY).orElse(0.0);

        for (int i = 0; i < points; ++i) {
            DataPoint dataPoint = new DataPoint();
            dataPoint.setX(i+1);
            dataPoint.setY((yVal + i));
            dataPoints.add(dataPoint);
        }
    }

    @Override
    protected void setDefaultStylingProperties() {
        dataSeries.setFill(false);
        dataSeries.setBorderWidth(1);
        dataSeries.setLineTension(0);
        dataSeries.setShowLine(true);
    }

    @Override
    public void setElementsFromCSVRows(List<List<String>> rows, List<String> columns) {
        // TODO: Refactor for more reusability
        // TODO: SetElements to default when all columns empty

        // No rows specified; Set elements to default values
        if (rows == null || rows.isEmpty()) {
            setElements("");
            return;
        }

        // First row is interpreted as the CSV column names
        List<String> columnNames = rows.get(0);

        StringBuilder elementStringBuilder = new StringBuilder(); // Used for constructing CSV-formatted elements
        List<Integer> indexes = new ArrayList<Integer>(); // Keep track of indexes representing column names

        // Iterate through the parameter specified for the columns to parse
        for (String column : columns) {
            // Get & store the index of the column
            int index = columnNames.indexOf(column);
            indexes.add(index);
        }

        // Iterate through all the rows (except the first, which is the columnNames)
        // The loop constructs a String of CSV values in format x1,y1,x2,y2,...,xn,yn
        for (int i = 1; i < rows.size(); ++i) {
            List<String> row = rows.get(i);

            // Iterate through all the indexes (or columns, in other words)
            for (int j = 0; j < indexes.size(); ++j) {
                // Get the index
                int index = indexes.get(j);

                if (index < 0) { // Column not found
                    // Use default value (just the i-th index)
                    elementStringBuilder.append(i);
                } else { // Column found
                    // The index represents the column to use from
                    // the current row. Fetch the value and add it to the
                    // result.
                    elementStringBuilder.append(row.get(index));
                }

                // Add a comma unless it is the very last entry
                if (i != rows.size() - 1 || j != indexes.size() - 1) {
                    elementStringBuilder.append(",");
                }
            }
        }

        // Pass the constructed result to parse via CSV
        setElements(elementStringBuilder.toString());
    }
}
