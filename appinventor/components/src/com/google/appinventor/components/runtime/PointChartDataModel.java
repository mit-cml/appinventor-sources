package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;

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

  public abstract void addEntry(float x, float y);

  @Override
  protected int getTupleSize() {
    return 2;
  }

  @Override
  public void addEntryFromTuple(YailList tuple) {
    try {
      String xValue = tuple.getString(0);
      String yValue = tuple.getString(1);

      try {
        float x = Float.parseFloat(xValue);
        float y = Float.parseFloat(yValue);

        addEntry(x, y);
      } catch (NumberFormatException e) {
        // Nothing happens: Do not add entry on NumberFormatException
      }
    } catch (Exception e) {
      // 2-tuples are invalid when null entries are present, or if
      // the number of entries is not sufficient to form a pair.
      // TODO: Show toast error notification
    }
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
