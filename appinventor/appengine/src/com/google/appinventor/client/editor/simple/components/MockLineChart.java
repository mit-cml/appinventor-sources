package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;
import org.pepstock.charba.client.LineChart;
import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.data.Labels;
import org.pepstock.charba.client.data.LineDataset;
import org.pepstock.charba.client.enums.CubicInterpolationMode;
import org.pepstock.charba.client.enums.SteppedLine;
import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

import java.util.ArrayList;

public final class MockLineChart extends MockChart<LineChart> {
    public static final String TYPE = "LineChart";

    /**
     * Creates a new instance of a Mock Line Chart component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockLineChart(SimpleEditor editor) {
        super(editor, TYPE, images.image());

        // Initialize Line Chart mock Widget
        chartWidget = new LineChart();

        // Load test data set
        loadTestData();

        // Initialize the Chart
        initChart();
    }

    /**
     * Loads a test data set for the Line Chart widget.
     */
    private void loadTestData() {
        // Initialize data set
        LineDataset dataset = chartWidget.newDataset();

        // Construct test data
        dataset.setData(1, 7, 5, 4);

        // Style settings
        dataset.setFill(false);
        dataset.setPointBackgroundColor("black");
        dataset.setBorderColor("black");
        dataset.setBorderWidth(1);
        dataset.setLineTension(0);
        dataset.setLabel("Data");

        // Set the data set to the chart
        chartWidget.getData().setDatasets(dataset);

        // Set x value labels
        chartWidget.getData().setLabels("1", "2", "3", "4");
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);
    }
}
