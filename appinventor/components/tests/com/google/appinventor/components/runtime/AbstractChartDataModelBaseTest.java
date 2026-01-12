// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;

import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Base class for ChartDataMod*el tests.
 *
 * <p>Tests the integration with the MPAndroidChart library
 * classes by actually operating on the Data Series objects.
 */
public abstract class AbstractChartDataModelBaseTest<M extends ChartDataModel<?, ?, ?, ?, ?>,
    D extends ChartData<?>> extends RobolectricTestBase {
  protected M model;
  protected D data;

  @Before
  public abstract void setup();

  /**
   * Test to ensure that the constructor properly instantiates an
   * empty data set, and that the reference of the passed in data
   * object instance is not broken.
   */
  @Test
  public void testConstructor() {
    assertEquals(data, model.getData());
    assertEquals(1, data.getDataSetCount());
    assertEquals(0, data.getDataSetByIndex(0).getEntryCount());
  }

  /**
   * Tests whether the setLabel method correctly changes the label
   * of the Data Series.
   */
  @Test
  public void testSetLabel() {
    String label = "Test Label Text";
    model.setLabel(label);
    assertEquals(label, model.getDataset().getLabel());
  }

  /**
   * Tests whether the setColor method correctly changes the color
   * of the Data Series.
   */
  @Test
  public void testSetColor() {
    int argb = 0xFFEEDDCC;
    model.setColor(argb);
    assertEquals(argb, model.getDataset().getColor());
  }

  /**
   * Tests whether the setColors method correctly changes the colors
   * of the Data Series.
   */
  @Test
  public void testSetColors() {
    List<Integer> colors = new ArrayList<Integer>() {{
        add(0xFFEEDDCC);
        add(0xFFFFFFFF);
        add(0xAABBCCDD);
      }};

    model.setColors(colors);

    assertEquals(colors, model.getDataset().getColors());
  }

  /**
   * Helper method that checks whether all the expected entries
   * are in the data set.
   * For each entry (as well as the size of the entries), an assertion
   * is made.
   *
   * @param expectedEntries list of expected entries
   */
  protected void assertExpectedEntriesHelper(List<Entry> expectedEntries) {
    // Make sure the number of entries parsed is correct
    assertEquals(expectedEntries.size(), model.getEntries().size());

    for (int i = 0; i < model.getEntries().size(); ++i) {
      // Get the entry from the Data Series
      Entry entry = model.getEntries().get(i);

      // Get the expected entry
      Entry expectedEntry = expectedEntries.get(i);

      // Assert that the two entries are equal
      assertEntriesEqual(expectedEntry, entry);
    }
  }

  protected void setElementsHelper(String elements, List<Entry> expectedEntries) {
    model.setElements(elements);
    assertExpectedEntriesHelper(expectedEntries);
  }

  protected void importFromListHelper(List<?> tuples, List<Entry> expectedEntries) {
    YailList pairs = YailList.makeList(tuples);

    // Import the data and assert all the entries
    model.importFromList(pairs);
    assertExpectedEntriesHelper(expectedEntries);
  }

  protected void importFromListGenericHelper(List<?> tuples, List<Entry> expectedEntries) {
    model.importFromList(tuples);
    assertExpectedEntriesHelper(expectedEntries);
  }

  protected void importFromCsvHelper(List<Entry> expectedEntries, YailList... columns) {
    YailList columnList = YailList.makeList(columns);

    model.importFromColumns(columnList, true);
    assertExpectedEntriesHelper(expectedEntries);
  }

  protected void removeEntryFromTupleHelper(List<YailList> tuples, List<Entry> expectedEntries,
                                            YailList deleteTuple) {
    YailList pairs = YailList.makeList(tuples);

    // Import the data, remove the entry and assert all the entries
    model.importFromList(pairs);
    model.removeEntryFromTuple(deleteTuple);
    assertExpectedEntriesHelper(expectedEntries);
  }

  protected void doesEntryExistHelper(List<YailList> tuples, YailList searchTuple,
      boolean expected) {
    YailList pairs = YailList.makeList(tuples);

    model.importFromList(pairs);
    boolean result = model.doesEntryExist(searchTuple);
    assertEquals(expected, result);
  }

  protected void findEntryIndexHelper(List<YailList> tuples, Entry searchEntry, int expectedIndex) {
    YailList pairs = YailList.makeList(tuples);
    model.importFromList(pairs);

    int result = model.findEntryIndex(searchEntry);
    assertEquals(expectedIndex, result);
  }

  protected void getEntriesAsTuplesHelper(List<YailList> tuples) {
    YailList expected = YailList.makeList(tuples);
    model.importFromList(expected);

    YailList result = model.getEntriesAsTuples();
    assertEquals(expected, result);
  }

  protected void removeValuesHelper(List<YailList> tuples, List<Entry> expectedEntries,
                                    List<?> removeEntries) {
    // Import the data
    model.importFromList(tuples);

    // Remove entries
    model.removeValues(removeEntries);

    // Assert expected entries
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Helper method that asserts whether the specified two entries are equal.
   * This is needed because the MPAndroidChart library's equal method checks
   * by hash-code instead.
   *
   * @param e1 Expected Entry
   * @param e2 Actual Entry
   */
  protected abstract void assertEntriesEqual(Entry e1, Entry e2);

  protected YailList createTuple(Object... entries) {
    return YailList.makeList(entries);
  }

  protected abstract Entry createEntry(Object... entries);
}
