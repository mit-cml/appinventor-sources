// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.github.mikephil.charting.charts.LineChart;

import com.google.appinventor.components.common.ChartType;

import org.junit.Before;

/**
 * Tests for the Line Chart type.
 *
 * <p>The class tests Line Chart-specific functionality, interaction
 * between the View component and the Chart component, as well as
 * integration with the MPAndroidChart library classes.
 */
public class LineChartTest extends AbstractAxisChartTest<LineChart, LineChartView> {
  /**
   * Prepare the test.
   */
  @Before
  public void setUp() {
    super.setUp();

    chartView = (LineChartView) chartComponent.getChartView();
    chart = (LineChart) chartView.getView();
    xAxisValueFormatter = chart.getXAxis().getValueFormatter();
  }

  @Override
  public void testChartType() {
    assertEquals(getType(), chartComponent.Type());
    assertThat(chart, instanceOf(LineChart.class));
    assertThat(chartView, instanceOf(LineChartView.class));
  }

  @Override
  public void testCreateChartModel() {
    ChartDataModel<?, ?, ?, ?, ?> model = chartComponent.createChartModel();

    assertThat(model, instanceOf(LineChartDataModel.class));
    assertEquals(chart.getData(), model.getData());
  }

  @Override
  public ChartType getType() {
    return ChartType.Line;
  }
}
