package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Base class for Line Chart based Data Model tests.
 */
public abstract class PointChartDataModelTest
    <M extends PointChartDataModel,
    D extends ChartData>
    extends ChartDataModel2DTest<M, D> {
  /**
   * Test to ensure that importing from a tuple with
   * an invalid X value does not add any entry.
   */
  @Test
  public void testAddEntryFromTupleInvalidX() {
    YailList tuple = createTuple("String", 1f);
    model.addEntryFromTuple(tuple);

    assertEquals(0, model.getDataset().getEntryCount());
  }

  /**
   * Test to ensure that importing from a tuple with
   * an invalid Y value does not add any entry.
   */
  @Test
  public void testAddEntryFromTupleInvalidY() {
    YailList tuple = createTuple(0f, "String");
    model.addEntryFromTuple(tuple);

    assertEquals(0, model.getDataset().getEntryCount());
  }

  @Override
  protected Entry createEntry(Object... entries) {
    float x = (float) entries[0];
    float y = (float) entries[1];
    return new Entry(x, y);
  }

  @Override
  protected void assertEntriesEqual(Entry e1, Entry e2) {
    assertEquals(e1.getX(), e2.getX());
    assertEquals(e1.getY(), e2.getY());
    assertEquals(e1.getClass(), e2.getClass());
  }
}