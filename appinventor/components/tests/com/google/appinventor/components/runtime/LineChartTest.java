package com.google.appinventor.components.runtime;

import android.view.View;
import com.github.mikephil.charting.data.LineData;
import com.google.appinventor.components.runtime.util.ChartModel;
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
     * Tests to make sure that when there is no data on the Chart, the created
     */
//    @Test
//    public void testCreateChartModelNoData() {
//        // Get current LineData object of the underlying Chart view object
//        LineData currentData = (LineData)chart.view.getData();
//
//        // Create a ChartModel object instance
//        ChartModel model = chart.createChartModel();
//
//        // Make sure that model data is not equal to Chart view data.
//        // This is because the data is initially empty, so it should not
//        // be assigned to the Chart.
//        assertNotSame(model.getData(), currentData);
//
//        // Make sure that data is of correct type
//        assertThat(model.getData(), instanceOf(LineData.class));
//    }

    @Before
    public void setUp() {
        super.setUp();
        chart = new LineChart(getForm()); // Chart type does not matter, since this is an abstract class test
    }
}