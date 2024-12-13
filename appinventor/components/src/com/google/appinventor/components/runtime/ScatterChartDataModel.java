// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import com.google.appinventor.components.common.PointStyle;

import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Handles the data operations & model-specific styling for Scatter
 * Chart data for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartDataModel
 */
public class ScatterChartDataModel extends PointChartDataModel<
    Entry, IScatterDataSet, ScatterData, ScatterChart, ScatterChartView> {
  /**
   * Initializes a new ScatterChartDataModel object instance.
   *
   * @param data Chart data instance
   * @param view Scatter Chart View to link model to
   */
  public ScatterChartDataModel(ScatterData data, ScatterChartView view) {
    this(data, view, new ScatterDataSet(new ArrayList<Entry>(), ""));
  }

  protected ScatterChartDataModel(ScatterData data, ScatterChartView view,
      IScatterDataSet dataset) {
    super(data, view);
    this.dataset = dataset;
    this.data.addDataSet(dataset); // Safe add
    setDefaultStylingProperties();
  }

  @Override
  public void addEntryFromTuple(YailList tuple) {
    // Construct an entry from the prvoided tuple
    Entry entry = getEntryFromTuple(tuple);

    // If entry constructed successfully, add it to the Data Series
    if (entry != null) {
      /* TODO: The commented out line should be used, however, it breaks in certain cases.
         When this is fixed in MPAndroidChart, this method should use the commented method instead
         of the current implementation.
         See: https://github.com/PhilJay/MPAndroidChart/issues/4616
      */
      // getDataset().addEntryOrdered(entry);


      // In Line Chart based data series, the data is already pre-sorted.
      // We can thus run binary search by comparing with the x value, and
      // using an x+1 value to find the insertion point
      int index = Collections.binarySearch(entries, // Use the list of entries
          entry, // Search for the same x value as the entry to be added
          new EntryXComparator()); // Compare by x value

      // Value not found: insertion point can be derived from it
      if (index < 0) {
        // result is (-(insertion point) - 1)
        index = -index - 1;
      } else {
        // Get the entry count of the Data Set
        int entryCount = entries.size();

        // Iterate until an entry with a differing (higher) x value is found (this
        // is where the value should be inserted)
        // The reason for a loop is to pass through all the duplicate entries.
        while (index < entryCount && entries.get(index).getX() == entry.getX()) {
          index++;
        }
      }

      entries.add(index, entry);
    }
  }

  @Override
  protected void setDefaultStylingProperties() {
    if (dataset instanceof ScatterDataSet) {
      ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.CIRCLE);
    }
  }

  /**
   * Changes the Point Shape of the Scatter Data Series.
   *
   * @param shape the desired point style
   */
  public void setPointShape(PointStyle shape) {
    if (!(dataset instanceof ScatterDataSet)) {
      return;
    }
    switch (shape) {
      case Circle:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        break;

      case Square:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.SQUARE);
        break;

      case Triangle:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.TRIANGLE);
        break;

      case Cross:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.CROSS);
        break;

      case X:
        ((ScatterDataSet) dataset).setScatterShape(ScatterChart.ScatterShape.X);
        break;

      default:
        throw new IllegalArgumentException("Unknown shape type: " + shape);
    }
  }
}
