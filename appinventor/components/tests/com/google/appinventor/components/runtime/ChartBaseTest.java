package com.google.appinventor.components.runtime;

import android.graphics.drawable.ColorDrawable;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.verify;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 * Abstract test class for the ChartBase class.
 *
 * Contains test cases for functionality independent of the Chart subclass.
 */
public abstract class ChartBaseTest<T extends ChartBase> extends RobolectricTestBase {
    protected T chart;

    /**
     * Test to ensure that Chart has the expected default properties.
     */
    @Test
    public void testChartBaseDefaults() {
        Assert.assertEquals(Component.COLOR_DEFAULT, chart.BackgroundColor());
        Assert.assertEquals("", chart.Description());
        Assert.assertTrue(chart.getView().isEnabled());
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

    /**
     * Test to ensure that the proper interactions are made
     * when refreshing the Chart and data is present.
     *
     * Relies on mocks to verify interactions.
     */
    @Test
    public void testRefresh() {
        // Mock a ChartData object
        ChartData chartDataMock = EasyMock.createMock(ChartData.class);

        // Set up the mock; expected notifyDataChanged call
        chartDataMock.notifyDataChanged();
        replay(chartDataMock);

        // Refresh the Chart with a mock Chart view
        testRefreshHelper(chartDataMock);

        // Verify method call
        verify(chartDataMock);
    }

    /**
     * Test to ensure that the proper interactions are made
     * when refreshing the Chart when no data is present.
     *
     * Relies on mocks to verify interactions.
     */
    @Test
    public void testRefreshDataNull() {
        testRefreshHelper(null);
    }

    /**
     * Helper method to test the Refresh method in the
     * Chart component.
     *
     * The test relies on mocking to verify method calls
     * to the Chart view.
     * @param data  ChartData object to set to the Chart view
     */
    private void testRefreshHelper(ChartData data) {
        // Create mock objects for the Chart view and the Chart Data
        Chart chartMock = EasyMock.createMock(Chart.class);

        // Assign mock Chart as view for the Chart
        chart.view = chartMock;

        // If data is present, chartMock.getData() will be called
        // twice.
        int times = (data == null) ? 1 : 2;

        // Return non-null data object
        expect(chartMock.getData()).andReturn(data).times(times);

        // Expect refresh methods to be called on the Chart Mock
        chartMock.notifyDataSetChanged();
        chartMock.invalidate();
        expectLastCall();

        // Replay mock
        replay(chartMock);

        // Call the appropriate method in the ChartBase class
        chart.Refresh();

        // Verify all the method calls for the mock Chart
        verify(chartMock);
    }

    @Before
    public void setUp() {
        super.setUp();
    }
}