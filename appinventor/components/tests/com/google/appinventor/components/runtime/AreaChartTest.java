package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.LineChart;
import com.google.appinventor.components.common.ComponentConstants;
import org.junit.Before;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class AreaChartTest extends AbstractChartTest<AreaChartView, LineChart> {
  @Before
  public void setUp() {
    super.setUp();

    chartView = (AreaChartView) chartComponent.getChartView();
    chart = (LineChart) chartView.getView();
  }

  @Override
  public void testChartType() {
    assertEquals(getType(), chartComponent.Type());
    assertThat(chart, instanceOf(LineChart.class));
    assertThat(chartView, instanceOf(AreaChartView.class));
  }

  @Override
  public void testCreateChartModel() {
    ChartDataModel model = chartComponent.createChartModel();

    assertThat(model, instanceOf(AreaChartDataModel.class));
    assertEquals(chart.getData(), model.getData());
  }

  @Override
  public int getType() {
    return ComponentConstants.CHART_TYPE_AREA;
  }
}