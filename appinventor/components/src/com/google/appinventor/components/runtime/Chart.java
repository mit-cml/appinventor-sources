package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.OnInitializeListener;

import java.util.ArrayList;
import java.util.HashSet;

@SimpleObject
@DesignerComponent(version = 1,
        category = ComponentCategory.CHARTS,
        description = "A component that allows visualizing data")
@UsesLibraries(libraries = "mpandroidchart.jar")
public class Chart extends AndroidViewComponent implements ComponentContainer, OnInitializeListener {
    // Root layout of the Chart view. This is used to make Chart
    // dynamic removal & adding easier.
    private RelativeLayout view;

    // Underlying Chart view
    private ChartView chartView;

    // Properties
    private int type;
    private int backgroundColor;
    private String description;
    private int pieRadius;

    // Synced t value across all Data Series (used for real-time entries)
    private int t = 0;

    // Attached Data components
    private ArrayList<ChartDataBase> dataComponents;

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

        dataComponents = new ArrayList<ChartDataBase>();

        // Set default values
        Type(ComponentConstants.CHART_TYPE_LINE);
        Width(ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH);
        Height(ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT);
        BackgroundColor(Component.COLOR_DEFAULT);
        Description("");
        PieRadius(100);

        // Register onInitialize event of the Chart
        $form().registerForOnInitialize(this);
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
     * @return  one of {@link ComponentConstants#CHART_TYPE_LINE},
     *          {@link ComponentConstants#CHART_TYPE_SCATTER},
     *          {@link ComponentConstants#CHART_TYPE_AREA},
     *          {@link ComponentConstants#CHART_TYPE_BAR} or
     *          {@link ComponentConstants#CHART_TYPE_PIE}
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
     * @param type one of {@link ComponentConstants#CHART_TYPE_LINE},
     *  {@link ComponentConstants#CHART_TYPE_SCATTER},
     *  {@link ComponentConstants#CHART_TYPE_AREA},
     *  {@link ComponentConstants#CHART_TYPE_BAR} or
     *  {@link ComponentConstants#CHART_TYPE_PIE}
     *
     * @throws IllegalArgumentException if shape is not a legal value.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_TYPE,
            defaultValue = ComponentConstants.CHART_TYPE_LINE + "")
    @SimpleProperty(description = "Specifies the chart's type (line, scatter," +
            "area, bar, pie).",
            userVisible = false)
    public void Type(int type) {
        this.type = type;

        // Keep track whether a ChartView already exists,
        // in which case it will have to be reinitialized.
        boolean chartViewExists = (chartView != null);

        // ChartView currently exists in root layout. Remove it.
        if (chartViewExists) {
            view.removeView(chartView.getView());
        }

        // Create a new Chart view based on the supplied type
        chartView = createChartViewFromType(type);

        // Add the new Chart view as the first child of the root RelativeLayout
        view.addView(chartView.getView(), 0);

        // If a ChartView already existed, then the Chart
        // has to be reinitialized.
        if (chartViewExists) {
            reinitializeChart();
        }
    }

    /**
     * Creates and returns a ChartView object based on the type
     * (integer) provided.
     * @param type one of {@link ComponentConstants#CHART_TYPE_LINE},
     *  {@link ComponentConstants#CHART_TYPE_SCATTER},
     *  {@link ComponentConstants#CHART_TYPE_AREA},
     *  {@link ComponentConstants#CHART_TYPE_BAR} or
     *  {@link ComponentConstants#CHART_TYPE_PIE}
     * @return  new ChartView instance
     */
    private ChartView createChartViewFromType(int type) {
        switch(type) {
            case ComponentConstants.CHART_TYPE_LINE:
                return new LineChartView(container.$context());
            case ComponentConstants.CHART_TYPE_SCATTER:
                return new ScatterChartView(container.$context());
            case ComponentConstants.CHART_TYPE_AREA:
                return new AreaChartView(container.$context());
            case ComponentConstants.CHART_TYPE_BAR:
                return new BarChartView(container.$context());
            case ComponentConstants.CHART_TYPE_PIE:
                return new PieChartView(container.$context());
            default:
                // Invalid argument
                throw new IllegalArgumentException("type:" + type);
        }
    }

    /**
     * Reinitializes the Chart view by reattaching all the Data
     * components and setting back all the properties.
     */
    private void reinitializeChart() {
        // Iterate through all attached Data Components and reinitialize them.
        // This is needed since the Type property is registered only after all
        // the Data components are attached to the Chart.
        // This has no effect when the Type property is default (0), since
        // the Data components are not attached yet, making the List empty.
        for (ChartDataBase dataComponent : dataComponents) {
            dataComponent.initChartData();
        }

        Description(description);
        BackgroundColor(backgroundColor);
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
     * Sets the Pie Radius of the Chart. If the current type is
     * not the Pie Chart, the value is simply stored, but not
     * processed.
     *
     * The value is hidden in the blocks due to it being applicable
     * to a single Chart only. TODO: Might be better to change this in the future
     *
     * TODO: Make this an enum selection in the future? (Donut, Full Pie, Small Donut, etc.)
     * @param percent  Percentage of the Pie Chart radius to fill
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHART_PIE_RADIUS,
                defaultValue = "100")
    @SimpleProperty(userVisible = false)
    public void PieRadius(int percent) {
        this.pieRadius = percent;

        // Only set the value if the Chart View is a
        // Pie Chart View; Otherwise take no action.
        if (chartView instanceof PieChartView) {
            ((PieChartView)chartView).setPieRadius(percent);
        }
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

    /**
     * Returns the underlying Chart View object.
     * @return  Chart View object
     */
    public ChartView getChartView() {
        return chartView;
    }

    /**
     * Attach a Data Component to the Chart.
     * @param dataComponent  Data component object instance to add
     */
    public void addDataComponent(ChartDataBase dataComponent) {
        dataComponents.add(dataComponent);
    }

    @Override
    public void onInitialize() {
        // If the Chart View is of type PieChartView, the
        // radius of the Chart has to be set after initialization
        // due to the method relying on retrieving width and height
        // via getWidth() and getHeight(), which only return non-zero
        // values after the Screen is initialized.
        if (chartView instanceof PieChartView) {
            ((PieChartView)chartView).setPieRadius(pieRadius);
            chartView.Refresh();
        }
    }

    /**
     * Returns the t value to use for time entries for a
     * Data Series of this Chart component.
     *
     * Takes in the t value of a Data Series as an argument
     * to determine a value tailored to the Data Series, while
     * updating the synced t value of the Chart component.
     *
     * This method primarily takes care of syncing t values
     * across all the Data Series of the Chart for consistency.
     *
     * @param dataSeriesT  t value of a Data Series
     * @return  t value to use for the next time entry based on the specified parameter
     */
    public int updateAndGetTValue(int dataSeriesT) {
        int returnValue;

        // If the difference between the global t and the Data Series' t
        // value is more than one, that means the Data Series' t value
        // is out of sync and must be updated.
        if (t - dataSeriesT > 1) {
            returnValue = t;
        } else {
            returnValue = dataSeriesT;
        }

        // Calculate the new Data Series t value (by incrementing by 1,
        // since that will be the value after adding a time entry).
        // If this value is more than t, then update the global t
        // value (since a bigger one will immediately be present upon
        // adding a Time Entry to the Data Series)
        if ((returnValue + 1) > t) {
            t = returnValue + 1;
        }

        // Return the calculated t value
        return returnValue;
    }
}
