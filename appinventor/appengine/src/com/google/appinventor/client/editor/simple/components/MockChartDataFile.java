package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class MockChartDataFile extends MockVisibleComponent {
    public static final String TYPE = "ChartDataFile";

    private static final String PROPERTY_NAME_SOURCE = "Source";

    private MockChart chart;

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

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        if (propertyName.equals(PROPERTY_NAME_SOURCE)) {
            setSourceProperty(newValue);
        }
    }

    private void setSourceProperty(String text) {
        if (!editor.isLoadComplete()) {
            return;
        }

        long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();

        Ode.getInstance().getProjectService().load(projectId, "assets/" + text, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorReporter.reportError(caught.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                MockChartDataFile.this.onSelectedChange(true); // otherwise the last imported component
                MockCoordinateData data = new MockCoordinateData(editor);
                getContainer().addComponent(data);
                data.addToChart(chart);
            }
        });
    }

    /**
     * Adds the Mock Chart Data component to the specified Mock Chart component
     * @param chart  Chart Mock component to add the data to
     */
    public void addToChart(MockChart chart) {
        // Hide the component visually
        setVisible(false);
        setWidth("0");
        setHeight("0");

        this.chart = chart;
    }
}
