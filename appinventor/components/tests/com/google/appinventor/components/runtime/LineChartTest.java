package com.google.appinventor.components.runtime;

import android.view.View;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.LineData;
import com.google.appinventor.components.runtime.util.ChartModel;
import com.google.appinventor.components.runtime.util.LineChartModel;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * Integration tests for Line Charts.
 *
 * Meant to test Line Chart-specific functionality and interaction with the MPAndroidChart
 * library classes.
 */
public class LineChartTest extends ChartBaseTest {
    /**
     * Tests that the view is of type Line Chart.
     */
    @Test
    public void testLineChartType() {
        View view = chart.getView();

        assertThat(view, instanceOf(com.github.mikephil.charting.charts.LineChart.class));
    }

    /**
     * Tests that the data object is of type Line Data.
     */
    @Test
    public void testLineChartDataType() {
        ChartData data = chart.data;

        assertThat(data, instanceOf(LineData.class));
    }

    /**
     * Tests that the creation of a Chart Model returns the proper
     * object type instance with the correct Data component set to it.
     */
    @Test
    public void testCreateChartModel() {
        ChartModel model = chart.createChartModel();

        assertThat(model, instanceOf(LineChartModel.class));
        assertEquals(chart.data, model.getData());
    }

    @Before
    public void setUp() {
        super.setUp();
        chart = new LineChart(getForm()); // Chart type does not matter, since this is an abstract class test
    }
}