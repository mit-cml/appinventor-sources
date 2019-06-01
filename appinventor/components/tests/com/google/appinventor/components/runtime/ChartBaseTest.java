package com.google.appinventor.components.runtime;

import android.graphics.drawable.ColorDrawable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChartBaseTest extends RobolectricTestBase {
    private ChartBase chart;

    /**
     * Test to ensure that Chart has the expected default properties.
     */
    @Test
    public void testChartBaseDefaults() {
        Assert.assertEquals(Component.COLOR_DEFAULT, chart.BackgroundColor());
        Assert.assertEquals("", chart.Description());
    }

    /**
     * Tests that the Chart's Description setter sets the Description
     * of the Chart properly.
     */
    @Test
    public void testDescription() {
        String description = "Chart Title Test";
        chart.Description(description);

        Assert.assertEquals(description, chart.Description());
        Assert.assertEquals(description, chart.view.getDescription().getText());
    }

    /**
     * Tests that the Chart's BackgroundColor setter sets the Background
     * Color of the Chart properly.
     */
    @Test
    public void testBackgroundColor() {
        int argb = 0xffaabbcc;
        chart.BackgroundColor(argb);

        Assert.assertEquals(argb, chart.BackgroundColor());

        // Assert that color has actually changed in the view
        ColorDrawable drawable = (ColorDrawable)chart.view.getBackground();
        Assert.assertEquals(argb, drawable.getColor());
    }

    @Before
    public void setUp() {
        super.setUp();
        chart = new LineChart(getForm()); // Chart type does not matter, since this is an abstract class test
        chart.getView().requestLayout();
    }
}