package com.google.appinventor.components.runtime;

import org.junit.Test;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.verify;

/**
 * Unit tests for CoordinateData component.
 */
public class CoordinateDataTest extends ChartDataBaseTest<CoordinateData> {
    @Override
    protected void setupChartComponents() {
        super.setupChartComponents();
        chartData = new CoordinateData(chart);
    }

    @Test
    public void testAddEntry() {
        float x = 3;
        float y = 4;

        // Except add entry on the model
        model.addEntry(x, y);
        expectLastCall();
        setupChartComponents();

        // Call the Add Entry method in the ChartData component
        // and verify that the appropriate model method was called.
        chartData.AddEntry(x, y);
        verify(model);
    }
}