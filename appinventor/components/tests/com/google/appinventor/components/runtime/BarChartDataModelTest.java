package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import static junit.framework.Assert.assertEquals;

public class BarChartDataModelTest extends ChartDataModel2DTest<BarChartDataModel, BarData> {

  @Override
  public void setup() {
    data = new BarData();
    model = new BarChartDataModel(data);
  }

  @Override
  public void testAddEntryFromTuplePair() {

  }

  @Override
  public void testFindEntryIndexExists() {

  }

  @Override
  public void testRemoveValuesInvalidEntries() {

  }

  @Override
  public void testImportFromCSVUnevenColumns() {

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
  public void testImportFromCSVManyEntries() {

  }

  @Override
  public void testImportFromCSVOneEntry() {

  }

  @Override
  public void testRemoveValuesSingleValue() {

  }

  @Override
  public void testImportFromListMixedEntries() {

  }

  @Override
  public void testRemoveFromTupleExists() {

  }

  @Override
  public void testRemoveValuesEmpty() {

  }

  @Override
  public void testImportFromListInvalidEntries() {

  }

  @Override
  public void testAddTimeEntryExceedsMaximum() {

  }

  @Override
  public void testRemoveFromTupleNonExistent() {

  }

  @Override
  public void testSetElementsInvalid() {

  }

  @Override
  public void testSetElementsOdd() {

  }

  @Override
  public void testSetElementsEven() {

  }

  @Override
  public void testImportFromListDuplicates() {

  }

  @Override
  public void testImportFromListBiggerTuples() {

  }

  @Override
  public void testImportFromListGenericList() {

  }

  @Override
  public void testRemoveFromTupleMultipleEntries() {

  }

  @Override
  public void testGetEntriesAsTuplesMultipleEntries() {

  }

  @Override
  public void testImportFromListMultipleEntries() {

  }

  @Override
  public void testRemoveValuesNonExistentValues() {

  }

  @Override
  public void testImportFromListGenericListEntries() {

  }

  @Override
  public void testFindEntryIndexExistsMultiple() {

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