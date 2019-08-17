package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class PointChartDataModel<T extends BarLineScatterCandleBubbleDataSet,
    D extends BarLineScatterCandleBubbleData> extends Chart2DDataModel<T, D> {
  /**
   * Initializes a new PointChartDataModel object instance.
   *
   * @param data Chart data instance
   */
  protected PointChartDataModel(D data) {
    super(data);
    entries = new ArrayList<Entry>();
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

//  @Override
//  protected YailList getDefaultValues(int size) {
//    // Default values for Point Chart Data Models should be
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
