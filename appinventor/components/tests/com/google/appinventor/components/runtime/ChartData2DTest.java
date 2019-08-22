package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Test;
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
    ShadowApplication.runBackgroundTasks();
    assertEquals(1, model.getDataset().getEntryCount());
  }

  @Test
  public void testRemoveEntry() {
    data.AddEntry("1", "1");
    assertEquals(1, model.getDataset().getEntryCount());

    data.RemoveEntry("1", "1");
    assertEquals(0, model.getDataset().getEntryCount());
  }
}