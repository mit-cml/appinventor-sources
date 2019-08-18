package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.Arrays;
import java.util.List;

public abstract class Chart2DDataModel<T extends DataSet, D extends ChartData> extends ChartDataModel<T, D> {
  /**
   * Initializes a new Chart2DDataModel object instance.
   *
   * @param data Chart data instance
   */
  protected Chart2DDataModel(D data) {
    super(data);
  }

  @Override
  protected int getTupleSize() {
    return 2;
  }

  @Override
  public YailList getTupleFromEntry(Entry entry) {
    // Create a list with the X and Y values of the entry, and
    // convert the generic List to a YailList
    List tupleEntries = Arrays.asList(entry.getX(), entry.getY());
    return YailList.makeList(tupleEntries);
  }
}
