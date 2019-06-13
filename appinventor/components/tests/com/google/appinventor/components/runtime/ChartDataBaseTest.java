package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.ChartModel;
import junit.framework.Assert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Unit tests for the ChartDataBase abstract class.
 *
 * Instantiates a CoordinateData subclass with mock dependencies.
 * The class is meant to test common functionality of the ChartData
 * component that is independent of the subclass used.
 */
public class ChartDataBaseTest {
    private ChartDataBase chartData;

    private ChartBase chart;
    private ChartModel model;

    /**
     * Helper method to set up mock Chart and Model object
     * instances.
     */
    private void setupMocks() {
        // Create nice mocks of Chart and Model objects.
        // Nice mocks used here because most of the method
        // calls done via setup should not be accounted for
        // in the tests.
        chart = EasyMock.createNiceMock(ChartBase.class);
        model = EasyMock.createNiceMock(ChartModel.class);

        // Make the Chart return the mccked Model object instance
        expect(chart.createChartModel()).andReturn(model);

        // Replay should be called after. Not done here because
        // more functionality will be added in other cases.
    }

    /**
     * Sets up all the Chart components.
     *
     * Mocks are registered (replayed) and chartData is
     * initialized.
     */
    private void setupChartComponents() {
        replay(chart);
        replay(model);
        chartData = new CoordinateData(chart);
    }

    @Before
    public void setup() {
        setupMocks();
    }

    /**
     * Tests that the references in the constructor of the
     * ChartData component are set to the proper objects.
     */
    @Test
    public void testConstructorReferences() {
        setupChartComponents();

        assertEquals(chart, chartData.container);
        assertEquals(model, chartData.chartModel);
    }

    /**
     * Tests that the default properties are set correctly after
     * instantiating a ChartData component.
     */
    @Test
    public void testChartDataBaseDefaults() {
        setupChartComponents();

        assertEquals(Component.COLOR_BLACK, chartData.Color());
        assertEquals("", chartData.Label());
    }

    /**
     * Tests that the ChartData's Color setter sets
     * the Color of the Data Series properly.
     */
    @Test
    public void testColor() {
        int argb = 0xffaabbcc;

        // Set up the mock to expect the setColor method call
        model.setColor(argb);
        expectLastCall();
        setupChartComponents();

        // Change the color of the Data Series
        chartData.Color(argb);

        // Assert the value change and the proper method call.
        Assert.assertEquals(argb, chartData.Color());
        verify(model);
    }

    /**
     * Tests that the ChartData's Label setter sets
     * the Label of the Data Series properly.
     */
    @Test
    public void testLabel() {
        String label = "Test label";

        // Set up the mock to expect the setLabel method call
        model.setLabel(label);
        expectLastCall();
        setupChartComponents();

        // Change the label of the Data Series
        chartData.Label(label);

        // Assert the value change and the proper method call
        Assert.assertEquals(label, chartData.Label());
        verify(model);
    }
}