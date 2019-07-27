package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.List;

public abstract class PointChartDataModel<T extends BarLineScatterCandleBubbleDataSet,
    D extends BarLineScatterCandleBubbleData> extends ChartDataModel<T, D>{
  /**
   * Initializes a new PointChartDataModel object instance.
   *
   * @param data Chart data instance
   */
  protected PointChartDataModel(D data) {
    super(data);
  }

  @Override
  protected int getTupleSize() {
    return 2;
  }

  @Override
  public Entry getEntryFromTuple(YailList tuple) {
    try {
      String xValue = tuple.getString(0);
      String yValue = tuple.getString(1);

      try {
        float x = Float.parseFloat(xValue);
        float y = Float.parseFloat(yValue);

        return new Entry(x, y);
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
  @SuppressWarnings("unchecked")
  public void removeEntryFromTuple(YailList tuple) {
    // Construct an entry from the specified tuple
    Entry entry = getEntryFromTuple(tuple);

    if (entry != null) {
      // TODO: The commented line should be used instead. However, the library does not (yet) implement
      // TODO: equals methods in it's entries as of yet, so the below method fails.
      // dataset.removeEntry(entry);

      // Get the index of the entry
      int index = findEntryIndex(entry);

      // Entry exists; remove it
      if (index >= 0) {
        getDataset().removeEntry(index);
      }
    }
  }

  @Override
  protected int findEntryIndex(Entry entry) {
    for (int i = 0; i < getDataset().getValues().size(); ++i) {
      Entry currentEntry = getDataset().getEntryForIndex(i);

      // Check whether the current entry is equal to the
      // specified entry. Note that (in v3.1.0), equals()
      // does not yield the same result.
      if (currentEntry.equalTo(entry)) {
        // Entry matched; Return
        return i;
      }
    }

    return -1;
  }

  @Override
  protected YailList getDefaultValues(int size) {
    // Default values for LineChartBaseDataModel should be
    // integers from 0 to N (0, 1, 2, ...)
    ArrayList<Integer> defaultValues = new ArrayList<>();

    for (int i = 0; i < size; ++i) {
      defaultValues.add(i);
    }

    return YailList.makeList(defaultValues);
  }
}
