package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

import java.util.Arrays;
import java.util.List;

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

    @Override
    protected void setDefaultData() {
        chart.chartWidget.getData().setLabels("1", "2", "3", "4");
        int add = chart.chartWidget.getData().getDatasets().size();

        for (int i = 1; i <= 4; ++i) {
            chartModel.addEntry(i, i + add);
        }
    }
}
