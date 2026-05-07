// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.github.mikephil.charting.charts.ScatterChart;

import com.google.appinventor.components.common.ChartType;

import org.junit.Before;

public class ScatterChartTest extends AbstractAxisChartTest<ScatterChart, ScatterChartView> {
  /**
   * Prepare the test.
   */
  @Before
  public void setUp() {
    super.setUp();

    chartView = (ScatterChartView) chartComponent.getChartView();
    chart = (ScatterChart) chartView.getView();
    xAxisValueFormatter = chart.getXAxis().getValueFormatter();
  }

  @Override
  public void testChartType() {
    assertEquals(getType(), chartComponent.Type());
    assertThat(chart, instanceOf(ScatterChart.class));
    assertThat(chartView, instanceOf(ScatterChartView.class));
  }

  @Override
  public void testCreateChartModel() {
    ChartDataModel<?, ?, ?, ?, ?> model = chartComponent.createChartModel();

    assertThat(model, instanceOf(ScatterChartDataModel.class));
    assertEquals(chart.getData(), model.getData());
  }

  @Override
  public ChartType getType() {
    return ChartType.Scatter;
  }
}
