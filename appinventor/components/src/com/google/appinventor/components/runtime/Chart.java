package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;

@SimpleObject
@DesignerComponent(version = 1,
        category = ComponentCategory.CHARTS,
        description = "A component that allows visualizing data")
@UsesLibraries(libraries = "mpandroidchart.jar")
public class Chart extends AndroidViewComponent implements ComponentContainer {
    // Backing for Chart type
    private int type;

    /**
     * Creates a new Chart component.
     *
     * @param container container, component will be placed in
     */
    public Chart(ComponentContainer container) {
        super(container);
    }

    @Override
    public View getView() {
        return null;
    }

    @Override
    public Activity $context() {
        return container.$context();
    }

    @Override
    public Form $form() {
        return container.$form();
    }

    @Override
    public void $add(AndroidViewComponent component) {
        throw new UnsupportedOperationException("ChartBase.$add() called");
    }

    @Override
    public void setChildWidth(AndroidViewComponent component, int width) {
        throw new UnsupportedOperationException("ChartBase.setChildWidth called");
    }

    @Override
    public void setChildHeight(AndroidViewComponent component, int height) {
        throw new UnsupportedOperationException("ChartBase.setChildHeight called");
    }

    /**
     * Returns the type of the Chart.
     *
     * @return  one of {@link Component#CHART_TYPE_LINE},
     *          {@link Component#CHART_TYPE_SCATTER},
     *          {@link Component#CHART_TYPE_AREA},
     *          {@link Component#CHART_TYPE_BAR} or
     *          {@link Component#CHART_TYPE_PIE}
     */
    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR,
            userVisible = false)
    public int Type() {
        return type;
    }

    /**
     * Specifies the type of the Chart. This does not check that the argument is a legal value.
     *
     * @param type one of {@link Component#CHART_TYPE_LINE},
     *  {@link Component#CHART_TYPE_SCATTER},
     *  {@link Component#CHART_TYPE_AREA},
     *  {@link Component#CHART_TYPE_BAR} or
     *  {@link Component#CHART_TYPE_PIE}
     *
     * @throws IllegalArgumentException if shape is not a legal value.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_TYPE,
            defaultValue = Component.CHART_TYPE_LINE + "")
    @SimpleProperty(description = "Specifies the chart's type (line, scatter," +
            "area, bar, pie).",
            userVisible = false)
    public void Type(int type) {
        this.type = type;
        // TODO: Update type
    }
}
