package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Test;

import java.util.Arrays;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.verify;

/**
 * Unit tests for ChartData2D component.
 */
public class ChartData2DTest extends ChartDataBaseTest<ChartData2D> {
    @Override
    protected void setupChartComponents() {
        super.setupChartComponents();
        chartData = new ChartData2D(chart);
    }

    /**
     * Tests that the add entry method calls the proper method in
     * the Chart model with the correct values.
     */
    @Test
    public void testAddEntry() {
        float x = 3;
        float y = 4;

        // Except add entry on the model
        model.addEntryFromTuple(YailList.makeList(Arrays.asList(x, y)));
        expectLastCall();
        setupChartComponents();

        // Call the Add Entry method in the ChartData component
        // and verify that the appropriate model method was called.
        chartData.AddEntry(x + "", y + "");
        verify(model);
    }
}