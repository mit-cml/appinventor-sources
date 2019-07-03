package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.resources.client.ImageResource;
import org.pepstock.charba.client.*;
import org.pepstock.charba.client.enums.Position;
import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

public final class MockChart extends MockContainer {
    public static final String TYPE = "Chart";

    private static final String PROPERTY_TYPE = "Type";
    private static final String PROPERTY_NAME_DESCRIPTION = "Description";

    static {
        ResourcesType.setClientBundle(EmbeddedResources.INSTANCE);
    }

    protected MockChartViewBase chartView;

    // Legal values for type are defined in
    // com.google.appinventor.components.runtime.Component.java.
    private int type;

    /**
     * Creates a new instance of a visible component.
     *
     * @param editor editor of source file the component belongs to
     */
    public MockChart(SimpleEditor editor) {
        super(editor, TYPE, images.image(), new MockChartLayout());

        // Since the Mcok Chart component is not a container in a normal
        // sense (attached components should not be visible), the Chart Widget
        // is added to the root panel, and the root panel itself is initialized.
        // This is done to ensure that Mock Chart Data components can be dragged
        // onto the Chart itself, rather than outside the Chart component.
        rootPanel.setStylePrimaryName("ode-SimpleMockComponent");

        initComponent(rootPanel);

        // Re-attach all children MockChartData components
        rootPanel.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent arg0) {
                if (arg0.isAttached()) {
                    for (MockComponent child : children) {
                        //((MockChartData) child).addToChart(MockChart.this);
                    }
                }
            }
        });

    }

    /*
     * Sets the button's Shape property to a new value.
     */
    private void setTypeProperty(String value) {
        // Update type
        type = Integer.parseInt(value);

        // Remove the current Chart Widget from the root panel (if present)
        if (chartView != null) {
            rootPanel.remove(chartView.getChartWidget());
        }

        switch(type) {
            case 0:
                // Line Chart
                //chartWidget = new ScatterChart();
                chartView = new MockLineChartView();
                break;
            case 1:
                // Scatter Chart
                //chartWidget = new ScatterChart();
                chartView = new MockLineChartView();
                break;
            case 2:
                // Area Chart
                //chartWidget = new ScatterChart();
                chartView = new MockLineChartView();
                break;
            case 3:
                // Bar Chart
                //chartWidget = new BarChart();
                chartView = new MockLineChartView();
                break;
            case 4:
                // Pie Chart
                //chartWidget = new PieChart();
                chartView = new MockLineChartView();
                break;
            default:
                // Invalid argument
                throw new IllegalArgumentException("type:" + type);
        }

        // Add the Chart Widget to the Root Panel
        rootPanel.add(chartView.getChartWidget());
    }

    @Override
    public int getPreferredWidth() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
    }

    @Override
    public int getPreferredHeight() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        if (propertyName.equals(PROPERTY_TYPE)) {
            setTypeProperty(newValue);
        } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
            chartView.setBackgroundColor(newValue);
        } else if (propertyName.equals(PROPERTY_NAME_DESCRIPTION)) {
            chartView.setTitle(newValue);
        }
    }

    /**
     * Returns the Mock Component of the Drag Source.
     *
     * @param source  DragSource instance
     * @return  MockComponent instance
     */
    protected MockComponent getComponentFromDragSource(DragSource source) {
        MockComponent component = null;
        if (source instanceof MockComponent) {
            component = (MockComponent) source;
        } else if (source instanceof SimplePaletteItem) {
            component = (MockComponent) source.getDragWidget();
        }

        return component;
    }

    @Override
    protected boolean acceptableSource(DragSource source) {
        return getComponentFromDragSource(source) instanceof MockChartData;
    }
}
