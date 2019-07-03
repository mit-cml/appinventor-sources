package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.resources.client.ImageResource;
import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.enums.Position;
import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

abstract class MockChartBase<C extends AbstractChart> extends MockContainer {
    private static final String PROPERTY_DESCRIPTION = "Description";

    protected C chartWidget;

    static {
        ResourcesType.setClientBundle(EmbeddedResources.INSTANCE);
    }

    /**
     * Creates a new instance of a visible component.
     *
     * @param editor editor of source file the component belongs to
     * @param type  type String of the component
     * @param icon  icon of the component
     */
    protected MockChartBase(SimpleEditor editor, String type, ImageResource icon) {
        super(editor, type, icon, new MockChartLayout());
    }

    /**
     * Initializes the Chart by setting predefined style settings
     * and initializing the component itself.
     */
    protected void initChart() {
        chartWidget.getOptions().setMaintainAspectRatio(false);
        chartWidget.getOptions().getTitle().setDisplay(true);
        chartWidget.getOptions().getLegend().getLabels().setBoxWidth(20);
        chartWidget.getOptions().getLegend().setPosition(Position.BOTTOM);

        // Since the Mcok Chart component is not a container in a normal
        // sense (attached components should not be visible), the Chart Widget
        // is added to the root panel, and the root panel itself is initialized.
        // This is done to ensure that Mock Chart Data components can be dragged
        // onto the Chart itself, rather than outside the Chart component.
        rootPanel.setStylePrimaryName("ode-SimpleMockComponent");
        rootPanel.add(chartWidget);
        chartWidget.setWidth("100%"); // Fill root panel with Chart Widget's width

        initComponent(rootPanel);

        // Re-attach all children MockChartData components
        chartWidget.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent arg0) {
                if (arg0.isAttached()) {
                    for (MockComponent child : children) {
                        ((MockChartData) child).addToChart(MockChartBase.this);
                    }
                }
            }
        });
    }

    /**
     * Sets the Chart's description property to a new value.
     * @param text  new description string
     */
    private void setDescriptionProperty(String text) {
        chartWidget.getOptions().getTitle().setText(text);
    }

    /*
     * Sets the Chart's BackgroundColor property to a new value.
     * @param text  Color string value in hex
     */
    private void setBackgroundColorProperty(String text) {
        if (MockComponentsUtil.isDefaultColor(text)) {
            text = "&HFFFFFFFF";  // white
        }
        MockComponentsUtil.setWidgetBackgroundColor(chartWidget, text);
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

        if (propertyName.equals(PROPERTY_DESCRIPTION)) {
            setDescriptionProperty(newValue);
            chartWidget.draw(); // Redraws (refreshes) the Chart
        } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
            setBackgroundColorProperty(newValue);
        }
    }

    /**
     * Creates a Chart Model instance of the proper type for this Chart.
     *
     * @return  New Chart Model instance.
     */
    public abstract MockChartModel createChartModel();

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
}
