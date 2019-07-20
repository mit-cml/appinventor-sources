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
        if (!(dataSource instanceof MockCSVFile)) {
            return;
        }

        List<List<String>> rows = ((MockCSVFile)(dataSource)).getRows();

        List<String> columns = Arrays.asList(csvXColumn, csvYColumn);
        chartDataModel.setElementsFromCSV(rows, columns);

        refreshChart();
    }
}
