package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

import java.util.Arrays;
import java.util.List;

public class MockCoordinateData extends MockChartData {
    public static final String TYPE = "CoordinateData";

    /**
     * Creates a new MockCoordinateData component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockCoordinateData(SimpleEditor editor) {
        super(editor, TYPE, images.tinyDB());
    }

    @Override
    protected void updateCSVData() {
        // DataSource is not of instance MockCSVFile. Ignore event call
        if (!(dataSource instanceof MockCSVFile)) {
            return;
        }

        // Get the rows of the MockCSVFile (safe cast)
        List<List<String>> rows = ((MockCSVFile)(dataSource)).getRows();

        // Construct a pair of columns from the local properties
        List<String> columns = Arrays.asList(csvXColumn, csvYColumn);

        // Parse CSV from the retrieved rows and the local column properties
        chartDataModel.setElementsFromCSVRows(rows, columns);
        refreshChart();
    }
}
