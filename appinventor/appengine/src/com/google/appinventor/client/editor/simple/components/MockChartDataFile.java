package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class MockChartDataFile extends MockVisibleComponent {
    public static final String TYPE = "ChartDataFile";

    /**
     * Creates a new instance of a Mock Data File component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockChartDataFile(SimpleEditor editor) {
        super(editor, TYPE, images.file());

        SimplePanel panel = new SimplePanel();
        panel.setWidth("16px");
        panel.setHeight("16px");
        panel.setStylePrimaryName("ode-SimpleMockComponent");
        Image icon = new Image(images.file());
        panel.add(icon);

        initComponent(panel);
    }

    @Override
    protected boolean isPropertyVisible(String propertyName) {
        // Hide HEIGHT and WIDTH properties (not needed for Chart Data File)
        if (propertyName.equals(PROPERTY_NAME_HEIGHT) ||
                propertyName.equals(PROPERTY_NAME_WIDTH)) {
            return false;
        }

        return super.isPropertyVisible(propertyName);
    }

    @Override
    protected void onSelectedChange(boolean selected) {
        super.onSelectedChange(selected);
        removeStyleDependentName("selected"); // Force remove styling
    }
}
