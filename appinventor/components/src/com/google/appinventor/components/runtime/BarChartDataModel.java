package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BarChartDataModel extends Chart2DDataModel<BarDataSet, BarData> {
  /**
   * Initializes a new ChartDataModel object instance.
   *
   * @param data Chart data instance
   */
  protected BarChartDataModel(BarData data) {
    super(data);
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
      int xValue = (int)entry.getX();

      // To ensure the two properties of the Bar Chart entries
      // (one of which is the property where entries are sorted
      // in ascending order by x values, where the difference between
      // subsequent x values is always 1, and the other which is
      // that x values correspond to an index), we need additional
      // logic for entry insertion.

      // Negative x value is an invalid input (negative index);
      // Skip entry adding.
      if (xValue < 0) {
        return;
      }

      // X Value is less than the entry count of the Data Series;
      // This means that the value already exists
      if (xValue < getDataset().getEntryCount()) {
        // Use x value as index and update the entry in that position
        getDataset().getValues().set(xValue, entry);
      } else {
        // To ensure that the x value would correspond to
        // the index, missing values up until the x value
        // need to be filled (with 0 values)
        while (getDataset().getEntryCount() < xValue) {
          getDataset().addEntry(new BarEntry(getDataset().getEntryCount(), 0));
        }

        // Add the entry to the Data Series; Since we
        // took care of missing values, this will now guarantee
        // that the x value corresponds to the last index of
        // the Data Series (equal to entryCount - 1)
        getDataset().addEntry(entry);
      }
    }
  }

  @Override
  public Entry getEntryFromTuple(YailList tuple) {
    try {
      // Tuple is expected to have at least 2 entries.
      // The first entry is assumed to be the x value, and
      // the second is assumed to be the y value.
      String xValue = tuple.getString(0);
      String yValue = tuple.getString(1);

      try {
        // Attempt to parse the x and y value String representations.
        // Since the Bar Chart uses x entries as an index (so an
        // x value of 3 would correspond to the 4th entry [3rd index],
        //the float value has to be rounded to an integer.
        int x = Math.round(Float.parseFloat(xValue));
        float y = Float.parseFloat(yValue);

        return new BarEntry(x, y);
      } catch (NumberFormatException e) {
        // Nothing happens: Do not add entry on NumberFormatException
      }
    } catch (Exception e) {
      // 2-tuples are invalid when null entries are present, or if
      // the number of entries is not sufficient to form a pair.
      // TODO: Show toast error notification
    }

    return null;
  }

  @Override
  protected void setDefaultStylingProperties() {

  }

  @Override
  public void removeEntryFromTuple(YailList tuple) {
    // Construct an entry from the specified tuple
    Entry entry = getEntryFromTuple(tuple);

    if (entry != null) {
      // Get the index of the entry
      int index = findEntryIndex(entry);

      // Entry exists; remove it
      if (index >= 0) {
        // If the index of the Entry to remove is the last Entry,
        // we can simply remove the Entry from the Data Series' values.
        if (index == getDataset().getEntryCount() - 1) {
          getDataset().getValues().remove(index);
        } else {
          // If the Entry to be removed is not the last Entry, we
          // have to instead set the Y value of the Entry to be
          // removed to 0 in order to preserve the Bar Chart Data
          // properties (x values sorted and correspond to indexes)
          getDataset().getValues().get(index).setY(0f);
        }
      }
    }
  }

  // TODO: FindEntryIndex can be optimized by making use of the property
  // TODO: that the x value corresponds to the index.

//  @Override
//  protected YailList getDefaultValues(int size) {
//    // Default values for Bar Chart Data Models should be
//    // integers from 0 to N (0, 1, 2, ...)
//    ArrayList<Integer> defaultValues = new ArrayList<>();
//
//    for (int i = 0; i < size; ++i) {
//      defaultValues.add(i);
//    }
//
//    return YailList.makeList(defaultValues);
//  }
}
