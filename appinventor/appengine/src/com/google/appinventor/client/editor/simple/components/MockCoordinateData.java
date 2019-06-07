package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;

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
}
