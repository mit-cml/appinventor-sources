package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.resources.client.ImageResource;
import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.Chart;
import org.pepstock.charba.client.resources.EmbeddedResources;
import org.pepstock.charba.client.resources.ResourcesType;

abstract class MockChart<C extends AbstractChart> extends MockVisibleComponent {
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
    protected MockChart(SimpleEditor editor, String type, ImageResource icon) {
        super(editor, type, icon);
    }

    /**
     * Initializes the Chart by setting predefined style settings
     * and initializing the component itself.
     */
    protected void initChart() {
        chartWidget.getOptions().setMaintainAspectRatio(false);
        chartWidget.getOptions().getTitle().setDisplay(true);
        chartWidget.setStylePrimaryName("ode-SimpleMockComponent");

        initComponent(chartWidget);
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
}
