// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;

import java.util.ArrayList;
import java.util.List;

import org.pepstock.charba.client.data.PieDataset;

public class MockPieChartDataModel extends MockChartDataModel<PieDataset, MockPieChartView> {
  // Local references of the colors and labels properties have to be
  // kept in the Data Model class to be able to add to them and set them.
  // This is due to the API for changing colors and labels being quite limited (in v2.5)
  private final List<String> colors = new ArrayList<>();
  private final List<String> labels = new ArrayList<>();
  private String color = "";

  /**
   * Creates a new Mock Pie Chart Model object instance, linking it with
   * the specified Chart View.
   *
   * @param view  Mock Pie Chart View to link Data Model to
   */
  public MockPieChartDataModel(MockPieChartView view) {
    super(view);

    // Create the Data Series object
    dataSeries = new PieDataset();

    // Set the default style properties for the Data Series
    setDefaultStylingProperties();

    // Adds the Data Series to the Chart.
    addDataSeriesToChart();
  }

  @Override
  public void changeColor(String color) {
    this.color = color;
    color = getHexColor(color);

    // Clear the old colors list
    colors.clear();

    // Add the same color for every entry
    for (int i = 0; i < dataSeries.getData().size(); ++i) {
      colors.add(color);
    }

    // Since the setBackgroundColor method accepts varargs as parameters,
    // we have to cast our result to a String array.
    dataSeries.setBackgroundColor(colors.toArray(new String[0]));
  }

  @Override
  protected void setDefaultElements() {
    final int values = 3; // Number of values to add

    // Get the index of the Data Series to use for default entries, and
    // multiply it by the number of values to use to create an offset
    // TODO: Much like the MockPointChartDataModel does now, this should
    // TODO: take into account the maximum data value in all the data series, since removing Data
    // TODO: components can re-add the same entries to the Pie Chart itself.
    // TODO: This can be kept and applied to the Line Chart Data Model as well if simplicity
    // TODO: is opted for instead.
    int indexOffset = chartData.getDatasets().indexOf(this.dataSeries) * values;

    for (int i = 0; i < values; ++i) {
      // Construct the x and y values based on the data series
      // index offset and the loop index. This allows to differentiate
      // the default entries of each individual Data Series.
      double x = indexOffset + i + 1;
      double y = indexOffset + i + 1;

      // Add an entry based on the constructed values
      addEntryFromTuple(x + "", y + "");
    }
  }

  @Override
  public void addEntryFromTuple(String... tuple) {
    try {
      // First entry is a String
      String x = tuple[0];

      // Second entry is expected to be a double; attempt parsing
      Double y = Double.parseDouble(tuple[1]);

      // Add data entry (this if check is required due to
      // the underlying library implementation of getData)
      if (dataSeries.getData().size() == 0) {
        dataSeries.setData(y);
      } else {
        dataSeries.getData().add(y);
      }

      // Add entry label (x value corresponds to the label)
      labels.add(x);
    } catch (NumberFormatException e) {
      ErrorReporter.reportInfo(MESSAGES.invalidChartDataEntry());
    }
  }

  @Override
  protected String getDefaultTupleEntry(int index) {
    // TODO: In the future, the getDefaultTupleEntry method could have
    // TODO: an additional parameter for the index of the dimension
    // TODO: to return differentiated values. Something like "Entry 1"
    // TODO: could be more suited for the x value, while "1" would be more
    // TODO: suited for the y value.
    return index + "";
  }

  @Override
  public void clearEntries() {
    dataSeries.getData().clear();
    labels.clear(); // Clear the labels (since they represent the x values)
  }

  @Override
  protected void postDataImportAction() {
    // No data points generated, fallback to default option.
    if (dataSeries.getData().isEmpty()) {
      setDefaultElements();
    }

    // After importing the data, the colors have to be changed again
    // (since the new entry count might be different)
    // TODO: possible to optimize by tracking differences
    changeColor(this.color);
  }

  @Override
  protected int getTupleSize() {
    return 2;
  }

  /**
   * Returns a List of entry labels corresponding to the
   * x values of the Data Series in order.
   *
   * @return List of entry labels (Strings)
   */
  public List<String> getLabels() {
    return labels;
  }

  @Override
  public void removeDataSeriesFromChart() {
    // The view has to be notified of the removal of the Data Model
    view.removeDataModel(this);

    // Proceed with general removal from the Chart
    super.removeDataSeriesFromChart();
  }
}
