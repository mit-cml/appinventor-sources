package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

/**
 * Base class for ChartDataModel tests.
 *
 * Tests the integration with the MPAndroidChart library
 * classes by actually operating on the Data Series objects.
 */
public abstract class ChartDataModelBaseTest<M extends ChartDataModel,
    D extends ChartData> extends RobolectricTestBase {
  protected M model;
  protected D data;

  @Before
  public abstract void setup();

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
   * Helper method that checks whether all the expected entries
   * are in the data set.
   * For each entry (as well as the size of the entries), an assertion
   * is made.
   *
   * @param expectedEntries  list of expected entries
   */
  protected void assertExpectedEntriesHelper(ArrayList<Entry> expectedEntries) {
    // Make sure the number of entries parsed is correct
    assertEquals(expectedEntries.size(), model.getDataset().getEntryCount());

    for (int i = 0; i < model.getDataset().getEntryCount(); ++i) {
      // Get the entry from the Data Series
      Entry entry = model.getDataset().getEntryForIndex(i);

      // Get the expected entry
      Entry expectedEntry = expectedEntries.get(i);

      // Assert that the two entries are equal
      assertEntriesEqual(expectedEntry, entry);
    }
  }

  /**
   * Helper method that asserts whether the specified two entries are equal.
   * This is needed because the MPAndroidChart library's equal method checks
   * by hash-code instead.
   *
   * @param e1  Expected Entry
   * @param e2  Actual Entry
   */
  protected abstract void assertEntriesEqual(Entry e1, Entry e2);

  protected YailList createTuple(Object... entries) {
    return YailList.makeList(entries);
  }
}
