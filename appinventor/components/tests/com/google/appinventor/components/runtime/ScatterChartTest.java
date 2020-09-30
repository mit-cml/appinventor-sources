// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.ScatterChart;
import com.google.appinventor.components.common.ComponentConstants;
import org.junit.Before;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ScatterChartTest extends AbstractAxisChartTest<ScatterChartView, ScatterChart> {
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
    ChartDataModel model = chartComponent.createChartModel();

    assertThat(model, instanceOf(ScatterChartDataModel.class));
    assertEquals(chart.getData(), model.getData());
  }

  @Override
  public int getType() {
    return ComponentConstants.CHART_TYPE_SCATTER;
  }
}
