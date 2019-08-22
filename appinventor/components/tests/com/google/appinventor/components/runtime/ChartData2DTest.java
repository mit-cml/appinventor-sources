package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Test;
import org.robolectric.android.util.concurrent.RoboExecutorService;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ChartData2DTest extends RobolectricTestBase {
  private Chart chartComponent;
  private ChartData2D data;
  private ChartDataModel model;

  @Override
  public void setUp() {
    super.setUp();
    chartComponent = new Chart(getForm());
    data = new ChartData2D(chartComponent);
    model = data.chartDataModel;

    // The ExecutorService used by the ChartData component has
    // to be changed to a RoboExecutorService in order to
    // make unit tests wait for the tasks to be finished.
    data.setExecutorService(new RoboExecutorService());
  }

  @Test
  public void testSetColor() {
    int argb = 0xFFAABBCC;
    data.Color(argb);
    assertEquals(argb, data.Color());
    assertEquals(argb, model.getDataset().getColor());
  }

  @Test
  public void testSetLabel() {
    String label = "Test Label";
    data.Label(label);
    assertEquals(label, data.Label());
    assertEquals(label, model.getDataset().getLabel());
  }

  @Test
  public void testSetColorsSingleColor() {
    YailList colors = YailList.makeList(
        Collections.singletonList(0xFFAABBCC));

    data.Colors(colors);

    assertEquals(colors, data.Colors());
    assertEquals(1, model.getDataset().getColors().size());
  }

  @Test
  public void testSetColorsMultipleColors() {
    YailList colors = YailList.makeList(
        Arrays.asList(0xFFAABBCC, 0xFFBBCCDD, 0xAABBCCDD)
    );

    data.Colors(colors);

    assertEquals(colors, data.Colors());
    assertEquals(3, model.getDataset().getColors().size());
  }

  @Test
  public void testSetColorsInvalidEntries() {
    YailList colors = YailList.makeList(
        Arrays.asList(0xFFAABBCC, "random-entry", 0xBBCCDDEE,
            "string")
    );

    data.Colors(colors);

    YailList expectedColors = YailList.makeList(
        Arrays.asList(0xFFAABBCC, 0xBBCCDDEE)
    );

    assertEquals(expectedColors, data.Colors());
    assertEquals(2, model.getDataset().getColors().size());
  }

  @Test
  public void testAddEntry() {
    assertEquals(0, model.getDataset().getEntryCount());
    data.AddEntry("3", "2");
    assertEquals(1, model.getDataset().getEntryCount());
  }

  @Test
  public void testRemoveEntry() {
    data.AddEntry("1", "1");
    assertEquals(1, model.getDataset().getEntryCount());

    data.RemoveEntry("1", "1");
    assertEquals(0, model.getDataset().getEntryCount());
  }

  @Test
  public void testEntryExistsTrue() {
    data.AddEntry("1", "3");
    assertTrue(data.DoesEntryExist("1", "3"));
  }

  @Test
  public void testEntryExistsFalse() {
    data.AddEntry("4", "2");
    assertFalse(data.DoesEntryExist("4", "1"));
  }

  @Test
  public void testImportFromList() {
    assertEquals(0, model.getDataset().getEntryCount());

    YailList entriesList = YailList.makeList(
        Arrays.asList(
            Arrays.asList("1", "1"),
            Arrays.asList("2", "2"),
            Arrays.asList("4", "1")
        )
    );

    data.ImportFromList(entriesList);

    assertEquals(3, model.getDataset().getEntryCount());
  }

  @Test
  public void testClear() {
    data.AddEntry("1", "3");
    data.AddEntry("2", "1");
    assertEquals(2, model.getDataset().getEntryCount());

    data.Clear();
    assertEquals(0, model.getDataset().getEntryCount());
  }

  @Test
  public void testGetAllEntries() {
    data.AddEntry("0", "5");
    data.AddEntry("2", "3");
    data.AddEntry("5", "1");

    YailList entries = data.GetAllEntries();

    assertEquals(3, entries.size());
  }

  @Test
  public void testGetEntriesByXValue() {
    data.AddEntry("1", "2");
    data.AddEntry("5", "4");

    YailList entries = data.GetEntriesWithXValue("1");

    assertEquals(1, entries.size());
  }

  @Test
  public void testGetEntriesByYValue() {
    data.AddEntry("0", "3");
    data.AddEntry("1", "3");
    data.AddEntry("2", "4");

    YailList entries = data.GetEntriesWithYValue("3");

    assertEquals(2, entries.size());
  }

  @Test
  public void testSetPointShape() {
    chartComponent.Type(ComponentConstants.CHART_TYPE_SCATTER);
    model = data.chartDataModel;

    IShapeRenderer renderer = ((ScatterDataSet)model.getDataset()).getShapeRenderer();

    data.PointShape(ComponentConstants.CHART_POINT_STYLE_TRIANGLE);
    assertNotSame(renderer, ((ScatterDataSet)model.getDataset()).getShapeRenderer());
  }

  @Test
  public void testSetLineType() {
    LineDataSet.Mode mode = ((LineDataSet)model.getDataset()).getMode();

    data.LineType(ComponentConstants.CHART_LINE_TYPE_STEPPED);
    assertNotSame(mode, ((LineDataSet)model.getDataset()).getMode());
  }

  @Test
  public void testSetElementsFromPairsNull() {
    data.ElementsFromPairs(null);
    assertEquals(0, model.getDataset().getEntryCount());
  }

  @Test
  public void testSetElementsFromPairsEmpty() {
    data.ElementsFromPairs("");
    assertEquals(0, model.getDataset().getEntryCount());
  }

  @Test
  public void testSetElementsFromPairs() {
    data.onBeforeInitialize();
    data.ElementsFromPairs("0,1,1,4,2,5");
    assertEquals(3, model.getDataset().getEntryCount());
  }

  @Test
  public void testImportFromDataFile() {

  }
}