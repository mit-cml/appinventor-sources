// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import android.graphics.drawable.ColorDrawable;

import android.view.View;

import android.widget.RelativeLayout;

import com.google.appinventor.components.common.ChartType;

import java.util.ArrayList;

import org.easymock.EasyMock;

import org.junit.Before;
import org.junit.Test;

/**
 * Abstract test class for the Chart class.
 *
 * <p>Contains test cases for functionality independent of the Chart type.</p>
 */
public abstract class AbstractChartTest<
    C extends com.github.mikephil.charting.charts.Chart<?>,
    V extends ChartView<?, ?, ?, C, V>>
    extends RobolectricTestBase {
  protected V chartView;
  protected C chart;
  protected Chart chartComponent;

  /**
   * Configures a new Chart component for testing.
   */
  @Before
  public void setUp() {
    super.setUp();
    chartComponent = new Chart(getForm());

    // Set the corresponding Type of the Chart according
    // to the test class in use.
    chartComponent.Type(getType());
  }

  /**
   * Test to ensure that Chart has the expected default properties.
   */
  @Test
  public void testChartConstructorDefaultProperties() {
    Chart defaultChart = new Chart(getForm());

    assertEquals(ChartType.Line, defaultChart.Type());
    assertEquals(Component.COLOR_WHITE, defaultChart.BackgroundColor());
    assertEquals("", defaultChart.Description());
    assertTrue(defaultChart.getView().isEnabled());
  }

  /**
   * Tests that the Chart's Description setter sets the Description
   * of the Chart properly.
   */
  @Test
  public void testDescription() {
    String description = "Chart Title Test";
    chartComponent.Description(description);

    assertEquals(description, chartComponent.Description());
    assertEquals(description, chart.getDescription().getText());
  }

  /**
   * Tests that the Chart's BackgroundColor setter sets the Background
   * Color of the Chart properly.
   */
  @Test
  public void testBackgroundColor() {
    int argb = 0xffaabbcc;
    chartComponent.BackgroundColor(argb);

    assertEquals(argb, chartComponent.BackgroundColor());

    // Assert that color has actually changed in the view
    ColorDrawable drawable = (ColorDrawable) chart.getBackground();
    assertEquals(argb, drawable.getColor());
  }

  /**
   * Tests that upon changing the Chart's Type property,
   * the current child Views of the root layout is removed,
   * and a new one is added.
   */
  @Test
  public void testChangeTypeReAddView() {
    // Get the root layout of the Chart
    RelativeLayout relativeLayout = (RelativeLayout) chartComponent.getView();

    // Assert that the current view is in the root layout, and
    // the getChartView method returns the proper result.
    assertEquals(chartView, chartComponent.getChartView());

    // The Chart view is expected to be the first view of the Relative Layout
    View child = relativeLayout.getChildAt(0);

    // Since the Pie Chart instantiates a RelativeLayout instead of the
    // Chart itself in the Chart componentt Relative Layout view,
    // separate handling has to be done here for the Pie Chart.
    // TODO: Refactor this to be in separate corresponding classes.
    if (getType() == ChartType.Pie) {
      // The Root Chart view is the child of the RelativeLayout which is
      // a child of the Chart component's RelativeLayout.
      assertEquals(chart, ((RelativeLayout) child).getChildAt(0));
    } else {
      assertEquals(chart, child);
    }

    // Change the Type of the Chart
    chartComponent.Type(ChartType.Line);

    // Assert that the getChartView method no longer returns
    // the removed chartView
    assertNotSame(chartView, chartComponent.getChartView());

    // Assert that the root layout only has 1 view, and it
    // is not the old Chart view
    assertNotSame(chart, relativeLayout.getChildAt(0));
    assertEquals(1, relativeLayout.getChildCount());
  }

  /**
   * Test to ensure that upon changing the Chart's type,
   * the necessary properties are reset.
   */
  @Test
  public void testChangeTypeReinitializeProperties() {
    String description = "Chart Title Test";
    int argb = 0xffaabbcc;

    chartComponent.Description(description);
    chartComponent.BackgroundColor(argb);
    chartComponent.Type(ChartType.Line);

    assertEquals(description, chart.getDescription().getText());
    assertEquals(argb, ((ColorDrawable) chart.getBackground()).getColor());
  }

  /**
   * Test to ensure that upon changing the Chart's Type,
   * the attached Data components are reinitialized.
   */
  @Test
  public void testChangeTypeReinitializeChartDataComponents() {
    ArrayList<ChartDataBase> dataComponents = new ArrayList<>();

    for (int i = 0; i < 3; ++i) {
      // Create a mock Data component, and expect an initChartData
      // method call.
      ChartDataBase dataComponent = EasyMock.createMock(ChartDataBase.class);
      dataComponent.initChartData();
      expectLastCall();
      replay(dataComponent);

      // Add the Data Component to the Chart
      chartComponent.addDataComponent(dataComponent);
      dataComponents.add(dataComponent);
    }

    chartComponent.Type(ChartType.Line);

    // Verify initChartData() method calls for all the
    // attached Data components.
    for (ChartDataBase dataComponent : dataComponents) {
      verify(dataComponent);
    }
  }

  /**
   * Tests that the Chart's Type property initializes the
   * views of the right types.
   */
  @Test
  public abstract void testChartType();

  /**
   * Tests that after changing the Type property, a Chart
   * Model of the proper type is created.
   */
  @Test
  public abstract void testCreateChartModel();

  /**
   * Test case to ensure that retrieving synced t values
   * consecutively in increasing order returns the
   * appropriate results.
   */
  @Test
  public void testGetSyncedTValueConsecutive() {
    // Get t values in order (0, 1, 2) and verify that the
    // proper values are returned in order.
    int t = chartComponent.getSyncedTValue(0);
    assertEquals(0, t);

    t = chartComponent.getSyncedTValue(1);
    assertEquals(1, t);

    t = chartComponent.getSyncedTValue(2);
    assertEquals(2, t);
  }


  /**
   * Test case to ensure that retrieving synced t values
   * updates the locally synced t value properly.
   */
  @Test
  public void testGetSyncedTValueUpdateT() {
    // Get t value of 4
    int t = chartComponent.getSyncedTValue(4);
    assertEquals(4, t);

    // Now the local t value stored is 5; Getting the t value
    // of 4 should still return 4 (since the difference is not
    // bigger than 1)
    t = chartComponent.getSyncedTValue(4);
    assertEquals(4, t);

    // Get the t value of 5 (should return 5). Now local
    // value is 6.
    t = chartComponent.getSyncedTValue(5);
    assertEquals(5, t);

    // Difference between 6 and 4 is 2, which is bigger than 1,
    // so the local t value should be returned instead.
    t = chartComponent.getSyncedTValue(4);
    assertEquals(6, t);
  }

  /**
   * Test case to ensure that retrieving a synced t value
   * with an argument that is far smaller than the synced
   * t value returns the local synced t value instead.
   */
  @Test
  public void testGetSyncedTValueSmaller() {
    // Get the t value of 9 (expected result is 9, since it
    // is way larger than the locally stored 0 value)
    int t = chartComponent.getSyncedTValue(9);
    assertEquals(9, t);

    // Get a far smaller value; Expected result is 10 (9 + 1), which
    // is the locally stored synced t value.
    t = chartComponent.getSyncedTValue(4);
    assertEquals(10, t);
  }

  /**
   * Returns the type of the Chart (integer representation).
   *
   * @return Chart Type (integer)
   */
  public abstract ChartType getType();
}
