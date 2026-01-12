// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;

import com.google.appinventor.components.common.PointStyle;

import java.util.Comparator;
import java.util.Optional;

import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.ScatterDataset;

/**
 * Data Model class for Point-based Charts for the Mock Chart component.
 * The class is used in extensions of the Point Charts whenever the specified
 * class type is selected in the Mock Chart (e.g. Line/Scatter Chart)
 *
 * <p>Extensions should minimally only provide a constructor. The class handles
 * 2D data operations.
 */
public abstract class MockPointChartDataModel<V extends MockPointChartView<V>>
    extends MockChartDataModel<ScatterDataset, V> {

  /**
   * Creates a new Mock Point Chart Data Model object instance, linking it with
   * the specified Point Chart View.
   *
   * @param view Chart View to link model to.
   */
  protected MockPointChartDataModel(V view) {
    super(view);

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
        .flatMap(l -> ((ScatterDataset) l).getDataPoints().stream()) // Flatten the nested lists
        .max(Comparator.comparing(DataPoint::getY)); // Get the maximum data point value

    // Get the maximum data point Y value. We take the maximum to ensure
    // that our newly added default data does not overlap existing lines.
    double maxY = maxYPoint.map(DataPoint::getY).orElse(0.0);

    for (int i = 0; i < points; ++i) {
      // Construct the x and y values based on the index
      double x = i + 1;
      double y = maxY + i;

      // Add an entry based on the constructed values
      addEntryFromTuple(x, y);
    }
  }

  @Override
  public void addEntryFromTuple(String... tuple) {
    try {
      // Parse x and y values
      double x = Double.parseDouble(tuple[0]);
      double y = Double.parseDouble(tuple[1]);

      // Add an entry from the parsed values
      addEntryFromTuple(x, y);
    } catch (NumberFormatException e) {
      ErrorReporter.reportInfo(MESSAGES.invalidChartDataEntry());
    }
  }

  /**
   * Adds an entry to the Data Series from the specified tuple.
   *
   * <p>The tuple is expected to have at least 2 entries. All subsequent
   * values are ignored.
   *
   * @param tuple tuple (array of doubles)
   */
  public void addEntryFromTuple(Double... tuple) {
    // Construct the data point
    DataPoint dataPoint = new DataPoint();
    dataPoint.setX(tuple[0]);
    dataPoint.setY(tuple[1]);

    // Due to the nature of the library used, if the data points
    // list is empty, the data point has to be set to the Data Series.
    // Accessing it directly will return a copy (rather than a reference)
    // of the data points list. However, once a data point is set, the
    // getDataPoints method will return the reference, which can then be
    // altered.
    if (dataSeries.getDataPoints().size() == 0) {
      dataSeries.setDataPoints(dataPoint);
    } else {
      dataSeries.getDataPoints().add(dataPoint);
    }
  }

  @Override
  public String getDefaultTupleEntry(int index) {
    // For Point-based Charts, the default tuple entry is simply the
    // current index.
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

  /**
   * Changes the Point Shape of the Data Series.
   * @param shape new Point Shape value (integer)
   */
  public void changePointShape(PointStyle shape) {
    /*
     * By default, no functionality should happen because not all
     * Point Chart Data Models can yet support this functionality.
     * Namely, the Line Chart Data Model cannot support it because
     * the Android library does not yet support different point
     * shapes for Line Charts.
     */
  }
}
