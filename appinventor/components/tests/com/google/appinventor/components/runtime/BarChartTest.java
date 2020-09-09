// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.BarChart;
import com.google.appinventor.components.common.ComponentConstants;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

public class BarChartTest extends AbstractAxisChartTest<BarChartView, BarChart> {
  @Before
  public void setUp() {
    super.setUp();

    chartView = (BarChartView) chartComponent.getChartView();
    chart = (BarChart) chartView.getView();
    xAxisValueFormatter = chart.getXAxis().getValueFormatter();
  }

  @Override
  public void testChartType() {
    assertEquals(getType(), chartComponent.Type());
    assertThat(chart, instanceOf(BarChart.class));
    assertThat(chartView, instanceOf(BarChartView.class));
  }

  @Override
  public void testCreateChartModel() {
    ChartDataModel model = chartComponent.createChartModel();

    assertThat(model, instanceOf(BarChartDataModel.class));
    assertEquals(chart.getData(), model.getData());
  }

  /**
   * Test case to ensure that creating multiple Chart models
   * changes the bar width of the Bar Chart (effective resizing
   * to fit groups of bars)
   */
  @Test
  public void testCreateMultipleChartModels() {
    ChartDataModel model1 = chartComponent.createChartModel();
    float barWidth = chart.getData().getBarWidth();

    ChartDataModel model2 = chartComponent.createChartModel();
    assertNotSame(chart.getData().getBarWidth(), barWidth);
    barWidth = chart.getData().getBarWidth();

    ChartDataModel model3 = chartComponent.createChartModel();
    assertNotSame(chart.getData().getBarWidth(), barWidth);
  }

  @Override
  public int getType() {
    return ComponentConstants.CHART_TYPE_BAR;
  }
}
