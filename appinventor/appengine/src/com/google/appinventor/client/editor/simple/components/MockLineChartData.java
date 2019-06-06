package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;

public class MockLineChartData extends MockVisibleComponent {
    public static final String TYPE = "LineChartData";

    // Temporary placeholder for the Chart Data image
    private InlineHTML labelWidget;

    /**
     * Creates a new MockLineChartData component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockLineChartData(SimpleEditor editor) {
        super(editor, TYPE, images.label());

        labelWidget = new InlineHTML();
        labelWidget.setStylePrimaryName("ode-SimpleMockComponent");
        labelWidget.setText("LINE CHART DATA");
        initComponent(labelWidget);
    }
}
