package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Unit tests for the LineChartModel class.
 */
public class LineChartDataModelTest
    extends PointChartDataModelTest<LineChartDataModel, LineData> {
  @Before
  @Override
  public void setup() {
    data = new LineData();
    model = new LineChartDataModel(data);
  }

  /**
   * Test to ensure that adding an entry to a Data Series of a duplicate
   * x value inserts it after all the other x-value entries.
   * This method particularly tests the case where the middle element
   * of the Data Series has the same x value as the entry to be inserted.
   */
  @Test
  public void testAddEntryMatchMiddle() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(0f, 5f));
      add(createTuple(1f, 1f));
      add(createTuple(1f, 3f));
      add(createTuple(1f, -5f));
      add(createTuple(1f, 7f));
      add(createTuple(1f, 10f));
      add(createTuple(2f, 15f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(0f, 5f));
      add(new Entry(1f, 1f));
      add(new Entry(1f, 3f));
      add(new Entry(1f, -5f));
      add(new Entry(1f, 7f));
      add(new Entry(1f, 10f));
      add(new Entry(1f, 6f)); // This will be the entry that we will insert
      add(new Entry(2f, 15f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    model.addEntryFromTuple(createTuple(1f, 6f)); // Should be inserted after all the entries that have x value 1
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that adding an entry to a Data Series of a duplicate
   * x value inserts it after all the other x-value entries.
   * This method tests the case where the middle element does not have
   * the same x value as the entry to be inserted.
   */
  @Test
  public void testAddEntryMatchRight() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(0f, 0f));
      add(createTuple(1f, 2f));
      add(createTuple(2f, 3f));
      add(createTuple(2f, 4f));
      add(createTuple(2f, -3f));
      add(createTuple(3f, 6f));
      add(createTuple(4f, 7f));
      add(createTuple(4f, 9f));
      add(createTuple(4f, -7f));
      add(createTuple(4f, 10f));
      add(createTuple(5f, 1f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(0f, 0f));
      add(new Entry(1f, 2f));
      add(new Entry(2f, 3f));
      add(new Entry(2f, 4f));
      add(new Entry(2f, -3f));
      add(new Entry(3f, 6f));
      add(new Entry(4f, 7f));
      add(new Entry(4f, 9f));
      add(new Entry(4f, -7f));
      add(new Entry(4f, 10f));
      add(new Entry(4f, -15f)); // This will be the entry that we will insert
      add(new Entry(5f, 1f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    model.addEntryFromTuple(createTuple(4f, -15f)); // Should be inserted after all the entries that have x value 4
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that adding various entries out of order results
   * in a sorted Data Series post-adding.
   *
   * This is an example case which breaks when using MPAndroidChart's
   * addEntryOrdered method (in v3.1.0)
   */
  @Test
  public void testAddEntriesOutOfOrder() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(25f, 3f));
      add(createTuple(3f, 4f));
      add(createTuple(4f, 7f));
      add(createTuple(7f, 3f));
      add(createTuple(9f, 1f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(3f, 4f));
      add(new Entry(4f, 7f));
      add(new Entry(7f, 3f));
      add(new Entry(9f, 1f));
      add(new Entry(25f, 3f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that adding an entry to a Data Series inserts it
   * in the appropriate position.
   * This method tests the case where the element is inserted at the
   * end of the Data Series entries.
   */
  @Test
  public void testAddEntryMatchLast() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(0f, 0f));
      add(createTuple(1f, 2f));
      add(createTuple(2f, 3f));
      add(createTuple(5f, 11f));
      add(createTuple(9f, 15f));
      add(createTuple(11f, 16f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(0f, 0f));
      add(new Entry(1f, 2f));
      add(new Entry(2f, 3f));
      add(new Entry(5f, 11f));
      add(new Entry(9f, 15f));
      add(new Entry(11f, 16f));
      add(new Entry(15f, 1f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    model.addEntryFromTuple(createTuple(15f, 1f)); // Should be inserted after all the entries that have x value 4
    assertExpectedEntriesHelper(expectedEntries);
  }

  /**
   * Test to ensure that adding an entry to a Data Series inserts it
   * in the appropriate position.
   * This method tests the case where the element is inserted at the
   * start of the Data Series entries.
   */
  @Test
  public void testAddEntryMatchFirst() {
    ArrayList<YailList> tuples = new ArrayList<YailList>() {{
      add(createTuple(-4f, 5f));
      add(createTuple(-1f, 2f));
      add(createTuple(3f, 1f));
      add(createTuple(7f, 11f));
      add(createTuple(15f, 15f));
      add(createTuple(21f, 16f));
    }};

    YailList pairs = YailList.makeList(tuples);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
      add(new Entry(-7f, 1f));
      add(new Entry(-4f, 5f));
      add(new Entry(-1f, 2f));
      add(new Entry(3f, 1f));
      add(new Entry(7f, 11f));
      add(new Entry(15f, 15f));
      add(new Entry(21f, 16f));
    }};

    // Import the data and assert all the entries
    model.importFromList(pairs);
    model.addEntryFromTuple(createTuple(-7f, 1f)); // Should be inserted after all the entries that have x value 4
    assertExpectedEntriesHelper(expectedEntries);
  }
}
