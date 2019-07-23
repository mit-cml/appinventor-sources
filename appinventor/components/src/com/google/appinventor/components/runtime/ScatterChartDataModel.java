package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;

public class ScatterChartDataModel extends PointChartDataModel<ScatterDataSet, ScatterData> {
  /**
   * Initializes a new ScatterChartDataModel object instance.
   *
   * @param data Chart data instance
   */
  public ScatterChartDataModel(ScatterData data) {
    super(data);
    dataset = new ScatterDataSet(new ArrayList<Entry>(), "");
    this.data.addDataSet(dataset); // Safe add
    setDefaultStylingProperties();
  }

  @Override
  public void addEntry(float x, float y) {
    Entry entry = new Entry(x, y);
    getDataset().addEntry(entry);
  }

  @Override
  protected void setDefaultStylingProperties() {
    getDataset().setScatterShape(ScatterChart.ScatterShape.CIRCLE);
  }
}
