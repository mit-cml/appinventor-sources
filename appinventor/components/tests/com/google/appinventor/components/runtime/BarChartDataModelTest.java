package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

public class BarChartDataModelTest extends ChartDataModel2DTest<BarChartDataModel, BarData> {

  @Override
  public void setup() {
    data = new BarData();
    model = new BarChartDataModel(data);
  }

  /**
   * Test case to ensure that adding an entry with an x value of 0
   * adds the entry to the Bar Chart as the first (and only) entry.
   */
  @Override
  public void testAddEntryFromTuplePair() {
      final float xValue = 0f;
      final float yValue = 10f;
      YailList tuple = createTuple(xValue, yValue);

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
      add(createEntry(0f, 0f));
      add(createEntry(1f, 1f));
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

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>();
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
  public void testRemoveValuesInvalidEntries() {

  }

  @Override
  public void testImportFromCSVUnevenColumns() {
    YailList xColumn = createTuple("X", 0f, 4f, 5f, 6f);
    YailList yColumn = createTuple("Y", -3f, 5f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(createEntry(0f, -3f));
      add(createEntry(1f, 0f));
      add(createEntry(2f, 0f));
      add(createEntry(3f, 0f));
      add(createEntry(4f, 5f));
    }};

    importFromCSVHelper(expectedEntries, xColumn, yColumn);
  }

  @Override
  public void testImportFromCSVManyEntries() {
    YailList xColumn = createTuple("X", 1f, 3f, 6f, 7f);
    YailList yColumn = createTuple("Y", 10f, 5f, 3f, 9f);

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

    importFromCSVHelper(expectedEntries, xColumn, yColumn);
  }

  @Override
  public void testImportFromCSVOneEntry() {
    YailList xColumn = createTuple("X", 4f);
    YailList yColumn = createTuple("Y", 1f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(createEntry(0f, 0f));
      add(createEntry(1f, 0f));
      add(createEntry(2f, 0f));
      add(createEntry(3f, 0f));
      add(createEntry(4f, 1f));
    }};

    importFromCSVHelper(expectedEntries, xColumn, yColumn);
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


  @Override
  public void testRemoveFromTupleInvalid() {

  }

  @Override
  public void testRemoveValuesMultipleValues() {

  }

  @Override
  public void testRemoveValuesYailListEntries() {

  }

  @Override
  public void testRemoveValuesSingleValue() {

  }

  @Override
  public void testRemoveFromTupleExists() {

  }

  @Override
  public void testRemoveFromTupleMultipleEntries() {

  }

  @Override
  public void testRemoveValuesNonExistentValues() {

  }

  @Override
  public void testAddTimeEntryExceedsMaximum() {

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