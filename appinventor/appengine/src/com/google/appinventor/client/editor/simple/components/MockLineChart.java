package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

public final class MockLineChart extends MockImageBase {
    public static final String TYPE = "LineChart";

    /**
     * Creates a new instance of a Mock Line Chart component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockLineChart(SimpleEditor editor) {
        super(editor, TYPE, images.image());
    }
}
