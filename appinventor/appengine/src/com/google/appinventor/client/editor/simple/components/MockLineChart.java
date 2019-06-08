package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.dnd.DragSource;
import org.pepstock.charba.client.LineChart;

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

        // Initialize the Chart
        initChart();
    }

    @Override
    public MockChartModel createChartModel() {
        return new MockLineChartModel();
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);
    }

    @Override
    protected boolean acceptableSource(DragSource source) {
        return getComponentFromDragSource(source) instanceof MockCoordinateData;
    }
}
