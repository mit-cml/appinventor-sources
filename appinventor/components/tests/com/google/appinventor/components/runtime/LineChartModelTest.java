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
    public void testImportFromTinyDB() {
        HashMap<String, String> valueMap = new HashMap<String, String>() {{
            put("0", "1");
            put("1", "2");
            put("2", "5");
            put("4", "3");
        }};


        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
           put(0f, 1f);
           put(1f, 2f);
           put(2f, 5f);
           put(4f, 3f);
        }};

        testImportFromTinyDBHelper(valueMap, expectedValues);
    }

    @Test
    public void testImportFromTinyDBEmpty() {

    }

    /**
     * Helper method that sets up a mock TinyDB, calls the required method to
     * import the values from a TinyDB component, and handles all the assertions
     * based on the passed in arguments.
     *
     * Values form valueMap will be parsed sequentially in the model method, whereas
     * the expectedValues are the values which are expected to be in the Data Series
     * data after parsing.
     *
     * @param valueMap  Values that the mock TinyDB should return
     * @param expectedValues  Values that are expected to be in the Data Series
     */
    @SuppressWarnings("unchecked")
    private void testImportFromTinyDBHelper(HashMap<String, String> valueMap, HashMap<Float, Float> expectedValues) {
        // Set up TinyDB mock and expected values
        TinyDB tinyDB = EasyMock.createNiceMock(TinyDB.class);

        // Make the TinyDB get all values method to return the created map.
        // We need this generic cast here, otherwise EasyMock will
        // give an error
        expect(tinyDB.getAllValues()).andReturn((Map)valueMap);
        replay(tinyDB);

        // Import the data from the TinyDB component
        model.importFromTinyDB(tinyDB);

        // Assert that the proper values are added
        assertEquals(expectedValues.size(), model.getDataset().getEntryCount());

        // Start from the first entry in the Data Series
        int index = 0;

        // Iterate over all the expected values
        for (Map.Entry<Float, Float> expectedEntry : expectedValues.entrySet()) {
            // Get the actual Entry added to the Data Series
            Entry entry = model.getDataset().getEntryForIndex(index);

            // Assert expxected x and y values
            assertEquals(expectedEntry.getKey(), entry.getX());
            assertEquals(expectedEntry.getValue(), entry.getY());

            // Move on to the next Data Series entry
            index++;
        }
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