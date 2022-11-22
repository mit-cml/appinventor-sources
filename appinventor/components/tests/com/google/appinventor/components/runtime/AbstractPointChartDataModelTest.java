// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static junit.framework.Assert.assertEquals;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;

import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;

import org.junit.Test;

/**
 * Base class for Line Chart based Data Model tests.
 *
 * @param <M> PointChartDataModel class type under test
 * @param <D> ChartData type under test (should be same type as the M ChartData type used)
 */
public abstract class AbstractPointChartDataModelTest
    <M extends PointChartDataModel<?, ?, ?, ?, ?>,
        D extends ChartData<?>>
    extends AbstractChartDataModel2DTest<M, D> {
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
  public void testImportFromCsvEmptyColumn() {
    YailList xcolumn = createTuple();
    YailList ycolumn = createTuple("Y", 3f, 5f, -3f, 7f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 5f));
        add(createEntry(2f, -3f));
        add(createEntry(3f, 7f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  /**
   * Test to ensure that importing from columns of uneven
   * size imports all the entries while replacing the
   * blank entries in other Lists with default values.
   */
  @Test
  public void testImportFromCsvUnevenColumnsBlankEntries() {
    YailList xcolumn = createTuple("X", 1f, 2f, "", "");
    YailList ycolumn = createTuple("Y", 2f, 5f, 7f, 10f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(1f, 2f));
        add(createEntry(2f, 5f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  /**
   * Test to ensure that specifying an existing Entry (of which there are duplicates)
   * to the findEntryIndex method  returns the first found entry's index.
   */
  @Test
  public void testFindEntryIndexExistsMultiple() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 3f));
        add(createTuple(1f, 2f));
        add(createTuple(2f, 1f));
        add(createTuple(2f, 3f));
        add(createTuple(2f, 3f));
        add(createTuple(3f, 4f));
      }};

    Entry searchEntry = createEntry(2f, 3f);
    final int expectedIndex = 3;

    findEntryIndexHelper(tuples, searchEntry, expectedIndex);
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
