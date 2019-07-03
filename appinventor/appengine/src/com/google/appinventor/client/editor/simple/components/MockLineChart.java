package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.dnd.DragSource;
import org.pepstock.charba.client.ScatterChart;

public final class MockLineChart extends MockChartBase<ScatterChart> {
    public static final String TYPE = "LineChart";

    /**
     * Creates a new instance of a Mock Line Chart component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockLineChart(SimpleEditor editor) {
        super(editor, TYPE, images.image());

        // Initialize Line Chart mock Widget
        // We are using ScatterChart instead of LineChart because
        // ScatterCharts in Charba allow more flexibility for our use case
        // E.g. datasets with varying amount of data points and scale.
        chartWidget = new ScatterChart();

        // Initialize the Chart
        initChart();
    }

    @Override
    public MockLineChartModel createChartModel() {
        return new MockLineChartModel(chartWidget.getData());
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
