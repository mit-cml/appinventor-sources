package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;

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

  }

  @Override
  public Entry getEntryFromTuple(YailList tuple) {
    return new BarEntry(1f, 1f);
  }

  @Override
  public YailList getTupleFromEntry(Entry entry) {
    return new YailList();
  }

  @Override
  protected void setDefaultStylingProperties() {

  }

  @Override
  protected YailList getDefaultValues(int size) {
    return null;
  }

  @Override
  protected boolean areEntriesEqual(Entry e1, Entry e2) {
    return false;
  }
}
