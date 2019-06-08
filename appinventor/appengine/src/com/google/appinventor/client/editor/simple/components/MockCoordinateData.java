package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;
import org.pepstock.charba.client.data.LineDataset;

public class MockCoordinateData extends MockChartData {
    public static final String TYPE = "CoordinateData";

    /**
     * Creates a new MockCoordinateData component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockCoordinateData(SimpleEditor editor) {
        super(editor, TYPE, images.label());
    }

    /**
     * Loads a test data set for the Line Chart widget.
     */
//    private void loadTestData() {
//        // Initialize data set
//        LineDataset dataset = chartWidget.newDataset();
//
//        // Construct test data
//        dataset.setData(1, 7, 5, 4);
//
//        // Style settings
//        dataset.setFill(false);
//        dataset.setPointBackgroundColor("black");
//        dataset.setBorderColor("black");
//        dataset.setBorderWidth(1);
//        dataset.setLineTension(0);
//        dataset.setLabel("Data");
//
//        // Set the data set to the chart
//        chartWidget.getData().setDatasets(dataset);
//
//        // Set x value labels
//        chartWidget.getData().setLabels("1", "2", "3", "4");
//    }
}
