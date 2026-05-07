// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import android.graphics.Color;

import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieEntry;

import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test class for the Pie Chart Data Model.
 * Tests various data operations on Pie data.
 *
 * @see AbstractChartDataModel2DTest
 */
public class PieChartDataModelTest
    extends AbstractChartDataModel2DTest<PieChartDataModel, PieData> {
  private PieChartView chartView;
  private List<LegendEntry> legendEntries;

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
        add(createEntry("0", 3f));
        add(createEntry("1", 5f));
        add(createEntry("2", -3f));
        add(createEntry("3", 7f));
      }};

    importFromCsvHelper(expectedEntries, xcolumn, ycolumn);
  }


  /**
   * Test to ensure that importing from a pair containing
   * valid values with an x value being a String label
   * adds the entry properly.
   */
  @Test
  public void testAddEntryFromTupleXLabel() {
    final String xValue = "Entry";
    final float yValue = 25f;

    YailList tuple = createTuple(xValue, yValue);
    model.addEntryFromTuple(tuple);

    Entry entry = model.getEntries().get(0);
    Entry expectedEntry = createEntry(xValue, yValue);

    assertEquals(1, model.getEntries().size());
    assertEntriesEqual(expectedEntry, entry);
  }

  /**
   * Test to ensure that comparing two entries which
   * have the same x and y values returns true via
   * the areEntriesEqual method.
   */
  @Override
  public void testEntriesEqual() {
    Entry entry1 = createEntry("Entry", 12f);
    Entry entry2 = createEntry("Entry", 12f);

    assertTrue(model.areEntriesEqual(entry1, entry2));
  }

  /**
   * Test to ensure that comparing two entries which
   * have the same x but different y values returns false via
   * the areEntriesEqual method.
   */
  @Override
  public void testEntriesNotEqualY() {
    Entry entry1 = createEntry("Entry", 12f);
    Entry entry2 = createEntry("Entry", 15f);

    assertFalse(model.areEntriesEqual(entry1, entry2));
  }

  /**
   * Test to ensure that comparing two entries which
   * have the same y but different x values returns false via
   * the areEntriesEqual method.
   */
  @Override
  public void testEntriesNotEqualX() {
    Entry entry1 = createEntry("Entry", 10f);
    Entry entry2 = createEntry("Entry 2", 10f);

    assertFalse(model.areEntriesEqual(entry1, entry2));
  }

  /**
   * Test to ensure that setting the Colors property
   * with a List containing a single entry properly
   * sets the colors of the Data Series and sets
   * the color to every value.
   */
  @Test
  public void testSetColorsSingleColor() {
    final int color = Color.BLUE;

    List<Integer> colorList = new ArrayList<Integer>() {{
        add(color);
      }};

    List<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple("Entry", 3f));
        add(createTuple("Entry 2", 4f));
        add(createTuple("Entry 3", 2f));
        add(createTuple("Entry 4", 9f));
      }};

    int[] expectedColors = {
        color,
        color,
        color,
        color
    };

    setColorsHelper(colorList, tuples, expectedColors);
  }

  /**
   * Test to ensure that setting the Colors property
   * with a List containing multiple entries and a
   * color for each entry properly sets the colors of
   * the Data Series and the color for each individual entry.
   */
  @Test
  public void testSetColorsMultipleColors() {
    List<Integer> colorList = new ArrayList<Integer>() {{
        add(Color.RED);
        add(Color.GREEN);
        add(Color.BLUE);
        add(Color.YELLOW);
        add(Color.CYAN);
      }};

    List<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple("Entry", 32f));
        add(createTuple("Entry 2", 40f));
        add(createTuple("Entry 3", 25f));
        add(createTuple("Entry 4", 15f));
        add(createTuple("Entry 5", 10f));
      }};

    int[] expectedColors = {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW,
        Color.CYAN
    };

    setColorsHelper(colorList, tuples, expectedColors);
  }

  /**
   * Test to ensure that setting the Colors property
   * with a List containing multiple entries and fewer
   * colors than entries properly sets the colors of
   * the Data Series and the color for each individual entry
   * is alternated properly between the set colors.
   */
  @Test
  public void testSetColorsMultipleEntriesRepeatColors() {
    List<Integer> colorList = new ArrayList<Integer>() {{
        add(Color.RED);
        add(Color.GREEN);
        add(Color.BLUE);
      }};

    List<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple("Entry", 32f));
        add(createTuple("Entry 2", 40f));
        add(createTuple("Entry 3", 25f));
        add(createTuple("Entry 4", 15f));
        add(createTuple("Entry 5", 10f));
        add(createTuple("Entry 6", 7f));
        add(createTuple("Entry 7", 3f));
      }};

    int[] expectedColors = {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.RED
    };

    setColorsHelper(colorList, tuples, expectedColors);
  }

  /**
   * Test to ensure that checking for criterion satisfaction with the
   * X Value criterion and a matching x value which is a String returns true.
   */
  @Test
  public void testCriterionSatisfiedXStringMatch() {
    Entry entry = createEntry("Entry", 4f);
    final DataModel.EntryCriterion criterion = DataModel.EntryCriterion.XValue;
    final String value = "Entry";

    boolean result = model.isEntryCriterionSatisfied(entry, criterion, value);
    assertTrue(result);
  }

  /**
   * Test case to ensure that adding a single entry
   * adds the appropriate Legend Entry with the x value
   * as the label and the value color as the color.
   */
  @Test
  public void testCheckLegendSingleEntry() {
    final int color = Color.RED;
    model.setColor(color);

    YailList tuple = createTuple("Entry", 5f);
    model.addEntryFromTuple(tuple);

    LegendEntry[] expectedEntries = {
        createLegendEntry("Entry", Color.RED)
    };

    checkLegendHelper(expectedEntries);
  }

  /**
   * Test case to ensure that adding multiple entries
   * to the Data Series adds the appropriate Legend
   * Entries representing the values (x values as labels
   * and value colors as Legend colors).
   */
  @Test
  public void testCheckLegendMultipleEntries() {
    final int color = Color.BLUE;
    model.setColor(color);

    List<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple("Entry 1", 5f));
        add(createTuple("Entry 2", 3f));
        add(createTuple("test", 7f));
        add(createTuple("test 2", 1f));
      }};

    model.importFromList(tuples);

    LegendEntry[] expectedEntries = {
        createLegendEntry("Entry 1", color),
        createLegendEntry("Entry 2", color),
        createLegendEntry("test", color),
        createLegendEntry("test 2", color)
    };

    checkLegendHelper(expectedEntries);
  }

  /**
   * Test case to ensure that adding entries from
   * different Data Models adds the appropriate entries
   * corresponding to each entry of the separate data
   * series.
   */
  @Test
  public void testCheckLegendDifferentDataModelEntries() {
    final int color1 = Color.RED;
    final int color2 = Color.GREEN;
    final int color3 = Color.BLUE;

    PieChartDataModel model2 = (PieChartDataModel) chartView.createChartModel();
    PieChartDataModel model3 = (PieChartDataModel) chartView.createChartModel();

    model.setColor(color1);
    model2.setColor(color2);
    model3.setColor(color3);

    model.addEntryFromTuple(createTuple("Entry 1", 5f));
    model2.addEntryFromTuple(createTuple("Model 2 Entry", 9f));
    model3.addEntryFromTuple(createTuple("Model 3 Entry", 1f));
    model.addEntryFromTuple(createTuple("Entry 2", 10f));

    LegendEntry[] expectedEntries = {
        createLegendEntry("Entry 1", color1),
        createLegendEntry("Model 2 Entry", color2),
        createLegendEntry("Model 3 Entry", color3),
        createLegendEntry("Entry 2", color1)
    };

    checkLegendHelper(expectedEntries);
  }

  /**
   * Test case to ensure that removing a single entry
   * from the Data Series removes the corresponding
   * Legend Entry properly.
   */
  @Test
  public void testCheckLegendRemoveEntry() {
    final int color = Color.BLUE;
    model.setColor(color);

    List<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple("Entry 1", 5f));
        add(createTuple("Entry 2", 3f));
        add(createTuple("Entry 3", 7f));
        add(createTuple("Entry 4", 1f));
      }};

    model.importFromList(tuples);
    model.removeEntryFromTuple(createTuple("Entry 2", 3f));

    LegendEntry[] expectedEntries = {
        createLegendEntry("Entry 1", color),
        createLegendEntry("Entry 3", color),
        createLegendEntry("Entry 4", color)
    };

    checkLegendHelper(expectedEntries);
  }

  /**
   * Test case to ensure that removing a single entry
   * from the Data Series removes the corresponding
   * Legend Entry properly.
   */
  @Test
  public void testCheckLegendRemoveMultipleEntries() {
    List<Integer> colorList = new ArrayList<Integer>() {{
        add(Color.RED);
        add(Color.GREEN);
        add(Color.BLUE);
        add(Color.CYAN);
        add(Color.YELLOW);
      }};

    model.setColors(colorList);

    List<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple("Entry", 5f));
        add(createTuple("Test", 5f));
        add(createTuple("Random", 1f));
        add(createTuple("Slice", 12f));
        add(createTuple("Pie Slice", 10f));
        add(createTuple("test", 17f));
      }};

    model.importFromList(tuples);
    model.removeEntryFromTuple(createTuple("Entry", 5f));
    model.removeEntryFromTuple(createTuple("Slice", 12f));
    model.removeEntryFromTuple(createTuple("test", 17f));

    LegendEntry[] expectedEntries = {
        createLegendEntry("Test", Color.RED),
        createLegendEntry("Random", Color.GREEN),
        createLegendEntry("Pie Slice", Color.BLUE)
    };

    checkLegendHelper(expectedEntries);
  }

  /**
   * Test case to ensure that clearing the entries from
   * the Data Series removes all the Legend entries.
   */
  @Test
  public void testCheckLegendClearEntries() {
    List<Integer> colorList = new ArrayList<Integer>() {{
        add(Color.RED);
        add(Color.GREEN);
        add(Color.BLUE);
        add(Color.CYAN);
      }};

    model.setColors(colorList);

    List<YailList> tuples = new ArrayList<YailList>() {{
        add(createTuple("Entry", 5f));
        add(createTuple("Test", 5f));
        add(createTuple("Random", 1f));
        add(createTuple("Slice", 12f));
      }};

    model.importFromList(tuples);
    model.clearEntries();

    LegendEntry[] expectedEntries = new LegendEntry[0];

    checkLegendHelper(expectedEntries);
  }

  /**
   * Test case to ensure that removing entries from
   * different Data Models properly changes the
   * Legend of the Data model.
   */
  @Test
  public void testCheckLegendRemoveEntriesFromMultipleDataSets() {
    final int color1 = Color.CYAN;
    final int color2 = Color.MAGENTA;
    final int color3 = Color.YELLOW;

    PieChartDataModel model2 = (PieChartDataModel) chartView.createChartModel();
    PieChartDataModel model3 = (PieChartDataModel) chartView.createChartModel();

    model.setColor(color1);
    model2.setColor(color2);
    model3.setColor(color3);

    model.addEntryFromTuple(createTuple("Entry 1", 2f));
    model2.addEntryFromTuple(createTuple("Model 2 Entry 1", 1f));
    model.addEntryFromTuple(createTuple("Entry 2", 7f));
    model2.addEntryFromTuple(createTuple("Model 2 Entry 1", 1f));
    model.addEntryFromTuple(createTuple("Entry 3", 10f));
    model3.addEntryFromTuple(createTuple("Model 3 Entry", 7f));

    model.removeEntryFromTuple(createTuple("Entry 2", 7f));
    model2.removeEntryFromTuple(createTuple("Model 2 Entry 1", 1f));

    LegendEntry[] expectedEntries = {
        createLegendEntry("Entry 1", color1),
        createLegendEntry("Model 2 Entry 1", color2),
        createLegendEntry("Entry 3", color1),
        createLegendEntry("Model 3 Entry", color3)
    };

    checkLegendHelper(expectedEntries);
  }

  /**
   * Test to ensure that importing from columns of uneven
   * size (where the x column is smaller) fills in missing
   * x values with empty Strings.
   */
  @Test
  public void testImportFromCsvUnevenColumnsXValue() {
    YailList xcolumn = createTuple("X", "Entry 1", "Entry 2");
    YailList ycolumn = createTuple("Y", 20f, 30f, 40f, 50f);

    ArrayList<Entry> expectedEntries = new ArrayList<Entry>() {{
        add(createEntry("Entry 1", 20f));
        add(createEntry("Entry 2", 30f));
        add(createEntry("", 40f));
        add(createEntry("", 50f));
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
        add(createTuple("Test", 3f));
        add(createTuple("Test", 2f));
        add(createTuple("Entry", 1f));
        add(createTuple("Entry", 3f));
        add(createTuple("Entry", 3f));
        add(createTuple("Entry 2", 4f));
      }};

    Entry searchEntry = createEntry("Entry", 3f);
    final int expectedIndex = 3;

    findEntryIndexHelper(tuples, searchEntry, expectedIndex);
  }

  private void checkLegendHelper(LegendEntry[] expectedEntries) {
    assertEquals(expectedEntries.length, legendEntries.size());

    for (int i = 0; i < expectedEntries.length; ++i) {
      LegendEntry expected = expectedEntries[i];
      LegendEntry actual = legendEntries.get(i);

      assertEquals(expected.label, actual.label);
      assertEquals(expected.formColor, actual.formColor);
    }
  }

  private void setColorsHelper(List<Integer> colorList, List<YailList> tuples,
      int[] expectedColors) {
    model.setColors(colorList);
    model.importFromList(tuples);

    assertEquals(colorList, model.getDataset().getColors());
    assertEquals(expectedColors.length, legendEntries.size());

    for (int i = 0; i < expectedColors.length; ++i) {
      LegendEntry legendEntry = legendEntries.get(i);

      assertEquals(expectedColors[i], legendEntry.formColor);
    }
  }

  @Override
  public void setup() {
    chartView = new PieChartView(new Chart(getForm()));
    model = (PieChartDataModel) chartView.createChartModel();
    data = (PieData) model.getData();
    legendEntries = chartView.getLegendEntries();
  }

  @Override
  protected void assertEntriesEqual(Entry e1, Entry e2) {
    assertEquals(e1.getClass(), e2.getClass());
    assertEquals(((PieEntry) e1).getLabel(), ((PieEntry) e2).getLabel());
    assertEquals(e1.getY(), e2.getY());
  }

  @Override
  protected YailList createTuple(Object... entries) {
    if (entries.length != 0) {
      entries[0] = entries[0].toString();
    }

    return super.createTuple(entries);
  }

  @Override
  protected Entry createEntry(Object... entries) {
    String x = entries[0].toString();
    float y = (float) entries[1];

    return new PieEntry(y, x);
  }

  private LegendEntry createLegendEntry(String x, int color) {
    LegendEntry legendEntry = new LegendEntry();
    legendEntry.label = x;
    legendEntry.formColor = color;
    return legendEntry;
  }
}
