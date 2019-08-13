package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.output.OdeLog;
import org.pepstock.charba.client.data.BarDataset;
import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MockBarChartDataModel extends MockChartDataModel<BarDataset> {
  MockBarChartView view;

  public MockBarChartDataModel(Data chartData, MockBarChartView view) {
    super(chartData);

    this.view = view;

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

    double yVal = chartData.getDatasets().size();

    for (int i = 0; i < points; ++i) {
      // Construct the x and y values based on the index
      double xValue = i;
      double yValue = yVal + i;

      // Add an entry based on the constructed values
      addEntryFromTuple(xValue, yValue);
    }
  }

  @Override
  protected void setDefaultStylingProperties() {

  }

  @Override
  public void addEntryFromTuple(String... tuple) {
    try {
      // Parse x and y values
      double xValue = Double.parseDouble(tuple[0]);
      double yValue = Double.parseDouble(tuple[1]);

      // Add an entry from the parsed values
      addEntryFromTuple(xValue, yValue);
    } catch (NumberFormatException e) {
      // Wrong input. Do nothing.
    }
  }

  /**
   * Adds an entry to the Data Series from the specified tuple.
   *
   * The tuple is expected to have at least 2 entries. All subsequent
   * values are ignored.
   *
   * @param tuple  tuple (array of doubles)
   */
  public void addEntryFromTuple(Double... tuple) {
    int xValue = (int) Math.round(tuple[0]);
    double yValue = tuple[1];

    // If x is less than 0, then skip the insertion, since
    // Bar Chart x values start from 0.
    if (xValue < 0) {
      return;
    }

    if (xValue < dataSeries.getData().size()) {
      dataSeries.getData().set(xValue, yValue);
    } else {
      while (dataSeries.getData().size() < xValue) {
        if (dataSeries.getData().size() == 0) {
          dataSeries.setData(0.0);
        } else {
          dataSeries.getData().add(0.0);
        }
      }

      if (dataSeries.getData().size() == 0) {
        dataSeries.setData(yValue);
      } else {
        dataSeries.getData().add(yValue);
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

    view.updateLabels();
  }

  @Override
  public void removeDataSeriesFromChart() {
    super.removeDataSeriesFromChart();

    view.updateLabels();
  }
}
