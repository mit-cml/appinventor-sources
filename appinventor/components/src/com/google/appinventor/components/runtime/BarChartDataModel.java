// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the data operations & model-specific styling for Bar
 * Chart data for the Chart component.
 *
 * @see com.google.appinventor.components.runtime.ChartDataModel
 */
// TODO: The data model's operations can be optimized further to O(1) instead of O(n)
// TODO: (mainly for FindEntryIndex) by making use of the property that the
// TODO: x value corresponds to the index in the Bar Chart Data Model.
public class BarChartDataModel
    extends Chart2DDataModel<BarEntry, IBarDataSet, BarData, BarChart, BarChartView> {
  /**
   * Initializes a new ChartDataModel object instance.
   *
   * @param data Chart data instance
   * @param view Bar Chart View to link model to
   */
  protected BarChartDataModel(BarData data, BarChartView view) {
    super(data, view);
    dataset = new BarDataSet(new ArrayList<BarEntry>(), "");
    this.data.addDataSet(dataset); // Safe add
    setDefaultStylingProperties();
  }

  @Override
  public void addEntryFromTuple(YailList tuple) {
    // Construct a Bar Entry from the provided tuple
    BarEntry entry = (BarEntry) getEntryFromTuple(tuple); // safe cast

    // If entry constructed successfully, add it to the Data Series
    if (entry != null) {
      // Since Bar Chart entries use x values as indexes (which
      // are integers), we need to cast the entry's x value to an integer.
      int x = (int)entry.getX();

      // To ensure the two properties of the Bar Chart entries
      // (one of which is the property where entries are sorted
      // in ascending order by x values, where the difference between
      // subsequent x values is always 1, and the other which is
      // that x values correspond to an index), we need additional
      // logic for entry insertion.

      // Negative x value is an invalid input (negative index);
      // Skip entry adding.
      if (x < 0) {
        return;
      }

      // X Value is less than the entry count of the Data Series;
      // This means that the value already exists
      if (x < entries.size()) {
        // Use x value as index and update the entry in that position
        entries.set(x, entry);
      } else {
        // To ensure that the x value would correspond to
        // the index, missing values up until the x value
        // need to be filled (with 0 values)
        while (entries.size() < x) {
          entries.add(new BarEntry(entries.size(), 0));
        }

        // Add the entry to the Data Series; Since we
        // took care of missing values, this will now guarantee
        // that the x value corresponds to the last index of
        // the Data Series (equal to entryCount - 1)
        entries.add(entry);
      }
    }
  }

  @Override
  public Entry getEntryFromTuple(YailList tuple) {
    try {
      // Tuple is expected to have at least 2 entries.
      // The first entry is assumed to be the x value, and
      // the second is assumed to be the y value.
      String rawX = tuple.getString(0);
      String rawY = tuple.getString(1);

      try {
        // Attempt to parse the x and y value String representations.
        // Since the Bar Chart uses x entries as an index (so an
        // x value of 3 would correspond to the 4th entry [3rd index],
        // the float value has to be floored.
        int x = (int)Math.floor(Float.parseFloat(rawX));
        float y = Float.parseFloat(rawY);

        return new BarEntry(x, y);
      } catch (NumberFormatException e) {
        // Nothing happens: Do not add entry on NumberFormatException
        this.view.getForm().dispatchErrorOccurredEvent(this.view.chartComponent,
            "GetEntryFromTuple",
            ErrorMessages.ERROR_INVALID_CHART_ENTRY_VALUES,
            rawX, rawY);
      }
    } catch (NullPointerException e) {
      this.view.getForm().dispatchErrorOccurredEvent(this.view.chartComponent,
          "GetEntryFromTuple",
          ErrorMessages.ERROR_NULL_CHART_ENTRY_VALUES);
    } catch (IndexOutOfBoundsException e) {
      this.view.getForm().dispatchErrorOccurredEvent(this.view.chartComponent,
          "GetEntryFromTuple",
          ErrorMessages.ERROR_INSUFFICIENT_CHART_ENTRY_VALUES,
          getTupleSize(), tuple.size());
    }

    return null;
  }

  @Override
  public void removeEntry(int index) {
    // Entry exists; remove it
    if (index >= 0) {
      // If the index of the Entry to remove is the last Entry,
      // we can simply remove the Entry from the Data Series' values.
      if (index == entries.size() - 1) {
        entries.remove(index);
      } else {
        // If the Entry to be removed is not the last Entry, we
        // have to instead set the Y value of the Entry to be
        // removed to 0 in order to preserve the Bar Chart Data
        // properties (x values sorted and correspond to indexes)
        entries.get(index).setY(0f);
      }
    }
  }

  @Override
  public void addTimeEntry(YailList tuple) {
    // TODO: Currently, this implementation breaks the sorted x value
    // TODO: property (the START X VALUE is no longer 0). A potential
    // TODO: fix could be shifting the values instead of removing them,
    // TODO: zeroing out values on removal (which would result in too
    // TODO: many entries, however) or by handling the case where
    // TODO: data adding is added after/during Real Time Data import.

    // If the entry count of the Data Series entries exceeds
    // the maximum allowed time entries, then remove the first one
    if (entries.size() >= maximumTimeEntries) {
      entries.remove(0);
    }

    entries.add((BarEntry)getEntryFromTuple(tuple));
  }

  @Override
  protected boolean areEntriesEqual(Entry e1, Entry e2) {
    // To avoid (unlikely) cast exceptions, check that
    // both the entries are of instance BarEntry, and return
    // false if that is not the case.
    if (!(e1 instanceof BarEntry && e2 instanceof BarEntry)) {
      return false;
    }

    // Due to Bar grouping affecting the x values of the
    // entries, we need to floor the entries to get rid
    // of the decimal part. The offset is guaranteed to be
    // less than 1 (based on the source code and the chosen
    // granularity and spacing)
    return Math.floor(e1.getX()) == Math.floor(e2.getX())
        && e1.getY() == e2.getY();
  }

  @Override
  public YailList getTupleFromEntry(Entry entry) {
    // Create a list with the X and Y values of the entry, and
    // convert the generic List to a YailList. Since Bar Chart
    // grouping adds offsets to the x value (which are expected
    // to be below 1), the x value needs to be floored.
    List<Float> tupleEntries = Arrays.asList((float)Math.floor(entry.getX()), entry.getY());
    return YailList.makeList(tupleEntries);
  }
}
