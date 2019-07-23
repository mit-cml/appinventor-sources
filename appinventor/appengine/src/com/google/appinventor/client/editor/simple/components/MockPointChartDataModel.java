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
  public String getDefaultTupleEntry(int index) {
    return index + "";
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
