package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

import java.util.Arrays;

public class MockCoordinateData extends MockChartData {
    public static final String TYPE = "CoordinateData";

    /**
     * Creates a new MockCoordinateData component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockCoordinateData(SimpleEditor editor) {
        super(editor, TYPE, images.tinyDB());

        // Initialize dataFileColumns to default values.
        // Since CoordinateData consists of 2 entries,
        // the List is initialized with 2 entries.
        dataFileColumns = Arrays.asList("", "");
    }
}
