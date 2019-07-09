package com.google.appinventor.components.runtime;

import android.graphics.drawable.ColorDrawable;
import android.widget.RelativeLayout;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

/**
 * Abstract test class for the Chart class.
 *
 * Contains test cases for functionality independent of the Chart type.
 */
public abstract class AbstractChartTest<V extends ChartView,
        C extends com.github.mikephil.charting.charts.Chart> extends RobolectricTestBase {
    protected V chartView;
    protected C chart;
    protected Chart chartComponent;

    @Before
    public void setUp() {
        super.setUp();
        chartComponent = new Chart(getForm());
    }

    /**
     * Test to ensure that Chart has the expected default properties.
     */
    @Test
    public void testChartConstructorDefaultProperties() {
        Chart defaultChart = new Chart(getForm());

        assertEquals(Component.CHART_TYPE_LINE, defaultChart.Type());
        assertEquals(Component.COLOR_DEFAULT, defaultChart.BackgroundColor());
        assertEquals("", defaultChart.Description());
        assertTrue(defaultChart.getView().isEnabled());
    }

    /**
     * Tests that the Chart's Description setter sets the Description
     * of the Chart properly.
     */
    @Test
    public void testDescription() {
        String description = "Chart Title Test";
        chartComponent.Description(description);

        assertEquals(description, chartComponent.Description());
        assertEquals(description, chart.getDescription().getText());
    }

    /**
     * Tests that the Chart's BackgroundColor setter sets the Background
     * Color of the Chart properly.
     */
    @Test
    public void testBackgroundColor() {
        int argb = 0xffaabbcc;
        chartComponent.BackgroundColor(argb);

        assertEquals(argb, chartComponent.BackgroundColor());

        // Assert that color has actually changed in the view
        ColorDrawable drawable = (ColorDrawable)chart.getBackground();
        assertEquals(argb, drawable.getColor());
    }

    /**
     * Tests that upon changing the Chart's Type property,
     * the current child Views of the root layout is removed,
     * and a new one is added.
     */
    @Test
    public void testChangeTypeReAddView() {
        // Get the root layout of the Chart
        RelativeLayout relativeLayout = (RelativeLayout)chartComponent.getView();

        // Assert that the current view is in the root layout, and
        // the getChartView method returns the proper result.
        assertEquals(chartView, chartComponent.getChartView());
        assertEquals(chart, relativeLayout.getChildAt(0));

        // Change the Type of the Chart
        chartComponent.Type(0);

        // Assert that the getChartView method no longer returns
        // the removed chartView
        assertNotSame(chartView, chartComponent.getChartView());

        // Assert that the root layout only has 1 view, and it
        // is not the old Chart view
        assertNotSame(chart, relativeLayout.getChildAt(0));
        assertEquals(1, relativeLayout.getChildCount());
    }

    /**
     * Test to ensure that upon changing the Chart's type,
     * the necessary properties are reset.
     */
    @Test
    public void testChangeTypeReinitializeProperties() {
        String description = "Chart Title Test";
        int argb = 0xffaabbcc;

        chartComponent.Description(description);
        chartComponent.BackgroundColor(argb);
        chartComponent.Type(0);

        assertEquals(description, chart.getDescription().getText());
        assertEquals(argb, ((ColorDrawable)chart.getBackground()).getColor());
    }

    /**
     * Test to ensure that upon changing the Chart's Type,
     * the attached Data components are reinitialized.
     */
    @Test
    public void testChangeTypeReinitializeChartDataComponents() {
        ArrayList<ChartDataBase> dataComponents = new ArrayList<ChartDataBase>();

        for (int i = 0; i < 3; ++i) {
            // Create a mock Data component, and expect an initChartData
            // method call.
            ChartDataBase dataComponent = EasyMock.createMock(ChartDataBase.class);
            dataComponent.initChartData();
            expectLastCall();
            replay(dataComponent);

            // Add the Data Component to the Chart
            chartComponent.addDataComponent(dataComponent);
            dataComponents.add(dataComponent);
        }

        chartComponent.Type(0);

        // Verify initChartData() method calls for all the
        // attached Data components.
        for (ChartDataBase dataComponent : dataComponents) {
            verify(dataComponent);
        }
    }

    /**
     * Tests that the Chart's Type property initializes the
     * views of the right types.
     */
    @Test
    public abstract void testChartType();

    /**
     * Tests that after changing the Type property, a Chart
     * Model of the proper type is created.
     */
    @Test
    public abstract void testCreateChartModel();
}