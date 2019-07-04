package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;

@SimpleObject
@DesignerComponent(version = 1,
        category = ComponentCategory.CHARTS,
        description = "A component that allows visualizing data")
@UsesLibraries(libraries = "mpandroidchart.jar")
public class Chart extends AndroidViewComponent implements ComponentContainer {
    private RelativeLayout view;
    private ChartViewBase chartView;

    private int type;
    private int backgroundColor;
    private String description;

    /**
     * Creates a new Chart component.
     *
     * @param container container, component will be placed in
     */
    public Chart(ComponentContainer container) {
        super(container);

        view = new RelativeLayout(container.$context());

        // Adds the view to the designated container
        container.$add(this);

        // Set default values
        Type(Component.CHART_TYPE_LINE);
        Width(ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH);
        Height(ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT);
        BackgroundColor(Component.COLOR_DEFAULT);
        Description("");
    }

    @Override
    public View getView() {
        return view;
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

        // ChartView currently exists in root layout. Remove it.
        if (chartView != null) {
            view.removeView(chartView.getView());
        }

        switch(type) {
            case 0:
                // Line Chart
                chartView = new LineChartView(container.$context());
                break;
            case 1:
                // Scatter Chart
                chartView = new LineChartView(container.$context());
                break;
            case 2:
                // Area Chart
                chartView = new LineChartView(container.$context());
                break;
            case 3:
                // Bar Chart
                chartView = new LineChartView(container.$context());
                break;
            case 4:
                // Pie Chart
                chartView = new LineChartView(container.$context());
                break;
            default:
                // Invalid argument
                throw new IllegalArgumentException("type:" + type);
        }

        view.addView(chartView.getView(), 0);
    }

    /**
     * Returns the description label text of the Chart.
     *
     * @return  description label
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public String Description() {
        return description;
    }

    /**
     * Specifies the text displayed by the description label.
     *
     * @param text  description
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
            defaultValue = "")
    @SimpleProperty
    public void Description(String text) {
        this.description = text;
        chartView.setDescription(description);
    }

    /**
     * Returns the chart's background color as an alpha-red-green-blue
     * integer.
     *
     * @return  background RGB color with alpha
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public int BackgroundColor() {
        return backgroundColor;
    }

    /**
     * Specifies the chart's background color as an alpha-red-green-blue
     * integer.
     *
     * @param argb  background RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
    @SimpleProperty
    public void BackgroundColor(int argb) {
        backgroundColor = argb;
        chartView.setBackgroundColor(argb);
    }

    /**
     * Creates a new instance of a ChartDataModel, corresponding
     * to the current Chart type.
     * @return  new ChartDataModel object instance
     */
    public ChartDataModel createChartModel() {
        return chartView.createChartModel();
    }

    /**
     * Refreshes the Chart View.
     */
    public void refresh() {
        chartView.Refresh();
    }
}
