package com.google.appinventor.components.runtime;

import android.view.View;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class LineChartTest extends RobolectricTestBase {
    private LineChart chart;

    /**
     * Tests that the view is of type Line Chart.
     */
    @Test
    public void testLineChartType() {
        View view = chart.getView();

        assertThat(view, instanceOf(com.github.mikephil.charting.charts.LineChart.class));
    }

    @Before
    public void setUp() {
        super.setUp();
        chart = new LineChart(getForm()); // Chart type does not matter, since this is an abstract class test
        chart.getView().requestLayout();
    }
}