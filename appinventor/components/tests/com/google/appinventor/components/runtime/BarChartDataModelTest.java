// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Test class for the Bar Chart Data Model.
 * Tests various data operations on Bar data.
 * @see AbstractChartDataModel2DTest
 */
public class BarChartDataModelTest
    extends AbstractChartDataModel2DTest<BarChartDataModel, BarData> {

  @Override
  public void setup() {
    data = new BarData();
    model = new BarChartDataModel(data, new BarChartView(new Chart(getForm())));
  }

  /**
   * Test case to ensure that adding an entry with an x value of 0
   * adds the entry to the Bar Chart as the first (and only) entry.
   */
  @Override
  public void testAddEntryFromTuplePair() {
    final float x = 0f;
    final float y = 10f;
    YailList tuple = createTuple(x, y);

    model.addEntryFromTuple(tuple);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 10f));
      }};

    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test case to ensure that adding an entry with
   * decimal float values properly rounds down
   * the x value on adding the entry.
   */
  @Test
  public void testAddEntryFromTupleRoundDown() {
    final float xValue = 0.4f;
    final float yValue = 4f;
    YailList tuple = createTuple(xValue, yValue);

    model.addEntryFromTuple(tuple);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 4f));
      }};

    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test case to ensure that adding an entry with
   * decimal float values properly rounds up
   * the x value on adding the entry.
   */
  @Test
  public void testAddEntryFromTupleRoundUp() {
    final float xValue = 0.7f;
    final float yValue = 1f;
    YailList tuple = createTuple(xValue, yValue);

    model.addEntryFromTuple(tuple);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 1f));
      }};

    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test case to ensure that adding an Entry with
   * an x value that already exists in the Data Series
   * replaces the existing entry.
   */
  @Test
  public void testAddEntryFromTupleReplaceValue() {
    YailList tuple1 = createTuple(2f, 3f);
    YailList tuple2 = createTuple(3f, 1f);

    model.addEntryFromTuple(tuple1);
    model.addEntryFromTuple(tuple2);

    ArrayList<Entry> expectedEntries1 = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
        add(createEntry(1f, 0f));
        add(createEntry(2f, 3f));
        add(createEntry(3f, 1f));
      }};

    assertExpectedEntriesHelper(expectedEntries1);

    YailList tuple3 = createTuple(2f, 10f);
    model.addEntryFromTuple(tuple3);

    ArrayList<Entry> expectedEntries2 = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
        add(createEntry(1f, 0f));
        add(createEntry(2f, 10f));
        add(createEntry(3f, 1f));
      }};

    assertExpectedEntriesHelper(expectedEntries2);
  }

  /**
   * Test case to ensure that adding an Entry with
   * an x value which corresponds to the x value
   * of the last Entry successfully replaces the
   * last Entry.
   */
  @Test
  public void testAddEntryFromTupleReplaceLastValue() {
    YailList tuple1 = createTuple(1f, 5f);
    YailList tuple2 = createTuple(4f, 7f);

    model.addEntryFromTuple(tuple1);
    model.addEntryFromTuple(tuple2);

    ArrayList<Entry> expectedEntries1 = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
        add(createEntry(1f, 5f));
        add(createEntry(2f, 0f));
        add(createEntry(3f, 0f));
        add(createEntry(4f, 7f));
      }};

    assertExpectedEntriesHelper(expectedEntries1);

    YailList tuple3 = createTuple(4f, 15f);
    model.addEntryFromTuple(tuple3);

    ArrayList<Entry> expectedEntries2 = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
        add(createEntry(1f, 5f));
        add(createEntry(2f, 0f));
        add(createEntry(3f, 0f));
        add(createEntry(4f, 15f));
      }};

    assertExpectedEntriesHelper(expectedEntries2);
  }

  /**
   * Test case to ensure that attempting to add an Entry
   * with a negative x value simply does not add the Entry.
   */
  @Test
  public void testAddEntryFromTupleNegativeXValue() {
    final float xValue = -3f;
    final float yValue = 12f;
    YailList tuple = createTuple(xValue, yValue);

    model.addEntryFromTuple(tuple);

    ArrayList<Entry> expectedEntries = new ArrayList<>();
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test case to ensure that adding a tuple which has
   * an x value that is higher than the current largest
   * x value fills the gaps with 0 y values.
   */
  @Test
  public void testAddEntryFromTupleFillBlankValues() {
    final float xValue = 9f;
    final float yValue = 3f;
    YailList tuple = createTuple(xValue, yValue);

    model.addEntryFromTuple(tuple);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
        add(createEntry(1f, 0f));
        add(createEntry(2f, 0f));
        add(createEntry(3f, 0f));
        add(createEntry(4f, 0f));
        add(createEntry(5f, 0f));
        add(createEntry(6f, 0f));
        add(createEntry(7f, 0f));
        add(createEntry(8f, 0f));
        add(createEntry(9f, 3f));
      }};

    assertExpectedEntriesHelper(expectedEntries);
  }

  @Override
  public void testFindEntryIndexExists() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(3f, 1f));
        add(createTuple(4f, 6f));
        add(createTuple(6f, -1f));
        add(createTuple(7f, 3f));
      }};

    Entry searchEntry = createEntry(7f, 3f);
    final int expectedIndex = 7;

    model.importFromList(tuples);

    findEntryIndexHelper(tuples, searchEntry, expectedIndex);
  }

  @Override
  public void testImportFromCsvUnevenColumns() {
    YailList xcolumn = createTuple("X", 0f, 4f, 5f, 6f);
    YailList ycolumn = createTuple("Y", -3f, 5f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, -3f));
        add(createEntry(1f, 0f));
        add(createEntry(2f, 0f));
        add(createEntry(3f, 0f));
        add(createEntry(4f, 5f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  @Override
  public void testImportFromCsvManyEntries() {
    YailList xcolumn = createTuple("X", 1f, 3f, 6f, 7f);
    YailList ycolumn = createTuple("Y", 10f, 5f, 3f, 9f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
        add(createEntry(1f, 10f));
        add(createEntry(2f, 0f));
        add(createEntry(3f, 5f));
        add(createEntry(4f, 0f));
        add(createEntry(5f, 0f));
        add(createEntry(6f, 3f));
        add(createEntry(7f, 9f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  @Override
  public void testImportFromCsvOneEntry() {
    YailList xcolumn = createTuple("X", 4f);
    YailList ycolumn = createTuple("Y", 1f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
        add(createEntry(1f, 0f));
        add(createEntry(2f, 0f));
        add(createEntry(3f, 0f));
        add(createEntry(4f, 1f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  @Override
  public void testImportFromListDuplicates() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 5f));
        add(createTuple(0f, 4f));
        add(createTuple(0f, 9f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 9f));
      }};

    importFromListHelper(tuples, expectedEntries);
  }

  @Override
  public void testImportFromListMultipleEntries() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 4f));
        add(createTuple(4f, 2f));
        add(createTuple(6f, -5f));
        add(createTuple(7f, 3f));
        add(createTuple(3f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
        add(createEntry(1f, 4f));
        add(createEntry(2f, 0f));
        add(createEntry(3f, 2f));
        add(createEntry(4f, 2f));
        add(createEntry(5f, 0f));
        add(createEntry(6f, -5f));
        add(createEntry(7f, 3f));
      }};

    importFromListHelper(tuples, expectedEntries);
  }

  /**
   * Test case to ensure that removing an Entry which is
   * not the last Entry simply sets the y value of the Entry
   * to 0.
   */
  @Override
  public void testRemoveFromTupleExists() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
        add(createTuple(1f, 7f));
        add(createTuple(3f, 5f)); // The entry to be removed
        add(createTuple(5f, 4f));
      }};
    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 7f));
        add(createEntry(2f, 0f));
        add(createEntry(3f, 0f));
        add(createEntry(4f, 0f));
        add(createEntry(5f, 4f));
      }};

    YailList deleteTuple = createTuple(3f, 5f);

    removeEntryFromTupleHelper(tuples, expectedEntries, deleteTuple);
  }

  /**
   * Test case to ensure that removing an Entry which is
   * the last Entry actually removes the Entry.
   */
  @Test
  public void testRemoveFromTupleLast() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
        add(createTuple(2f, 4f));
        add(createTuple(3f, 1f));
        add(createTuple(4f, 5f)); // The entry to be removed
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 0f));
        add(createEntry(2f, 4f));
        add(createEntry(3f, 1f));
      }};

    YailList deleteTuple = createTuple(4f, 5f);

    removeEntryFromTupleHelper(tuples, expectedEntries, deleteTuple);
  }

  @Override
  public void testRemoveFromTupleMultipleEntries() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 1f));
        add(createTuple(1f, 3f));
        add(createTuple(1f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
      }};

    YailList deleteTuple = createTuple(1f, 2f);

    removeEntryFromTupleHelper(tuples, expectedEntries, deleteTuple);
  }

  @Override
  public void testRemoveValuesNonExistentValues() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, -1f));
        add(createTuple(2f, 2f));
        add(createTuple(4f, 4f));
        add(createTuple(5f, -3f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, -1f));
        add(createEntry(1f, 0f));
        add(createEntry(2f, 2f));
        add(createEntry(3f, 0f));
      }};

    // Remove entries
    List<YailList> removeEntries = new ArrayList<YailList>() {{
        add(createTuple(5f, -3f));
        add(createTuple(4f, 4f));
        add(createTuple(3f, 1f)); // Does not exist
        add(createTuple(2f, 3f)); // Does not exist
      }};

    removeValuesHelper(tuples, expectedEntries, removeEntries);
  }

  @Override
  public void testRemoveValuesSingleValue() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, -3f));
        add(createTuple(2f, 4f));
        add(createTuple(4f, 5f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 0f));
        add(createEntry(1f, -3f));
        add(createEntry(2f, 4f));
        add(createEntry(3f, 0f));
      }};

    // Remove entries
    List<List<Float>> removeEntries = new ArrayList<List<Float>>() {{
        add(Arrays.asList(4f, 5f));
      }};

    removeValuesHelper(tuples, expectedEntries, removeEntries);
  }

  @Override
  public void testRemoveValuesMultipleValues() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 0f));
        add(createTuple(1f, 3f));
        add(createTuple(3f, 7f));
        add(createTuple(5f, 6f));
        add(createTuple(6f, 4f));
        add(createTuple(8f, 3f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f,0f));
        add(createEntry(1f,0f));
        add(createEntry(2f,0f));
        add(createEntry(3f,7f));
        add(createEntry(4f,0f));
        add(createEntry(5f,0f));
        add(createEntry(6f,4f));
        add(createEntry(7f,0f));
      }};

    // Remove entries
    List<YailList> removeEntries = new ArrayList<YailList>() {{
        add(createTuple(1f, 3f));
        add(createTuple(5f, 6f));
        add(createTuple(8f, 3f));
      }};

    removeValuesHelper(tuples, expectedEntries, removeEntries);
  }

  /**
   * Test to ensure that importing from columns of uneven
   * size imports all the entries while replacing the
   * blank entries in other Lists with default values.
   */
  @Test
  public void testImportFromCsvUnevenColumnsBlankEntries() {
    YailList xcolumn = createTuple("X", 0f, 1f, "", "");
    YailList ycolumn = createTuple("Y", 1f, 3f, 5f, -4f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 1f));
        add(createEntry(1f, 3f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
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
    YailList ycolumn = createTuple("Y", 1f, 3f, 4f, -3f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 1f));
        add(createEntry(1f, 3f));
        add(createEntry(2f, 4f));
        add(createEntry(3f, -3f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * X Value criterion and an integer x value while checking against an
   * Entry which has a decimal x value returns true (since x value is floored).
   */
  @Test
  public void testCriterionSatisfiedXDecimalMatch() {
    Entry entry = createEntry(1.7f, 4f);
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.XValue;
    final String value = "1";

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value);
    assertTrue(result);
  }

  /**
   * Test case to ensure that retrieving a tuple from an
   * Entry which has a decimal x value returns a tuple
   * with a rounded x value.
   */
  @Test
  public void testGetTupleFromEntryDecimal() {
    Entry entry = createEntry(4.75f, 1f);

    YailList expected = createTuple(4f, 1f);
    YailList result = model.getTupleFromEntry(entry);

    assertEquals(expected, result);
  }

  /**
   * Test case to ensure that checking for Entry
   * equality between two entries which have the
   * same y value and x values with differing
   * decimal parts returns true (since values are floored).
   */
  @Test
  public void testEntriesEqualDecimalXValues() {
    Entry entry1 = createEntry(3.3f, 5f);
    Entry entry2 = createEntry(3.7f, 5f);

    assertTrue(model.areEntriesEqual(entry1, entry2));
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
