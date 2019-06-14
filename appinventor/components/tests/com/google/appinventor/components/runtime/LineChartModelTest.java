package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.google.appinventor.components.runtime.LineChartModel;
import com.google.appinventor.components.runtime.RobolectricTestBase;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the LineChartModel class.
 */
public class LineChartModelTest extends RobolectricTestBase {
    /*
     * TBD: abstract common properties (i.e. Label & Color)
     */


    private com.google.appinventor.components.runtime.LineChartModel model;
    private LineData data;

    @Before
    public void setup() {
        data = new LineData();
        model = new LineChartModel(data);
    }

    /**
     * Test to ensure that the constructor properly instantiates an
     * empty data set, and that the reference of the passed in data
     * object instance is not broken.
     */
    @Test
    public void testConstructor() {
        Assert.assertEquals(data, model.getData());
        Assert.assertEquals(1, data.getDataSetCount());
        Assert.assertEquals(0, data.getDataSetByIndex(0).getEntryCount());
    }

    /**
     * Tests whether the setLabel method correctly changes the label
     * of the Data Series.
     */
    @Test
    public void testSetLabel() {
        String label = "Test Label Text";
        model.setLabel(label);
        Assert.assertEquals(label, model.getDataset().getLabel());
    }

    /**
     * Tests whether the setColor method correctly changes the color
     * of the Data Series.
     */
    @Test
    public void testSetColor() {
        int argb = 0xFFEEDDCC;
        model.setColor(argb);
        Assert.assertEquals(argb, model.getDataset().getColor());
    }

    /**
     * Tests whether an entry is correctly added to the Data Set
     * upon calling the add entry method with x and y coordinates.
     */
    @Test
    public void testAddEntry() {
        // Pre-condition: make sure there are no entries initially
        Assert.assertEquals(0, model.getDataset().getEntryCount());

        // Add an entry
        float x = 4;
        float y = 5;
        model.addEntry(x, y);

        // Ensure that the entry has been added
        Assert.assertEquals(1, model.getDataset().getEntryCount());

        // Make sure that a correct entry has been added
        Entry entry = model.getDataset().getEntryForIndex(0);
        Assert.assertEquals(x, entry.getX());
        Assert.assertEquals(y, entry.getY());
    }

    /**
     * Tests to ensure that Data Series entries are not changed
     * when passing in invalid input via setEelements.
     */
    @Test
    public void testSetElementsInvalid() {
        String elements = "1, 2, 3, 4, A, B";

        model.setElements(elements);

        // Make sure that the method was abruptly cut because
        // of invalid entries detected.
        Assert.assertEquals(0, model.getDataset().getEntryCount());
    }

    /**
     * Tests to ensure that the entries are properly parsed
     * and ordered when using the setElements method.
     */
    @Test
    public void testSetElementsEven() {
        // We are adding (3, 1), (2, 4), (1, 2)
        // End result should be ordered by X value.
        String elements = "3, 1, 2, 4, 1, 2";
        model.setElements(elements);

        // Make sure that 3 elements have been added
        Assert.assertEquals(3, model.getDataset().getEntryCount());

        // Verify the 3 entries
        Entry entry1 = model.getDataset().getEntryForIndex(0);
        Assert.assertEquals(1f, entry1.getX());
        Assert.assertEquals(2f, entry1.getY());

        Entry entry2 = model.getDataset().getEntryForIndex(1);
        Assert.assertEquals(2f, entry2.getX());
        Assert.assertEquals(4f, entry2.getY());

        Entry entry3 = model.getDataset().getEntryForIndex(2);
        Assert.assertEquals(3f, entry3.getX());
        Assert.assertEquals(1f, entry3.getY());
    }

    /**
     * Tests to ensure that the last entry is cut
     * off to make the list even, and the proper
     * entries are added.
     */
    @Test
    public void testSetElementsOdd() {
        // List length is odd
        String elements = "1, 3, 5";
        model.setElements(elements);

        // Only the first entry should be added (cut off last value
        // since pairs are accepted)
        Assert.assertEquals(1, model.getDataset().getEntryCount());

        // Make sure entry is correct
        Entry entry = model.getDataset().getEntryForIndex(0);
        Assert.assertEquals(1f, entry.getX());
        Assert.assertEquals(3f, entry.getY());
    }
}