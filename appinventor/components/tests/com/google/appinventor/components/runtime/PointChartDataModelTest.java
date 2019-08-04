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
   * Test to ensure that importing from an x Column which is
   * empty and a Y column which has values results in the
   * x values to resolve to the default option (1 for first entry,
   * 2 for second, ...)
   */
  @Test
  public void testImportFromCSVEmptyColumn() {
    YailList xColumn = createTuple();
    YailList yColumn = createTuple("Y", 3f, 5f, -3f, 7f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(createEntry(1f, 3f));
      add(createEntry(2f, 5f));
      add(createEntry(3f, -3f));
      add(createEntry(4f, 7f));
    }};

    importFromCSVHelper(expectedEntries, xColumn, yColumn);
  }

  /**
   * Test to ensure that comparing two entries which
   * have the same x and y values returns true via
   * the areEntriesEqual method.
   */
  @Test
  public void testEntriesEqual() {
    Entry entry1 = createEntry(1f, 3f);
    Entry entry2 = createEntry(1f, 3f);

    assertTrue(model.areEntriesEqual(entry1, entry2));
  }

  /**
   * Test to ensure that comparing two entries which
   * have the same x but different y values returns false via
   * the areEntriesEqual method.
   */
  @Test
  public void testEntriesNotEqualY() {
    Entry entry1 = createEntry(3f, 5f);
    Entry entry2 = createEntry(3f, 7f);

    assertFalse(model.areEntriesEqual(entry1, entry2));
  }

  /**
   * Test to ensure that comparing two entries which
   * have the same y but different x values returns false via
   * the areEntriesEqual method.
   */
  @Test
  public void testEntriesNotEqualX() {
    Entry entry1 = createEntry(4f, 2f);
    Entry entry2 = createEntry(1f, 2f);

    assertFalse(model.areEntriesEqual(entry1, entry2));
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