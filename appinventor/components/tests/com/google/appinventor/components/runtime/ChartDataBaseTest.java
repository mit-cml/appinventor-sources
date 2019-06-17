package com.google.appinventor.components.runtime;

import junit.framework.Assert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Abstract class for Unit tests of ChartData components.
 */
public abstract class ChartDataBaseTest<T extends ChartDataBase> {
    protected T chartData;
    protected ChartBase chart;
    protected ChartModel model;

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
     * Mocks are registered (replayed) and chartData should
     *  be initialized on implementations.
     */
    protected void setupChartComponents() {
        replay(chart);
        replay(model);
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

    /**
     * Tests that the ChartData's Elements setter
     * calls the right method of the ChartModel object.
     */
    @Test
    public void testElementsFromPairs() {
        String elements = "1, 1, 2, 2";

        // Set up the mock to expect the setElements method call
        model.setElements(elements);
        expectLastCall();
        setupChartComponents();

        // Change the elements of the Data Series
        chartData.ElementsFromPairs(elements);

        // Verify the method call
        verify(model);
    }

    /**
     * Tests that the ChartData's ImportFromTinyDB method
     * calls the proper method in the Chart model.
     */
    @Test
    public void testImportFromTinyDB() {
        // Create a mock TinyDB object
        TinyDB tinyDB = EasyMock.createMock(TinyDB.class);

        // Expect the ImportFromTinyDB method call in model
        model.importFromTinyDB(tinyDB);
        setupChartComponents();

        chartData.ImportFromTinyDB(tinyDB);

        // Verify that the required method was called
        verify(model);
    }
}