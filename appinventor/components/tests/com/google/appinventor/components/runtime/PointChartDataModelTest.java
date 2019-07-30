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
    extends ChartDataModelBaseTest<M, D> {
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
   * Tests whether an entry is correctly added to the Data Set
   * upon calling the add entry method with x and y coordinates.
   */
//  @Test
//  public void testAddEntry() {
//    // Pre-condition: make sure there are no entries initially
//    assertEquals(0, model.getDataset().getEntryCount());
//
//    // Add an entry
//    float x = 4;
//    float y = 5;
//    model.addEntry(x, y);
//
//    // Ensure that the entry has been added
//    assertEquals(1, model.getDataset().getEntryCount());
//
//    // Make sure that a correct entry has been added
//    Entry entry = model.getDataset().getEntryForIndex(0);
//    assertEquals(x, entry.getX());
//    assertEquals(y, entry.getY());
//  }

  /**
   * Tests to ensure that Data Series entries are not changed
   * when passing in invalid input via setEelements.
   */
  @Test
  public void testSetElementsInvalid() {
    String elements = "1, 2, 3, 4, A, B";

    model.setElements(elements);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 2f));
      add(new Entry(3f, 4f));
    }};

    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Tests to ensure that the entries are properly parsed
   * and ordered when using the setElements method.
   */
  @Test
  public void testSetElementsEven() {
    // We are adding (1, 2), (2, 4), (3, 1)
    // End result should be ordered by X value.
    String elements = "1, 2, 2, 4, 3, 1";
    model.setElements(elements);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 2f));
      add(new Entry(2f, 4f));
      add(new Entry(3f, 1f));
    }};

    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Tests to ensure that the last entry is cut
   * off to make the list even, and the proper
   * entries are added.
   */
  @Test
  public void testSetElementsOdd() {
    // List length is odd
    String elements = "1, 3, 5";
    model.setElements(elements);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 3f));
    }};

    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from a single-entry
   * pairs List adds the entry successfully.
   */
  @Test
  public void testImportFromListSingleEntry() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(1f, 2f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 2f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    assertExpectedEntriesHelper(expectedEntries);
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

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(-2f, 3f));
      add(new Entry(0f, 7f));
      add(new Entry(1f, 5f));
      add(new Entry(3f, 4f));
      add(new Entry(5f, 3f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    assertExpectedEntriesHelper(expectedEntries);
  }

  @Override
  protected void assertEntriesEqual(Entry e1, Entry e2) {
    assertEquals(e1.getX(), e2.getX());
    assertEquals(e1.getY(), e2.getY());
    assertEquals(e1.getClass(), e2.getClass());
  }

  /**
   * Test to ensure that importing from a larger tuple List
   * adds entries to the Data Series successfully.
   */
  @Test
  public void testImportFromListBiggerTuples() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(-2f, 7f, 3f));
      add(createTuple(0f, 3f, 2f));
      add(createTuple(5f, 5f, 2f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(-2f, 7f));
      add(new Entry(0f, 3f));
      add(new Entry(5f, 5f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from an empty List does
   * not add any new entries.
   */
  @Test
  public void testImportFromListEmpty() {
    YailList pairs = new YailList();
    ArrayList<Entry> expectedEntries = new ArrayList<Entry>();

    model.importFromList(pairs);
    assertExpectedEntriesHelper(expectedEntries);
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
      add(createTuple(1f, 2f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 2f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from a pairs List containing duplicate
   * entries (by x value, and by x and y values) successfully imports
   * all of the entries.
   */
  @Test
  public void testImportFromListDuplicates() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(1f, 1f));
      add(createTuple(1f, 1f));
      add(createTuple(1f, 2f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 1f));
      add(new Entry(1f, 1f));
      add(new Entry(1f, 2f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    assertExpectedEntriesHelper(expectedEntries);
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

    Entry entry = model.getDataset().getEntryForIndex(0);
    Entry expectedEntry = new Entry(xValue, yValue);

    assertEquals(1, model.getDataset().getEntryCount());
    assertEntriesEqual(expectedEntry, entry);
  }

  /**
   * Test to ensure that importing from an n-tuple adds the
   * entry, taking the first two entries as x and y values.
   */
  @Test
  public void testAddEntryFromTupleBiggerTuple() {
    final float xValue = 1f;
    final float yValue = 2f;

    YailList tuple = createTuple(xValue, yValue, 5f, 7f, 3f);
    model.addEntryFromTuple(tuple);

    Entry entry = model.getDataset().getEntryForIndex(0);
    Entry expectedEntry = new Entry(xValue, yValue);

    assertEquals(1, model.getDataset().getEntryCount());
    assertEntriesEqual(expectedEntry, entry);
  }


  /**
   * Test to ensure that importing from a 1-tuple does
   * not import any data (since it is an invalid entry)
   */
  @Test
  public void testAddEntryFromTupleSmallerTuple() {
    YailList tuple = createTuple(1f);
    model.addEntryFromTuple(tuple);

    assertEquals(0, model.getDataset().getEntryCount());
  }

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
    model.addEntryFromTuple(createTuple(4f, 5f));
    model.addEntryFromTuple(createTuple(3f, 2f));
    model.addEntryFromTuple(createTuple(1f, 4f));

    assertEquals(3, model.getDataset().getEntryCount());

    model.clearEntries();

    assertEquals(0, model.getDataset().getEntryCount());
  }

  /**
   * Test to ensure that importing two empty columns
   * with a row count of zero does not add any new entries.
   */
  @Test
  public void testImportFromCSVEmpty() {
    YailList xColumn = createTuple();
    YailList yColumn = createTuple();
    YailList columns = YailList.makeList(Arrays.asList(xColumn, yColumn));

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>();

    model.importFromCSV(columns);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from two empty columns with
   * a row size greater than 1 triggers the default option, which
   * populates values automatically starting from 1 and incrementing
   * on each new entry.
   */
  @Test
  public void testImportFromCSVEmptyDefaultOption() {
    YailList xColumn = createTuple();
    YailList yColumn = createTuple();
    YailList columns = YailList.makeList(Arrays.asList(xColumn, yColumn));

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>();

    model.importFromCSV(columns);
    assertExpectedEntriesHelper(expectedEntries);
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
    YailList columns = YailList.makeList(Arrays.asList(xColumn, yColumn));

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 3f));
      add(new Entry(2f, 5f));
      add(new Entry(3f, -3f));
      add(new Entry(4f, 7f));
    }};

    model.importFromCSV(columns);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from columns consisting of one
   * row does not add any new entries.
   */
  @Test
  public void testImportFromCSVOneRow() {
    YailList xColumn = createTuple("X");
    YailList yColumn = createTuple("Y");
    YailList columns = YailList.makeList(Arrays.asList(xColumn, yColumn));

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>();

    model.importFromCSV(columns);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from columns consisting
   * of one entry (excluding the first row) results in
   * the correct outcome.
   */
  @Test
  public void testImportFromCSVOneEntry() {
    YailList xColumn = createTuple("X", 2);
    YailList yColumn = createTuple("Y", 4);
    YailList columns = YailList.makeList(Arrays.asList(xColumn, yColumn));

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(2f, 4f));
    }};

    model.importFromCSV(columns);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from columns containing
   * many entries imports all the entries correctly.
   */
  @Test
  public void testImportFromCSVManyEntries() {
    YailList xColumn = createTuple("X", 2, 3, 5, 7, 9);
    YailList yColumn = createTuple("Y", 4, 1, 3, 6, 10);
    YailList columns = YailList.makeList(Arrays.asList(xColumn, yColumn));

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(2f, 4f));
      add(new Entry(3f, 1f));
      add(new Entry(5f, 3f));
      add(new Entry(7f, 6f));
      add(new Entry(9f, 10f));
    }};

    model.importFromCSV(columns);
    assertExpectedEntriesHelper(expectedEntries);
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

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(0f, 3f));
      add(new Entry(2f, 7f));
      add(new Entry(7f, 4f));
      add(new Entry(11f, 3f));
    }};

    YailList deleteTuple = createTuple(4f, 5f);

    // Import the data and assert all the entries
    model.importFromList(pairs);
    model.removeEntryFromTuple(deleteTuple);
    assertExpectedEntriesHelper(expectedEntries);
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

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 1f));
      add(new Entry(1f, 2f));
    }};

    YailList deleteTuple = createTuple(1f, 1f);

    // Import the data and assert all the entries
    model.importFromList(pairs);
    model.removeEntryFromTuple(deleteTuple);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that deleting from a tuple that does not
   * exist in the Data Series does not delete any entries.
   */
  @Test
  public void testRemoveFromTupleNonExistant() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(1f, 3f));
      add(createTuple(2f, 4f));
      add(createTuple(5f, 9f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 3f));
      add(new Entry(2f, 4f));
      add(new Entry(5f, 9f));
    }};

    YailList deleteTuple = createTuple(7f, 2f);

    // Import the data and assert all the entries
    model.importFromList(pairs);
    model.removeEntryFromTuple(deleteTuple);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that deleting from an invalid tuple
   * does not remove any entries.
   */
  @Test
  public void testRemoveFromTupleInvalid() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(1f, 3f));
      add(createTuple(5f, 2f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(1f, 3f));
      add(new Entry(5f, 2f));
    }};

    YailList deleteTuple = createTuple(5f);

    // Import the data and assert all the entries
    model.importFromList(pairs);
    model.removeEntryFromTuple(deleteTuple);
    assertExpectedEntriesHelper(expectedEntries);
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

    YailList pairs = YailList.makeList(tuples);

    YailList searchTuple = createTuple(7f, 3f);

    model.importFromList(pairs);
    assertTrue(model.doesEntryExist(searchTuple));
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

    YailList pairs = YailList.makeList(tuples);

    YailList searchTuple = createTuple(9f, 1f);

    model.importFromList(pairs);
    assertFalse(model.doesEntryExist(searchTuple));
  }

  /**
   * Test to ensure that querying for an invalid entry (wrong tuple)
   * returns false for the DoesEntryExist method
   */
  @Test
  public void testDoesEntryExistInvalid() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(1f, 3f));
      add(createTuple(5f, 2f));
      add(createTuple(7f, 3f));
    }};

    YailList pairs = YailList.makeList(tuples);

    YailList searchTuple = createTuple(9f);

    model.importFromList(pairs);
    assertFalse(model.doesEntryExist(searchTuple));
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

    YailList pairs = YailList.makeList(tuples);
    model.importFromList(pairs);

    Entry searchEntry = new Entry(7f, 3f);
    final int expectedIndex = 2;


    int result = model.findEntryIndex(searchEntry);
    assertEquals(expectedIndex, result);
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

    YailList pairs = YailList.makeList(tuples);
    model.importFromList(pairs);

    Entry searchEntry = new Entry(2f, 3f);
    final int expectedIndex = 3;


    int result = model.findEntryIndex(searchEntry);
    assertEquals(expectedIndex, result);
  }

  /**
   * Test to ensure that specifying a non-existent entry returns a negative
   * index (denoting entry not found)
   */
  @Test
  public void testFindEntryIndexDoesNotExist() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(1f, 3f));
      add(createTuple(5f, 2f));
      add(createTuple(7f, 3f));
      add(createTuple(9f, 1f));
    }};

    YailList pairs = YailList.makeList(tuples);
    model.importFromList(pairs);

    Entry searchEntry = new Entry(11f, 1f);
    final int expectedIndex = -1;

    int result = model.findEntryIndex(searchEntry);
    assertEquals(expectedIndex, result);
  }

  /**
   * Test to ensure that the getEntriesAsTuples method
   * returns an empty List when no entries exist.
   */
  @Test
  public void testGetEntriesAsTuplesEmpty() {
    final YailList expected = new YailList();
    YailList result = model.getEntriesAsTuples();

    assertEquals(expected, result);
  }


  /**
   * Test to ensure that the getEntriesAsTuples method
   * returns a List containing a single entry when only
   * one entry exists.
   */
  @Test
  public void testGetEntriesAsTuplesSingleEntry() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(1f, 3f));
    }};

    YailList expected = YailList.makeList(tuples);
    model.importFromList(expected);

    YailList result =  model.getEntriesAsTuples();
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that the getEntriesAsTuples method
   * returns a List containing all the entries (in
   * case of multiple entries existing)
   */
  @Test
  public void testGetEntriesAsTuplesMultipleEntries() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(1f, 3f));
      add(createTuple(3f, 4f));
      add(createTuple(5f, 2f));
    }};

    YailList expected = YailList.makeList(tuples);
    model.importFromList(expected);

    YailList result =  model.getEntriesAsTuples();
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * All EntryCriterion returns true using a seemingly arbitrary value.
   */
  @Test
  public void testCriterionSatisfiedAll() {
    Entry entry = new Entry(1f, 3f);
    final ChartDataModel.EntryCriterion criterion = ChartDataModel.EntryCriterion.All;
    final float value = 5f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value);

    assertTrue(result);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * X Value criterion and a matching x value returns true.
   */
  @Test
  public void testCriterionSatisfiedXMatch() {
    Entry entry = new Entry(1f, 4f);
    final ChartDataModel.EntryCriterion criterion = ChartDataModel.EntryCriterion.XValue;
    final float value = 1f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value);

    assertTrue(result);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * X Value criterion and a non-matching x value returns false.
   */
  @Test
  public void testCriterionSatisfiedXNoMatch() {
    Entry entry = new Entry(5f, 2f);
    final ChartDataModel.EntryCriterion criterion = ChartDataModel.EntryCriterion.XValue;
    final float value = 2f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value);

    assertFalse(result);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * Y Value criterion and a matching y value returns true.
   */
  @Test
  public void testCriterionSatisfiedYMatch() {
    Entry entry = new Entry(2f, 4f);
    final ChartDataModel.EntryCriterion criterion = ChartDataModel.EntryCriterion.YValue;
    final float value = 4f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value);

    assertTrue(result);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * Y Value criterion and a non-matching y value returns false.
   */
  @Test
  public void testCriterionSatisfiedYNoMatch() {
    Entry entry = new Entry(7f, 15f);
    final ChartDataModel.EntryCriterion criterion = ChartDataModel.EntryCriterion.YValue;
    final float value = 14f;

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value);

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
    final ChartDataModel.EntryCriterion criterion = ChartDataModel.EntryCriterion.XValue;

    YailList result = model.findEntriesByCriterion(value, criterion);
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
    final ChartDataModel.EntryCriterion criterion = ChartDataModel.EntryCriterion.YValue;

    YailList result = model.findEntriesByCriterion(value, criterion);
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
    final ChartDataModel.EntryCriterion criterion = ChartDataModel.EntryCriterion.YValue;

    YailList result = model.findEntriesByCriterion(value, criterion);
    assertEquals(expected, result);
  }

  /**
   * Test to ensure that getting a Tuple from an Entry object
   * returns the correct result.
   */
  @Test
  public void testGetTupleFromEntry() {
    Entry entry = new Entry(2f, 3f);

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

    Entry expected = new Entry(3f, 4f);
    Entry result = model.getEntryFromTuple(tuple);

    assertEquals(expected.getX(), result.getX());
    assertEquals(expected.getY(), result.getY());
  }

  /**
   * Test to ensure that attempting to get an Entry from a
   * Tuple that is too small returns a null value.
   */
  @Test
  public void testGetEntryFromTupleTooSmall() {
    YailList tuple = createTuple(1f);

    Entry expected = null;
    Entry result = model.getEntryFromTuple(tuple);

    assertEquals(expected, result);
  }

  /**
   * Test to ensure that getting an entry from a
   * Tuple that is too large results in a constructed
   * Entry from the first 2 values.
   */
  @Test
  public void testGetEntryFromTupleTooLarge() {
    YailList tuple = createTuple(4f, 1f, 2f, 7f);

    Entry expected = new Entry(4f, 1f);
    Entry result = model.getEntryFromTuple(tuple);

    assertEquals(expected.getX(), result.getX());
    assertEquals(expected.getY(), result.getY());
  }

  /**
   * Test to ensure that importing from a generic List (instead of
   * a YailList) containing multiple YailList entries imports
   * all of the entries successfully.
   */
  @Test
  public void testImportFromListGenericList() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(-2f, 3f));
      add(createTuple(0f, 7f));
      add(createTuple(1f, 5f));
    }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(-2f, 3f));
      add(new Entry(0f, 7f));
      add(new Entry(1f, 5f));
    }};

    // Import the data and assert all the entries
    model.importFromList(tuples);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from a generic List (instead of
   * a YailList) containing multiple List (instead of YailList)
   * entries imports all of the entries successfully.
   */
  @Test
  public void testImportFromListGenericListEntries() {
    ArrayList<List> tuples = new ArrayList<List>() {{
      add(Arrays.asList(-2f, 3f));
      add(Arrays.asList(0f, 7f));
      add(Arrays.asList(5f, 4f));
    }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(-2f, 3f));
      add(new Entry(0f, 7f));
      add(new Entry(5f, 4f));
    }};

    // Import the data and assert all the entries
    model.importFromList(tuples);
    assertExpectedEntriesHelper(expectedEntries);
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
      add(Arrays.asList(3f, 1f));
    }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(0f, 7f));
      add(new Entry(3f, 1f));
    }};

    // Import the data and assert all the entries
    model.importFromList(tuples);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from a List containing
   * mixed entries (both generic List and YailList)
   * imports all of them.
   */
  @Test
  public void testImportFromListMixedEntries() {
    ArrayList<List> tuples = new ArrayList<List>() {{
      add(Arrays.asList(-2f, 3f));
      add(createTuple(0f, 7f));
      add(Arrays.asList(5f, 4f));
    }};

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(-2f, 3f));
      add(new Entry(0f, 7f));
      add(new Entry(5f, 4f));
    }};

    // Import the data and assert all the entries
    model.importFromList(tuples);
    assertExpectedEntriesHelper(expectedEntries);
  }

//  /**
//   * Test to ensure that passing in a row size
//   * less than the size of the columns imports
//   * only those select entries.
//   */
//  @Test
//  public void testImportFromCSVLimitRows() {
//    final int rows = 2;
//    YailList xColumn = createTuple("X", 1, 4, 7, 9);
//    YailList yColumn = createTuple("Y", -2, -3, 9, 8);
//    YailList columns = YailList.makeList(Arrays.asList(xColumn, yColumn));
//
//    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
//      add(new Entry(1f, -2f));
//    }};
//
//    model.importFromCSV(columns);
//    assertExpectedEntriesHelper(expectedEntries);
//  }
}