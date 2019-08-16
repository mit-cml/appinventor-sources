package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import static junit.framework.Assert.assertEquals;

public class BarChartDataModelTest extends ChartDataModel2DTest<BarChartDataModel, BarData> {

  @Override
  public void setup() {
    data = new BarData();
    model = new BarChartDataModel(data);
  }

  @Override
  protected void assertEntriesEqual(Entry e1, Entry e2) {
    assertEquals(e1.getX(), e2.getX());
    assertEquals(e1.getY(), e2.getY());
    assertEquals(e1.getClass(), e2.getClass());
  }

  @Override
  protected Entry createEntry(Object... entries) {
    float x = (float) entries[0];
    float y = (float) entries[1];
    return new BarEntry(x, y);
  }
}