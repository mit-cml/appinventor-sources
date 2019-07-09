package com.google.appinventor.components.runtime;

import android.widget.RelativeLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.LineData;
import org.junit.Before;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class LineChartViewTest extends AbstractChartTest<LineChartView, LineChart> {
    private final static int TYPE = 0;

    @Before
    public void setUp() {
        super.setUp();

        chartComponent.Type(TYPE);

        chartView = (LineChartView)chartComponent.getChartView();
        chart = chartView.getView();
    }

    @Override
    public void testChartType() {
        assertEquals(TYPE, chartComponent.Type());
        assertThat(chart, instanceOf(LineChart.class));
        assertThat(chartView, instanceOf(LineChartView.class));
    }

    @Override
    public void testCreateChartModel() {
        ChartDataModel model = chartComponent.createChartModel();

        assertThat(model, instanceOf(LineChartDataModel.class));
        assertEquals(chart.getData(), model.getData());
    }
}