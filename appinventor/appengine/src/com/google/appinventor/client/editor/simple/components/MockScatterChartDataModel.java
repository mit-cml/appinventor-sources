package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.DataPoint;

import java.util.ArrayList;
import java.util.Comparator;

public class MockScatterChartDataModel extends MockPointChartDataModel {
  /**
   * Creates a new Mock Scatter Chart Data Model object instance, linking it with
   * the Data object of a specific Chart.
   *
   * @param chartData Chart Data object to link to
   */
  protected MockScatterChartDataModel(Data chartData) {
    super(chartData);
  }

  @Override
  protected void setDefaultStylingProperties() {
    dataSeries.setShowLine(false);
    dataSeries.setBorderWidth(1);
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
    }

    // Set the generated data points to the Data Series
    dataSeries.setDataPoints(dataPoints);
  }
}
