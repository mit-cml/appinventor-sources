package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;

public class MockLineChartData extends MockVisibleComponent {
    /**
     * Component type name.
     */
    public static final String TYPE = "LineChartData";

    // GWT label widget used to mock a Simple Label
    private InlineHTML labelWidget;

    private String savedText;     // Saved text, so if we change from
    // text to/from html we have the text
    // to set

    /**
     * Creates a new MockLabel component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockLineChartData(SimpleEditor editor) {
        super(editor, TYPE, images.label());

        // Initialize mock label UI
        labelWidget = new InlineHTML();
        labelWidget.setStylePrimaryName("ode-SimpleMockComponent");
        initComponent(labelWidget);
    }
}
