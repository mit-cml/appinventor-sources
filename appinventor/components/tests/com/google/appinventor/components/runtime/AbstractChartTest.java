package com.google.appinventor.components.runtime;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.RelativeLayout;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

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

    @Test
    public void testChangeTypeReAddView() {
        RelativeLayout relativeLayout = (RelativeLayout)chartComponent.getView();

        assertEquals(chartView, chartComponent.getChartView());
        assertEquals(chart, relativeLayout.getChildAt(0));

        chartComponent.Type(0);

        assertNotSame(chartView, chartComponent.getChartView());
        assertNotSame(chart, relativeLayout.getChildAt(0));
        assertEquals(1, relativeLayout.getChildCount());
    }

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

    @Test
    public void testChangeTypeReinitializeChartDataComponents() {
        ArrayList<ChartDataBase> dataComponents = new ArrayList<ChartDataBase>();

        for (int i = 0; i < 3; ++i) {
            ChartDataBase dataComponent = EasyMock.createMock(ChartDataBase.class);
            dataComponent.initChartData();
            expectLastCall();
            replay(dataComponent);

            chartComponent.addDataComponent(dataComponent);
            dataComponents.add(dataComponent);
        }

        chartComponent.Type(0);

        for (ChartDataBase dataComponent : dataComponents) {
            verify(dataComponent);
        }
    }

    /**
     * Tests that the Chart's Type property sets the appropriate type
     * for the Chart and initializes the views of the right types.
     */
    @Test
    public abstract void testChartType();
}