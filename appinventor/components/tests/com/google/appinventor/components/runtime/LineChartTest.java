package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * Tests for the Line Chart type.
 *
 * The class tests Line Chart-specific functionality, interaction
 * between the View component and the Chart component, as well as
 * integration with the MPAndroidChart library classes.
 */
public class LineChartTest extends AxisChartTest<LineChartView, LineChart> {
    @Before
    public void setUp() {
        super.setUp();

        chartView = (LineChartView)chartComponent.getChartView();
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
        ChartDataModel model = chartComponent.createChartModel();

        assertThat(model, instanceOf(LineChartDataModel.class));
        assertEquals(chart.getData(), model.getData());
    }

    @Override
    public int getType() {
        return ComponentConstants.CHART_TYPE_LINE;
    }
}