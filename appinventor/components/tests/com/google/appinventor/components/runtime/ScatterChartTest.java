package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.ScatterChart;
import com.google.appinventor.components.common.ComponentConstants;
import org.junit.Before;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ScatterChartTest extends AbstractChartTest<ScatterChartView, ScatterChart> {
  @Before
  public void setUp() {
    super.setUp();

    chartView = (ScatterChartView) chartComponent.getChartView();
    chart = chartView.getView();
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
