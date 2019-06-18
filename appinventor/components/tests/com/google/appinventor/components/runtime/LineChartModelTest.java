package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.google.appinventor.components.runtime.LineChartModel;
import com.google.appinventor.components.runtime.RobolectricTestBase;
import junit.framework.Assert;
import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 * Unit tests for the LineChartModel class.
 */
public class LineChartModelTest extends RobolectricTestBase {
    /*
     * TBD: abstract common properties (i.e. Label & Color)
     */


    private LineChartModel model;
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
        assertEquals(data, model.getData());
        assertEquals(1, data.getDataSetCount());
        assertEquals(0, data.getDataSetByIndex(0).getEntryCount());
    }

    /**
     * Tests whether the setLabel method correctly changes the label
     * of the Data Series.
     */
    @Test
    public void testSetLabel() {
        String label = "Test Label Text";
        model.setLabel(label);
        assertEquals(label, model.getDataset().getLabel());
    }

    /**
     * Tests whether the setColor method correctly changes the color
     * of the Data Series.
     */
    @Test
    public void testSetColor() {
        int argb = 0xFFEEDDCC;
        model.setColor(argb);
        assertEquals(argb, model.getDataset().getColor());
    }

    /**
     * Tests whether an entry is correctly added to the Data Set
     * upon calling the add entry method with x and y coordinates.
     */
    @Test
    public void testAddEntry() {
        // Pre-condition: make sure there are no entries initially
        assertEquals(0, model.getDataset().getEntryCount());

        // Add an entry
        float x = 4;
        float y = 5;
        model.addEntry(x, y);

        // Ensure that the entry has been added
        assertEquals(1, model.getDataset().getEntryCount());

        // Make sure that a correct entry has been added
        Entry entry = model.getDataset().getEntryForIndex(0);
        assertEquals(x, entry.getX());
        assertEquals(y, entry.getY());
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
        assertEquals(0, model.getDataset().getEntryCount());
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
        assertEquals(3, model.getDataset().getEntryCount());

        // Verify the 3 entries
        Entry entry1 = model.getDataset().getEntryForIndex(0);
        assertEquals(1f, entry1.getX());
        assertEquals(2f, entry1.getY());

        Entry entry2 = model.getDataset().getEntryForIndex(1);
        assertEquals(2f, entry2.getX());
        assertEquals(4f, entry2.getY());

        Entry entry3 = model.getDataset().getEntryForIndex(2);
        assertEquals(3f, entry3.getX());
        assertEquals(1f, entry3.getY());
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
        assertEquals(1, model.getDataset().getEntryCount());

        // Make sure entry is correct
        Entry entry = model.getDataset().getEntryForIndex(0);
        assertEquals(1f, entry.getX());
        assertEquals(3f, entry.getY());
    }

    /**
     * Tests to ensure that TinyDB data importing
     * works as expected in the LineChartModel.
     * A mock TinyDB is used with pre-defined returning
     * of all entries.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testImportFromTinyDB() {
        // Set up TinyDB mock and expected values
        TinyDB tinyDB = EasyMock.createNiceMock(TinyDB.class);

        HashMap<String, String> map = new HashMap<String, String>();

        // We will be adding (0, 1), (1, 2), (2, 5) and (4, 3) entries
        map.put("0", "1");
        map.put("1", "2");
        map.put("2", "5");
        map.put("4", "3");

        // We need this generic cast here, otherwise EasyMock will
        // give an error
        expect(tinyDB.getAllValues()).andReturn((Map)map);
        replay(tinyDB);

        // Import the data from the TinyDB component
        model.importFromTinyDB(tinyDB);

        // Assert that the proper values are added
        assertEquals(4, model.getDataset().getEntryCount());

        Entry entry1 = model.getDataset().getEntryForIndex(0);
        Entry entry2 = model.getDataset().getEntryForIndex(1);
        Entry entry3 = model.getDataset().getEntryForIndex(2);
        Entry entry4 = model.getDataset().getEntryForIndex(3);

        assertEquals(0f, entry1.getX());
        assertEquals(1f, entry1.getY());

        assertEquals(1f, entry2.getX());
        assertEquals(2f, entry2.getY());

        assertEquals(2f, entry3.getX());
        assertEquals(5f, entry3.getY());

        assertEquals(4f, entry4.getX());
        assertEquals(3f, entry4.getY());
    }

    /**
     * Test to ensure that the Clear Entries method deletes
     * all the entries from the Data Series.
     */
    @Test
    public void testClearEntries() {
        model.addEntry(4, 5);
        model.addEntry(3, 2);
        model.addEntry(1, 4);

        assertEquals(3, model.getDataset().getEntryCount());

        model.clearEntries();

        assertEquals(0, model.getDataset().getEntryCount());
    }
}