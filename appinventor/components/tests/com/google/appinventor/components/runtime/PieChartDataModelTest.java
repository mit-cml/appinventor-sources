package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieEntry;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

public class PieChartDataModelTest extends ChartDataModel2DTest<PieChartDataModel, PieData> {
  private PieChartView chartView;

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
      add(createEntry("1", 3f));
      add(createEntry("2", 5f));
      add(createEntry("3", -3f));
      add(createEntry("4", 7f));
    }};

    importFromCSVHelper(expectedEntries, xColumn, yColumn);
  }


  /**
   * Test to ensure that importing from a pair containing
   * valid values with an x value being a String label
   * adds the entry properly.
   */
  @Test
  public void testAddEntryFromTupleLabel() {
    final String xValue = "Entry";
    final float yValue = 25f;

    YailList tuple = createTuple(xValue, yValue);
    model.addEntryFromTuple(tuple);

    Entry entry = model.getDataset().getEntryForIndex(0);
    Entry expectedEntry = createEntry(xValue, yValue);

    assertEquals(1, model.getDataset().getEntryCount());
    assertEntriesEqual(expectedEntry, entry);

    // TODO: Test LegendEntry addition
  }

  @Override
  public void setup() {
    chartView = new PieChartView(getForm());
    model = (PieChartDataModel)chartView.createChartModel();
    data = (PieData) model.getData();
  }

  @Override
  protected void assertEntriesEqual(Entry e1, Entry e2) {
    assertEquals(e1.getClass(), e2.getClass());
    assertEquals(((PieEntry)e1).getLabel(), ((PieEntry)e2).getLabel());
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
    String xValue = entries[0].toString();
    float yValue = (float) entries[1];

    return new PieEntry(yValue, xValue);
  }
}
