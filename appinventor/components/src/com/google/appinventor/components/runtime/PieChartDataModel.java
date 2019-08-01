package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;

public class PieChartDataModel extends ChartDataModel<PieDataSet, PieData> {

  /**
   * Initializes a new PieChartDataModel object instance.
   *
   * Links the Data Model to the specified Chart, since one
   * Pie Chart instance represents a single ring of a Pie Chart.
   *
   * @param chart  Chart to link Data Model
   * @param data Chart data instance
   */
  protected PieChartDataModel(PieChart chart, PieData data) {
    super(data);
    dataset = new PieDataSet(new ArrayList<PieEntry>(), "");
    this.data.addDataSet(dataset);
    chart.setData(data);
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
  public void removeEntryFromTuple(YailList tuple) {

  }

  @Override
  public Entry getEntryFromTuple(YailList tuple) {
    return new PieEntry(1);
  }

  @Override
  public YailList getTupleFromEntry(Entry entry) {
    return new YailList();
  }

  @Override
  protected int findEntryIndex(Entry entry) {
    return 0;
  }

  @Override
  protected void setDefaultStylingProperties() {

  }

  @Override
  protected YailList getDefaultValues(int size) {
    return new YailList();
  }
}
