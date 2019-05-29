package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

public final class MockChartLine extends MockImageBase {
    public static final String TYPE = "ChartLine";

    /**
     * Creates a new instance of a Mock Line Chart component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockChartLine(SimpleEditor editor) {
        super(editor, TYPE, images.image());
    }
}
