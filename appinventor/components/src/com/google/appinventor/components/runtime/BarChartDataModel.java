package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BarChartDataModel extends ChartDataModel<BarDataSet, BarData> {
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
  protected int getTupleSize() {
    return 2;
  }

  @Override
  public void addEntryFromTuple(YailList tuple) {
    // Construct a Bar Entry from the provided tuple
    BarEntry entry = (BarEntry) getEntryFromTuple(tuple); // safe cast

    // If entry constructed successfully, add it to the Data Series
    if (entry != null) {
      int xValue = (int)entry.getX();

      if (xValue < getDataset().getEntryCount()) {
        getDataset().getValues().set(xValue, entry);
      } else {
        while (getDataset().getEntryCount() < xValue) {
          getDataset().addEntry(new BarEntry(0, 0));
        }

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
        // Attempt to parse the x and y value String representations
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
  public YailList getTupleFromEntry(Entry entry) {
    // Create a list with the X and Y values of the entry, and
    // convert the generic List to a YailList
    List tupleEntries = Arrays.asList(entry.getX(), entry.getY());
    return YailList.makeList(tupleEntries);
  }

  @Override
  protected void setDefaultStylingProperties() {

  }

  @Override
  protected YailList getDefaultValues(int size) {
    // Default values for Bar Chart Data Models should be
    // integers from 0 to N (0, 1, 2, ...)
    ArrayList<Integer> defaultValues = new ArrayList<>();

    for (int i = 0; i < size; ++i) {
      defaultValues.add(i);
    }

    return YailList.makeList(defaultValues);
  }

  @Override
  protected boolean areEntriesEqual(Entry e1, Entry e2) {
    return e1.equalTo(e2);
  }
}
