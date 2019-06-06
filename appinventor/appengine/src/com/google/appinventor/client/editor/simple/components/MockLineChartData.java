package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;

public class MockLineChartData extends MockChartData {
    public static final String TYPE = "LineChartData";

    /**
     * Creates a new MockLineChartData component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockLineChartData(SimpleEditor editor) {
        super(editor, TYPE, images.label());
    }
}
