package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.ScatterChart;

public abstract class MockLineChartViewBase extends MockChartView<ScatterChart> {
    /**
     * Creates a new MockLineChartViewBase object instance.
     */
    protected MockLineChartViewBase() {
        // A ScatterChart widget is used to allow
        // arbitrary insertions of (x, y) points and
        // automatic generation of the X axis labels
        // (as well as automatic scaling)
        chartWidget = new ScatterChart();
        initializeDefaultSettings();
    }
}
