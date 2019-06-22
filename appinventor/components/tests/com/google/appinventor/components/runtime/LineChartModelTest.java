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
import java.util.List;
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
     * Tests to ensure that TinyDB data importing works as
     * expected in the LineChartModel.
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

    /**
     * Tests to ensure that TinyDB data importing
     * works as expected when the data is imported
     * from an empty TinyDB component.
     */
    @Test
    public void testImportFromTinyDBEmpty() {
        HashMap<String, String> valueMap = new HashMap<String, String>();
        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>();

        testImportFromTinyDBHelper(valueMap, expectedValues);
    }

    /**
     * Tests to ensure that entries imported from a TinyDB component
     * with an invalid X value are discarded, while still parsing the
     * entries with valid values.
     */
    @Test
    public void testImportFromTinyDBInvalidX() {
        HashMap<String, String> valueMap = new HashMap<String, String>() {{
            put("string", "3");
            put("0", "5");
        }};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
            put(0f, 5f);
        }};

        testImportFromTinyDBHelper(valueMap, expectedValues);
    }

    /**
     * Tests to ensure that entries imported from a TinyDB component
     * with an invalid Y value are discarded, while still parsing the
     * entries with valid values.
     */
    @Test
    public void testImportFromTinyDBInvalidY() {
        HashMap<String, String> valueMap = new HashMap<String, String>() {{
            put("1", "string");
            put("0", "5");
        }};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
            put(0f, 5f);
        }};

        testImportFromTinyDBHelper(valueMap, expectedValues);
    }

    /**
     * Tests to ensure that entries imported from a TinyDB component
     * with invalid X and Y values are discarded, while still parsing the
     * entries with valid values.
     */
    @Test
    public void testImportFromTinyDBInvalidXY() {
        HashMap<String, String> valueMap = new HashMap<String, String>() {{
            put("-1", "3");
            put("string", "string");
            put("0", "5");
        }};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
            put(-1f, 3f);
            put(0f, 5f);
        }};

        testImportFromTinyDBHelper(valueMap, expectedValues);
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
     * Test to ensure that importing from two lists
     * yields the correct entries in the Data Series.
     */
    @Test
    public void testImportFromLists() {
        String[] xValues = new String[] {"-2", "0", "1", "3", "5"};
        String[] yValues = new String[] {"3", "7", "5", "4", "3"};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
            put(-2f, 3f);
            put(0f, 7f);
            put(1f, 5f);
            put(3f, 4f);
            put(5f, 3f);
        }};

        testImportFromListsHelper(xValues, yValues, expectedValues);
    }

    /**
     * Test to ensure that importing from empty Lists does
     * not add any new entries.
     */
    @Test
    public void testImportFromListsEmpty() {
        String[] xValues = new String[] {};
        String[] yValues = new String[] {};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>();

        testImportFromListsHelper(xValues, yValues, expectedValues);
    }

    /**
     * Test to ensure that importing from Lists that contain
     * an invalid X entry skip the invalid entry, while importing
     * the rest.
     */
    @Test
    public void testImportFromListsInvalidX() {
        String[] xValues = new String[] {"1", "string", "3"};
        String[] yValues = new String[] {"4", "5", "3"};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
            put(1f, 4f);
            put(3f, 3f);
        }};

        testImportFromListsHelper(xValues, yValues, expectedValues);
    }

    /**
     * Test to ensure that importing from Lists that contain
     * an invalid Y entry skip the invalid entry, while importing
     * the rest.
     */
    @Test
    public void testImportFromListsInvalidY() {
        String[] xValues = new String[] {"1", "5", "3"};
        String[] yValues = new String[] {"4", "string", "2"};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
            put(1f, 4f);
            put(3f, 2f);
        }};

        testImportFromListsHelper(xValues, yValues, expectedValues);
    }

    /**
     * Test to ensure that importing from Lists that contain
     * an entry with both an invalid X and Y entry result in skipping the
     * invalid entry, while importing the rest.
     */
    @Test
    public void testImportFromListsInvalidXY() {
        String[] xValues = new String[] {"2", "string", "3"};
        String[] yValues = new String[] {"4", "string", "9"};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
            put(2f, 4f);
            put(3f, 9f);
        }};

        testImportFromListsHelper(xValues, yValues, expectedValues);
    }

    /**
     * Test to ensure that a larger X values list results in
     * skipping of the excess entries in the Y values list.
     */
    @Test
    public void testImportFromListsXSmaller() {
        String[] xValues = new String[] {"1", "2"};
        String[] yValues = new String[] {"7", "3", "4"};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
            put(1f, 7f);
            put(2f, 3f);
        }};

        testImportFromListsHelper(xValues, yValues, expectedValues);
    }

    /**
     * Test to ensure that a larger Y values list results in
     * skipping of the excess entries in the X values list.
     */
    @Test
    public void testImportFromListsYSmaller() {
        String[] xValues = new String[] {"0", "5", "7", "3"};
        String[] yValues = new String[] {"4", "3", "9"};

        HashMap<Float, Float> expectedValues = new HashMap<Float, Float>() {{
            put(0f, 4f);
            put(5f, 3f);
            put(7f, 9f);
        }};

        testImportFromListsHelper(xValues, yValues, expectedValues);
    }

    /**
     * Helper method that calls the method to import data from the
     * specified x and y values List, and then handles the assertions
     * for the expeccted Entry count as well as the Entry values.
     *
     * @param xValues  X Value array to use for importing
     * @param yValues  Y Value array to use for importing
     * @param expectedValues  Map of expected values in the Data Series
     */
    private void testImportFromListsHelper(String[] xValues, String[] yValues,
                                           HashMap<Float, Float> expectedValues) {
        // Call the import from Lists method
        model.importFromLists(xValues, yValues);

        // Make sure the number of entries parsed is correct
        assertEquals(expectedValues.size(), model.getDataset().getEntryCount());

        // Start from the first Data Series entry
        int index = 0;

        for (Map.Entry<Float, Float> expectedEntry : expectedValues.entrySet()) {
            // Get the entry from the Data Series
            Entry entry = model.getDataset().getEntryForIndex(index);

            // Assert expected values
            assertEquals(expectedEntry.getKey(), entry.getX());
            assertEquals(expectedEntry.getValue(), entry.getY());

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