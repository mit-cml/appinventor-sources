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
import com.google.appinventor.components.common.ComponentConstants;

import org.junit.Before;

public class AreaChartTest extends AbstractAxisChartTest<LineChart, AreaChartView> {

  /**
   * Configures a AreaChart for testing.
   */
  @Before
  public void setUp() {
    super.setUp();

    chartView = (AreaChartView) chartComponent.getChartView();
    chart = (LineChart) chartView.getView();
    xAxisValueFormatter = chart.getXAxis().getValueFormatter();
  }

  @Override
  public void testChartType() {
    assertEquals(getType(), chartComponent.Type());
    assertThat(chart, instanceOf(LineChart.class));
    assertThat(chartView, instanceOf(AreaChartView.class));
  }

  @Override
  public void testCreateChartModel() {
    ChartDataModel<?, ?, ?, ?, ?> model = chartComponent.createChartModel();

    assertThat(model, instanceOf(AreaChartDataModel.class));
    assertEquals(chart.getData(), model.getData());
  }

  @Override
  public ChartType getType() {
    return ChartType.Area;
  }
}
