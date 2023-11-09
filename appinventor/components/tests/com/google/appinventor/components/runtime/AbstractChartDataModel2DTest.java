// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;

import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public abstract class AbstractChartDataModel2DTest<M extends Chart2DDataModel<?, ?, ?, ?, ?>,
    D extends ChartData<?>>
    extends AbstractChartDataModelBaseTest<M, D> {
  /**
   * Tests to ensure that Data Series entries are not changed
   * when passing in invalid input via setEelements.
   */
  @Test
  public void testSetElementsInvalid() {
    String elements = "0.0,2.0,1.0,4.0,A,B";

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 2f));
        add(createEntry(1f, 4f));
      }};

    setElementsHelper(elements, expectedEntries);
  }

  /**
   * Tests to ensure that the entries are properly parsed
   * and ordered when using the setElements method.
   */
  @Test
  public void testSetElementsEven() {
    String elements = "0.0,2.0,1.0,4.0,2.0,1.0";

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 2f));
        add(createEntry(1f, 4f));
        add(createEntry(2f, 1f));
      }};

    setElementsHelper(elements, expectedEntries);
  }

  /**
   * Tests to ensure that the last entry is cut
   * off to make the list even, and the proper
   * entries are added.
   */
  @Test
  public void testSetElementsOdd() {
    String elements = "0.0,3.0,5.0";

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
      }};

    setElementsHelper(elements, expectedEntries);
  }

  /**
   * Test to ensure that importing from a single-entry
   * pairs List adds the entry successfully.
   */
  @Test
  public void testImportFromListSingleEntry() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 2f));
      }};

    importFromListHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing from a pairs List containing multiple
   * entries adds the entries successfully.
   */
  @Test
  public void testImportFromListMultipleEntries() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(-2f, 3f));
        add(createTuple(0f, 7f));
        add(createTuple(1f, 5f));
        add(createTuple(3f, 4f));
        add(createTuple(5f, 3f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(-2f, 3f));
        add(createEntry(0f, 7f));
        add(createEntry(1f, 5f));
        add(createEntry(3f, 4f));
        add(createEntry(5f, 3f));
      }};

    importFromListHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing from a larger tuple List
   * adds entries to the Data Series successfully.
   */
  @Test
  public void testImportFromListBiggerTuples() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 7f, 3f));
        add(createTuple(1f, 3f, 2f));
        add(createTuple(2f, 5f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 7f));
        add(createEntry(1f, 3f));
        add(createEntry(2f, 5f));
      }};

    importFromListHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing from an empty List does
   * not add any new entries.
   */
  @Test
  public void testImportFromListEmpty() {
    YailList tuples = new YailList();
    ArrayList<Entry> expectedEntries = new ArrayList<>();

    importFromListHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing entries from a List that
   * contains a tuple that has too few entries and a tuple
   * that has 2 entries skips the invalidly formatted element,
   * but imports the valid tuple.
   */
  @Test
  public void testImportFromListSmallerTuple() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(5f));
        add(createTuple(0f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 2f));
      }};

    importFromListHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing from a pairs List containing duplicate
   * entries (by x value, and by x and y values) successfully imports
   * all of the entries.
   */
  @Test
  public void testImportFromListDuplicates() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 1f));
        add(createTuple(0f, 1f));
        add(createTuple(0f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 1f));
        add(createEntry(0f, 1f));
        add(createEntry(0f, 2f));
      }};

    importFromListHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing from a generic List (instead of
   * a YailList) containing multiple YailList entries imports
   * all of the entries successfully.
   */
  @Test
  public void testImportFromListGenericList() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
        add(createTuple(1f, 7f));
        add(createTuple(2f, 5f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 7f));
        add(createEntry(2f, 5f));
      }};

    importFromListGenericHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing from a generic List (instead of
   * a YailList) containing multiple List (instead of YailList)
   * entries imports all of the entries successfully.
   */
  @Test
  public void testImportFromListGenericListEntries() {
    ArrayList<List<Float>> tuples = new ArrayList<List<Float>>() {{
        add(Arrays.asList(0f, 3f));
        add(Arrays.asList(1f, 7f));
        add(Arrays.asList(2f, 4f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 7f));
        add(createEntry(2f, 4f));
      }};

    importFromListGenericHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing from a generic List (instead of
   * a YailList) containing invalid entries does not import the
   * invalid entries, but imports the valid entries in the List.
   */
  @Test
  public void testImportFromListInvalidEntries() {
    ArrayList<Object> tuples = new ArrayList<Object>() {{
        add(Collections.singletonList(-2f));
        add(Arrays.asList(0f, 7f));
        add("test-string");
        add(Arrays.asList(1f, 1f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 7f));
        add(createEntry(1f, 1f));
      }};

    importFromListHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing from a List containing
   * mixed entries (both generic List and YailList)
   * imports all of them.
   */
  @Test
  public void testImportFromListMixedEntries() {
    ArrayList<List<?>> tuples = new ArrayList<List<?>>() {{
        add(Arrays.asList(0f, 3f));
        add(createTuple(1f, 7f));
        add(Arrays.asList(2f, 4f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 7f));
        add(createEntry(2f, 4f));
      }};

    importFromListHelper(tuples, expectedEntries);
  }

  /**
   * Test to ensure that importing from a pair containing
   * valid values adds the entry properly.
   */
  @Test
  public void testAddEntryFromTuplePair() {
    final float xValue = 3f;
    final float yValue = 4f;

    YailList tuple = createTuple(xValue, yValue);
    model.addEntryFromTuple(tuple);

    Entry entry = model.getEntries().get(0);
    Entry expectedEntry = createEntry(xValue, yValue);

    assertEquals(1, model.getEntries().size());
    assertEntriesEqual(expectedEntry, entry);
  }

  /**
   * Test to ensure that importing from an n-tuple adds the
   * entry, taking the first two entries as x and y values.
   */
  @Test
  public void testAddEntryFromTupleBiggerTuple() {
    final float xValue = 0f;
    final float yValue = 2f;

    YailList tuple = createTuple(xValue, yValue, 5f, 7f, 3f);
    model.addEntryFromTuple(tuple);

    Entry entry = model.getEntries().get(0);
    Entry expectedEntry = createEntry(xValue, yValue);

    assertEquals(1, model.getEntries().size());
    assertEntriesEqual(expectedEntry, entry);
  }


  /**
   * Test to ensure that importing from a 1-tuple does
   * not import any data (since it is an invalid entry).
   */
  @Test
  public void testAddEntryFromTupleSmallerTuple() {
    YailList tuple = createTuple(1f);
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

  /**
   * Test to ensure that importing from a tuple with
   * invalid X and Y values does not add any entry.
   */
  @Test
  public void testAddEntryFromTupleInvalidXY() {
    YailList tuple = createTuple("String", "String2");
    model.addEntryFromTuple(tuple);

    assertEquals(0, model.getDataset().getEntryCount());
  }

  /**
   * Test to ensure that the Clear Entries method deletes
   * all the entries from the Data Series.
   */
  @Test
  public void testClearEntries() {
    model.addEntryFromTuple(createTuple(0f, 5f));
    model.addEntryFromTuple(createTuple(1f, 2f));
    model.addEntryFromTuple(createTuple(2f, 4f));

    assertEquals(3, model.getEntries().size());

    model.clearEntries();

    assertEquals(0, model.getEntries().size());
  }

  /**
   * Test to ensure that importing two empty columns
   * with a row count of zero does not add any new entries.
   */
  @Test
  public void testImportFromCsvEmpty() {
    YailList xcolumn = createTuple();
    YailList ycolumn = createTuple();

    ArrayList<Entry> expectedEntries = new ArrayList<>();

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  /**
   * Test to ensure that importing from columns consisting of one
   * row does not add any new entries.
   */
  @Test
  public void testImportFromCsvOneRow() {
    YailList xcolumn = createTuple("X");
    YailList ycolumn = createTuple("Y");

    ArrayList<Entry> expectedEntries = new ArrayList<>();

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  /**
   * Test to ensure that importing from columns consisting
   * of one entry (excluding the first row) results in
   * the correct outcome.
   */
  @Test
  public void testImportFromCsvOneEntry() {
    YailList xcolumn = createTuple("X", 2f);
    YailList ycolumn = createTuple("Y", 4f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(2f, 4f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  /**
   * Test to ensure that importing from columns containing
   * many entries imports all the entries correctly.
   */
  @Test
  public void testImportFromCsvManyEntries() {
    YailList xcolumn = createTuple("X", 2f, 3f, 5f, 7f, 9f);
    YailList ycolumn = createTuple("Y", 4f, 1f, 3f, 6f, 10f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(2f, 4f));
        add(createEntry(3f, 1f));
        add(createEntry(5f, 3f));
        add(createEntry(7f, 6f));
        add(createEntry(9f, 10f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  /**
   * Test to ensure that importing from columns of uneven
   * size imports all the entries while replacing the
   * missing entries in other Lists with default values.
   */
  @Test
  public void testImportFromCsvUnevenColumns() {
    YailList xcolumn = createTuple("X", 1f, 3f, 4f, 7f);
    YailList ycolumn = createTuple("Y", 4f, 9f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(1f, 4f));
        add(createEntry(3f, 9f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }

  /**
   * Test to ensure that deleting an entry by providing an
   * existing tuple from the Data Series properly deletes it.
   */
  @Test
  public void testRemoveFromTupleExists() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
        add(createTuple(2f, 7f));
        add(createTuple(4f, 5f)); // The entry to be deleted
        add(createTuple(7f, 4f));
        add(createTuple(11f, 3f));
      }};
    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(2f, 7f));
        add(createEntry(7f, 4f));
        add(createEntry(11f, 3f));
      }};

    YailList deleteTuple = createTuple(4f, 5f);

    removeEntryFromTupleHelper(tuples, expectedEntries, deleteTuple);
  }

  /**
   * Test to ensure that deleting an entry of which a duplicate
   * exists only deletes the first found entry.
   */
  @Test
  public void testRemoveFromTupleMultipleEntries() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 1f));
        add(createTuple(1f, 1f));
        add(createTuple(1f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(1f, 1f));
        add(createEntry(1f, 2f));
      }};

    YailList deleteTuple = createTuple(1f, 1f);

    removeEntryFromTupleHelper(tuples, expectedEntries, deleteTuple);
  }

  /**
   * Test to ensure that deleting from a tuple that does not
   * exist in the Data Series does not delete any entries.
   */
  @Test
  public void testRemoveFromTupleNonExistent() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
        add(createTuple(1f, 4f));
        add(createTuple(2f, 9f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 4f));
        add(createEntry(2f, 9f));
      }};

    YailList deleteTuple = createTuple(7f, 2f);

    removeEntryFromTupleHelper(tuples, expectedEntries, deleteTuple);
  }

  /**
   * Test to ensure that deleting from an invalid tuple
   * does not remove any entries.
   */
  @Test
  public void testRemoveFromTupleInvalid() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
        add(createTuple(1f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 2f));
      }};

    YailList deleteTuple = createTuple(5f);

    removeEntryFromTupleHelper(tuples, expectedEntries, deleteTuple);
  }

  /**
   * Test to ensure that querying for an entry that exists
   * returns true for the DoesEntryExists method.
   */
  @Test
  public void testDoesEntryExistExists() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 3f));
        add(createTuple(5f, 2f));
        add(createTuple(7f, 3f));
      }};

    YailList searchTuple = createTuple(7f, 3f);

    doesEntryExistHelper(tuples, searchTuple, true);
  }

  /**
   * Test to ensure that querying for an entry that does not exist
   * returns false for the DoesEntryExists method.
   */
  @Test
  public void testDoesEntryExistDoesNotExist() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 3f));
        add(createTuple(5f, 2f));
        add(createTuple(7f, 3f));
      }};

    YailList searchTuple = createTuple(9f, 1f);

    doesEntryExistHelper(tuples, searchTuple, false);
  }

  /**
   * Test to ensure that querying for an invalid entry (wrong tuple)
   * returns false for the DoesEntryExist method.
   */
  @Test
  public void testDoesEntryExistInvalid() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 3f));
        add(createTuple(5f, 2f));
        add(createTuple(7f, 3f));
      }};

    YailList searchTuple = createTuple(9f);

    doesEntryExistHelper(tuples, searchTuple, false);
  }

  /**
   * Test to ensure that specifying an existing Entry to the findEntryIndex
   * method returns the correct index.
   */
  @Test
  public void testFindEntryIndexExists() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 3f));
        add(createTuple(5f, 2f));
        add(createTuple(7f, 3f));
        add(createTuple(9f, 1f));
      }};

    Entry searchEntry = createEntry(7f, 3f);
    final int expectedIndex = 2;

    findEntryIndexHelper(tuples, searchEntry, expectedIndex);
  }

  /**
   * Test to ensure that specifying a non-existent entry returns a negative
   * index (denoting entry not found).
   */
  @Test
  public void testFindEntryIndexDoesNotExist() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 3f));
        add(createTuple(5f, 2f));
        add(createTuple(7f, 3f));
        add(createTuple(9f, 1f));
      }};

    Entry searchEntry = createEntry(11f, 1f);
    final int expectedIndex = -1;

    findEntryIndexHelper(tuples, searchEntry, expectedIndex);
  }

  /**
   * Test to ensure that the getEntriesAsTuples method
   * returns an empty List when no entries exist.
   */
  @Test
  public void testGetEntriesAsTuplesEmpty() {
    ArrayList<YailList> tuples = new ArrayList<>();

    getEntriesAsTuplesHelper(tuples);
  }


  /**
   * Test to ensure that the getEntriesAsTuples method
   * returns a List containing a single entry when only
   * one entry exists.
   */
  @Test
  public void testGetEntriesAsTuplesSingleEntry() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
      }};

    getEntriesAsTuplesHelper(tuples);
  }

  /**
   * Test to ensure that the getEntriesAsTuples method
   * returns a List containing all the entries (in
   * case of multiple entries existing).
   */
  @Test
  public void testGetEntriesAsTuplesMultipleEntries() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
        add(createTuple(1f, 4f));
        add(createTuple(2f, 2f));
      }};

    getEntriesAsTuplesHelper(tuples);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * All EntryCriterion returns true using a seemingly arbitrary value.
   */
  @Test
  public void testCriterionSatisfiedAll() {
    Entry entry = createEntry(1f, 3f);
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.All;
    final float value = 5f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value + "");
    assertTrue(result);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * X Value criterion and a matching x value returns true.
   */
  @Test
  public void testCriterionSatisfiedXMatch() {
    Entry entry = createEntry(1f, 4f);
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.XValue;
    final float value = 1f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value + "");
    assertTrue(result);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * X Value criterion and a non-matching x value returns false.
   */
  @Test
  public void testCriterionSatisfiedXNoMatch() {
    Entry entry = createEntry(5f, 2f);
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.XValue;
    final float value = 2f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value + "");
    assertFalse(result);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * Y Value criterion and a matching y value returns true.
   */
  @Test
  public void testCriterionSatisfiedYMatch() {
    Entry entry = createEntry(2f, 4f);
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.YValue;
    final float value = 4f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value + "");
    assertTrue(result);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * Y Value criterion and a non-matching y value returns false.
   */
  @Test
  public void testCriterionSatisfiedYNoMatch() {
    Entry entry = createEntry(7f, 15f);
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.YValue;
    final float value = 14f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value + "");
    assertFalse(result);
  }

  /**
   * Test to ensure that getting entries by a specified criterion and value
   * when there is only a single match returns the correct result.
   */
  @Test
  public void testFindEntriesByCriterionSingleMatch() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 3f));
        add(createTuple(3f, 4f));
        add(createTuple(5f, 2f));
        add(createTuple(7f, 12f));
        add(createTuple(8f, 10f));
        add(createTuple(12f, 15f));
      }};

    model.importFromList(YailList.makeList(tuples));

    ArrayList<YailList> expectedTuples = new ArrayList<YailList>() {{
        add(createTuple(8f, 10f));
      }};

    YailList expected = YailList.makeList(expectedTuples);
    final float value = 8f;
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.XValue;

    YailList result = model.findEntriesByCriterion(value + "", criterion);
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that getting entries by a specified criterion and value
   * when there are multiple matches returns the correct result.
   */
  @Test
  public void testFindEntriesByCriterionMultipleMatches() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 1f));
        add(createTuple(3f, 1f));
        add(createTuple(5f, 3f));
        add(createTuple(7f, 5f));
        add(createTuple(8f, 1f));
        add(createTuple(12f, 4f));
      }};

    model.importFromList(YailList.makeList(tuples));

    ArrayList<YailList> expectedTuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 1f));
        add(createTuple(3f, 1f));
        add(createTuple(8f, 1f));
      }};

    YailList expected = YailList.makeList(expectedTuples);
    final float value = 1f;
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.YValue;

    YailList result = model.findEntriesByCriterion(value + "", criterion);
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that getting entries by a specified criterion and value
   * when there is no match returns an empty list.
   */
  @Test
  public void testFindEntriesByCriterionNoMatches() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, 1f));
        add(createTuple(5f, 1f));
        add(createTuple(9f, 3f));
        add(createTuple(12f, 5f));
      }};

    model.importFromList(YailList.makeList(tuples));

    YailList expected = new YailList();

    final float value = 4f;
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.YValue;

    YailList result = model.findEntriesByCriterion(value + "", criterion);
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that getting a Tuple from an Entry object
   * returns the correct result.
   */
  @Test
  public void testGetTupleFromEntry() {
    Entry entry = createEntry(2f, 3f);

    YailList expected = createTuple(2f, 3f);
    YailList result = model.getTupleFromEntry(entry);

    assertEquals(expected, result);
  }

  /**
   * Test to ensure that getting an Entry from a Tuple
   * returns the correct result.
   */
  @Test
  public void testGetEntryFromTuple() {
    YailList tuple = createTuple(3f, 4f);
    Entry expected = createEntry(3f, 4f);

    Entry result = model.getEntryFromTuple(tuple);
    assertEntriesEqual(expected, result);
  }

  /**
   * Test to ensure that attempting to get an Entry from a
   * Tuple that is too small returns a null value.
   */
  @Test
  public void testGetEntryFromTupleTooSmall() {
    YailList tuple = createTuple(1f);

    Entry result = model.getEntryFromTuple(tuple);
    assertNull(result);
  }

  /**
   * Test to ensure that getting an entry from a
   * Tuple that is too large results in a constructed
   * Entry from the first 2 values.
   */
  @Test
  public void testGetEntryFromTupleTooLarge() {
    YailList tuple = createTuple(4f, 1f, 2f, 7f);
    Entry expected = createEntry(4f, 1f);

    Entry result = model.getEntryFromTuple(tuple);
    assertEntriesEqual(expected, result);
  }

  /**
   * Test to ensure that invoking the removeValues method
   * with an empty List does not remove any entries.
   */
  @Test
  public void testRemoveValuesEmpty() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, -1f));
        add(createTuple(1f, 1f));
        add(createTuple(2f, 7f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, -1f));
        add(createEntry(1f, 1f));
        add(createEntry(2f, 7f));
      }};

    List<List<Object>> removeEntries = new ArrayList<>();

    removeValuesHelper(tuples, expectedEntries, removeEntries);
  }

  /**
   * Test to ensure that invoking the removeValues method
   * with a single entry removes the entry.
   */
  @Test
  public void testRemoveValuesSingleValue() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(1f, -1f));
        add(createTuple(3f, 1f));
        add(createTuple(5f, 7f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(1f, -1f));
        add(createEntry(5f, 7f));
      }};

    // Remove entries
    List<List<Float>> removeEntries = new ArrayList<List<Float>>() {{
        add(Arrays.asList(3f, 1f));
      }};

    removeValuesHelper(tuples, expectedEntries, removeEntries);
  }

  /**
   * Test to ensure that invoking the removeValues method
   * with a multiple values removes all the appropriate values.
   */
  @Test
  public void testRemoveValuesMultipleValues() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 1f));
        add(createTuple(1f, 3f));
        add(createTuple(3f, 2f));
        add(createTuple(5f, 4f));
        add(createTuple(6f, 8f));
        add(createTuple(9f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(1f, 3f));
        add(createEntry(5f, 4f));
        add(createEntry(6f, 8f));
      }};

    // Remove entries
    List<List<Float>> removeEntries = new ArrayList<List<Float>>() {{
        add(Arrays.asList(0f, 1f));
        add(Arrays.asList(3f, 2f));
        add(Arrays.asList(9f, 2f));
      }};

    removeValuesHelper(tuples, expectedEntries, removeEntries);
  }

  /**
   * Test to ensure that invoking the removeValues method
   * with a List that contains entries that do not exist
   * in the Data Series does not do anything with
   * the removed entries, but removes the existing
   * ones in between.
   */
  @Test
  public void testRemoveValuesNonExistentValues() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 1f));
        add(createTuple(1f, 3f));
        add(createTuple(3f, 2f));
        add(createTuple(6f, 8f));
        add(createTuple(9f, 2f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 1f));
        add(createEntry(1f, 3f));
        add(createEntry(3f, 2f));
        add(createEntry(6f, 8f));
      }};

    // Remove entries
    List<YailList> removeEntries = new ArrayList<YailList>() {{
        add(createTuple(1f, 5f)); // Does not exist
        add(createTuple(10f, 5f)); // Does not exist
        add(createTuple(9f, 2f));
      }};

    removeValuesHelper(tuples, expectedEntries, removeEntries);
  }

  /**
   * Test to ensure that invoking the removeValues method
   * with a List containing invalid entries does not
   * process the invalid entries, but processes the
   * valid ones.
   */
  @Test
  public void testRemoveValuesInvalidEntries() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
        add(createTuple(1f, 10f));
        add(createTuple(2f, 5f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 10f));
      }};

    // Remove entries
    List<Object> removeEntries = new ArrayList<Object>() {{
        add(Arrays.asList(2f, 5f));
        add(Collections.singletonList(1f)); // tuple too small
        add("test-string"); // invalid entry
      }};

    removeValuesHelper(tuples, expectedEntries, removeEntries);
  }

  /**
   * Test to ensure that invoking the removeValues method
   * with YailList entries removes the entries properly.
   */
  @Test
  public void testRemoveValuesYailListEntries() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple(0f, 3f));
        add(createTuple(1f, 10f));
        add(createTuple(2f, 5f));
      }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
        add(createEntry(1f, 10f));
      }};

    // Remove entries
    List<YailList> removeEntries = new ArrayList<YailList>() {{
        add(createTuple(2f, 5f));
      }};

    removeValuesHelper(tuples, expectedEntries, removeEntries);
  }

  /**
   * Test case to ensure that adding a valid time entry properly
   * adds it to the Data Series.
   */
  @Test
  public void testAddTimeEntry() {
    YailList timeEntry = createTuple(0f, 3f);
    model.addTimeEntry(timeEntry);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(0f, 3f));
      }};

    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test case to ensure that if the maximum time entries
   * count is exhausted, the first values are removed upon
   * adding new time entries.
   */
  @Test
  public void testAddTimeEntryExceedsMaximum() {
    model.setMaximumTimeEntries(5);

    List<YailList> entries = new ArrayList<YailList>() {{
        add(createTuple(0f, 1f));
        add(createTuple(1f, 3f));
        add(createTuple(2f, 4f));
        add(createTuple(3f, 2f));
        add(createTuple(4f, 1f));
        add(createTuple(5f, 7f));
        add(createTuple(6f, 1f));
        add(createTuple(7f, 5f));
      }};


    for (YailList entry : entries) {
      model.addTimeEntry(entry);
    }

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry(3f, 2f));
        add(createEntry(4f, 1f));
        add(createEntry(5f, 7f));
        add(createEntry(6f, 1f));
        add(createEntry(7f, 5f));
      }};

    assertExpectedEntriesHelper(expectedEntries);
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
}
