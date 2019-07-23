package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.ScatterDataset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public abstract class MockPointChartDataModel extends MockChartDataModel<ScatterDataset> {

  /**
   * Creates a new Mock Point Chart Data Model object instance, linking it with
   * the Data object of a specific Chart.
   *
   * @param chartData Chart Data object to link to
   */
  protected MockPointChartDataModel(Data chartData) {
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
  protected void setDefaultElements() {
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
      addEntryFromTuple(i + 1.0, yVal + i);
    }
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

  @Override
  public void addEntryFromTuple(String... tuple) {
    try {
      double xValue = Double.parseDouble(tuple[0]);
      double yValue = Double.parseDouble(tuple[1]);

      addEntryFromTuple(xValue, yValue);
    } catch (NumberFormatException e) {
      // Wrong input. Do nothing.
    }
  }

  public void addEntryFromTuple(Double... tuple) {
    DataPoint dataPoint = new DataPoint();
    dataPoint.setX(tuple[0]);
    dataPoint.setY(tuple[1]);

    if (dataSeries.getDataPoints().size() == 0) {
      dataSeries.setDataPoints(dataPoint);
    } else {
      dataSeries.getDataPoints().add(dataPoint);
    }
  }

  @Override
  protected int getTupleSize() {
    return 2;
  }

  @Override
  public void clearEntries() {
    dataSeries.getDataPoints().clear();
  }
}
