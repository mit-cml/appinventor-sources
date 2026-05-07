// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.hamcrest.CoreMatchers.instanceOf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LegendEntry;

import com.google.appinventor.components.common.ChartType;
import com.google.appinventor.components.common.ComponentConstants;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for the Pie Chart View.
 * The test class also tests the integration with the Chart Component
 * with the given view.
 *
 * @see com.google.appinventor.components.runtime.AbstractChartTest
 */
public class PieChartTest extends AbstractChartTest<PieChart, PieChartView> {
  private RelativeLayout rootView;

  /**
   * Prepare the test.
   */
  @Before
  public void setUp() {
    super.setUp();

    chartView = (PieChartView) chartComponent.getChartView();
    chartView.createChartModel(); // Creates root Pie Chart
    rootView = (RelativeLayout) chartView.getView();
    chart = (PieChart) rootView.getChildAt(0);
  }

  @Override
  public void testChartType() {
    assertEquals(getType(), chartComponent.Type());
    assertThat(chartView.getView(), instanceOf(RelativeLayout.class));
    assertThat(chart, instanceOf(PieChart.class));
    assertThat(chartView, instanceOf(PieChartView.class));
  }

  @Override
  public void testCreateChartModel() {
    // Get model and assert that it is of the appropriate type
    ChartDataModel<?, ?, ?, ?, ?> model = chartView.createChartModel();
    assertThat(model, instanceOf(PieChartDataModel.class));

    // Assert that the new Pie Chart ring has been added to the root ivew
    assertEquals(2, rootView.getChildCount());

    // Get the newly added inner Chart
    PieChart innerChart = (PieChart) rootView.getChildAt(1);

    // Assert that the newly added Inner Chart's data instance is
    // equal to the model's Data instance
    assertEquals(innerChart.getData(), model.getData());

    // Assert that the root Chart's & Inner Chart's data instances
    // are not the same
    assertNotSame(chart.getData(), innerChart.getData());
  }

  /**
   * Test case to ensure that setting the Pie Radius
   * of the Chart while the type is of type Pie Chart
   * actually has effect on the radius.
   *
   * <p>Since the calculated values are non-direct, for
   * simplicity, this test case tests a non-full Pie Chart
   * fill and a full fill, the difference between them being
   * that the full fill disables hole drawing, and the partial
   * fill enables it.
   */
  @Test
  public void testChangePieRadius() {
    final int firstRadius = 50;
    final int newRadius = 100;

    // Set the Pie Radius to non-full and
    // assert that the hole is drawn
    chartComponent.PieRadius(firstRadius);
    assertTrue(chart.isDrawHoleEnabled());

    // Set the Pie Radius to 100 (full fill)
    // and assert that the hole is no longer drawn
    chartComponent.PieRadius(newRadius);
    assertFalse(chart.isDrawHoleEnabled());
  }

  /**
   * Test case to ensure that adding a single Legend Entry properly
   * adds it to the root Pie Chart's Legend.
   */
  @Test
  public void testAddSingleLegendEntry() {
    LegendEntry entry = new LegendEntry();
    entry.label = "Label";

    chartView.addLegendEntry(entry);

    List<LegendEntry> entries = chartView.getLegendEntries();

    assertEquals(1, entries.size());
    assertEquals(entry, entries.get(0));
  }

  /**
   * Test case to ensure that adding multiple LEgnd Entries properly
   * adds them to the root Pie Chart's Legend.
   */
  @Test
  public void testAddMultipleLegendEntries() {
    final int entries = 5;
    List<LegendEntry> expectedEntries = new ArrayList<>();

    for (int i = 0; i < entries; ++i) {
      LegendEntry entry = new LegendEntry();
      entry.label = "Entry " + i;

      // Add Entry to both expected
      expectedEntries.add(entry);
      chartView.addLegendEntry(entry);
    }

    // Get the LegendEntries array and assert that the count of entries
    // is the same as the expected count
    List<LegendEntry> legendEntries = chartView.getLegendEntries();
    assertEquals(entries, legendEntries.size());

    for (int i = 0; i < entries; ++i) {
      // Get the expected and actual LegendEntries, and
      // assert that they are equal
      LegendEntry expected = expectedEntries.get(i);
      LegendEntry actual = legendEntries.get(i);
      assertEquals(expected, actual);
    }
  }

  /**
   * Test case to ensure that removing a Legend Entry from the
   * Pie Chart View properly removes it form the root Pie Chart's
   * legend as well.
   */
  @Test
  public void testRemoveLegendEntry() {
    int entries = 4;
    List<LegendEntry> expectedEntries = new ArrayList<>();

    for (int i = 0; i < entries; ++i) {
      LegendEntry entry = new LegendEntry();
      entry.label = "Entry " + i;

      // Add entry to both the Chart View and the expected entries List
      expectedEntries.add(entry);
      chartView.addLegendEntry(entry);
    }

    // Remove the first entry from both the expected entries List
    // and the Chart View itself
    LegendEntry removeEntry = expectedEntries.remove(0);
    chartView.removeLegendEntry(removeEntry);

    // Get the LegendEntries of the chart, and assert that the
    // size with the expected entries is equivalent
    List<LegendEntry> legendEntries = chartView.getLegendEntries();
    assertEquals(expectedEntries.size(), legendEntries.size());

    for (int i = 0; i < expectedEntries.size(); ++i) {
      // Get both the expected and actual Legend Entries,
      // and assert that they are equal
      LegendEntry expected = expectedEntries.get(i);
      LegendEntry actual = legendEntries.get(i);
      assertEquals(expected, actual);
    }
  }

  /**
   * Test case to ensure that attempting to remove
   * a LegendEntry that does not exist in the Legend of
   * the root Pie Chart view does not remove any entries
   * at all.
   */
  @Test
  public void testRemoveLegendEntryNonExistent() {
    // Create a new Legend Entry and add it to the Legend
    LegendEntry entry = new LegendEntry();
    entry.label = "Entry";
    chartView.addLegendEntry(entry);

    // Create a different Legend Entry and attempt to remove it
    // from the Legend
    LegendEntry removeEntry = new LegendEntry();
    removeEntry.label = "Test";
    chartView.removeLegendEntry(removeEntry);

    // Get the entries of the Legend and assert that no entries
    // have been removed.
    List<LegendEntry> legendEntries = chartView.getLegendEntries();
    assertEquals(1, legendEntries.size());
    assertEquals(entry, legendEntries.get(0));
  }

  @Override
  public ChartType getType() {
    return ChartType.Pie;
  }
}
