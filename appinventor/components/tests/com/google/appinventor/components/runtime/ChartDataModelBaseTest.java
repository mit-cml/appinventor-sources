package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;
import org.junit.Before;
import org.junit.Test;

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
}
