// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import org.pepstock.charba.client.data.BarDataset;

/**
 * Chart Data Model for Bar Chart based views.
 */
public class MockBarChartDataModel extends MockChartDataModel<BarDataset, MockBarChartView> {
  /**
   * Creates a new Mock Bar Chart Data Model object instance,
   * linking it with the specified MockBarChartView object.
   *
   * @param view Chart View to link the model to
   */
  public MockBarChartDataModel(MockBarChartView view) {
    super(view);

    // Create the Data Series object
    dataSeries = new BarDataset();

    // Set the default style properties for the Data Series
    setDefaultStylingProperties();

    // Adds the Data Series to the Chart.
    addDataSeriesToChart();
  }

  @Override
  public void changeColor(String color) {
    color = getHexColor(color);
    dataSeries.setBackgroundColor(color);
    dataSeries.setBorderColor(color);
  }

  @Override
  protected void setDefaultElements() {
    final int points = 4; // Number of points to add

    // Set the starting y value for the current model
    // as the total size of the Datasets present in the Chart.
    // This is used to differentiate values between added Data
    // components.
    // TODO: In the future, this could take into account the global
    // TODO: maximum (much like the MockLineChartDataModel does now)
    // TODO: or have recalculations done, since if Data Series are
    // TODO: deleted, the y values can overlap between Data Series.
    double startY = chartData.getDatasets().size();

    for (int i = 0; i < points; ++i) {
      // Construct the x and y values based on the index
      double curY = startY + i;

      // Add an entry based on the constructed values
      addEntryFromTuple((double) i, curY);
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
   * <p>The addEntryFromTuple method relies on the property that each
   * entry in the Bar Chart corresponds to an index, meaning that
   * an x value directly corresponds to an index in the Data Series.
   * This also implies that the entries in the Data Series are
   * sorted by x value. Moreover, if entries with an x value
   * which is at least 1 bigger than the current size is added,
   * blank entries are filled in between.
   *
   * @param tuple tuple (array of doubles)
   */
  public void addEntryFromTuple(Double... tuple) {
    // The first entry of the tuple is expected to represent
    // the x value. Since the Bar Chart's x values are whole
    // numbers, the value has to be rounded.
    int x = (int) Math.round(tuple[0]);

    // The second entry of the tuple is expected to be the y value.
    double y = tuple[1];

    // If x is less than 0, then skip the insertion, since
    // Bar Chart x values start from 0.
    if (x < 0) {
      return;
    }

    // Same data adding logic is used as in BarChartDataModel
    // (Android implementation)
    // TODO: This could probably be factored somehow to reduce redundancy
    // TODO: and use a common method.

    // If the x value is less than the current number of
    // Data Series in the Chart, then the x value already
    // exists in the Data Series (by the sorted entries property)
    if (x < dataSeries.getData().size()) {
      // Use x value as index to update the y value
      dataSeries.getData().set(x, y);
    } else {
      // Fill Bar Data Series with empty values until the
      // size equals the x value (to preserve sorted entries
      // and index property)
      while (dataSeries.getData().size() < x) {
        // Due to the way the Charba library currently
        // handles the getData method, if the current
        // number of Data entries is 0, the Data has to
        // be set instead of being added directly.
        if (dataSeries.getData().size() == 0) {
          dataSeries.setData(0.0);
        } else {
          dataSeries.getData().add(0.0);
        }
      }

      // After filling the entries (if necessary), the
      // value is added at the end of the Data Series
      // (size becomes 1 bigger than the x value, so the
      // x value now represents the last index of the Data)
      if (dataSeries.getData().size() == 0) {
        dataSeries.setData(y);
      } else {
        dataSeries.getData().add(y);
      }
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
    dataSeries.getData().clear();
  }

  @Override
  protected void postDataImportAction() {
    // No data points generated, fallback to default option.
    if (dataSeries.getData().isEmpty()) {
      setDefaultElements();
    }

    // After changing the elements of the Mock Bar Chart Data Model,
    // the labels for the x axis have to be reconstructed. This is
    // done from the view since all Data Series need to be taken into
    // account.
    view.updateLabels();
  }

  @Override
  public void removeDataSeriesFromChart() {
    super.removeDataSeriesFromChart();

    // After removing the Data Series from the Charts,
    // the labels have to yet again be recalculated.
    view.updateLabels();
  }
}
