package com.google.appinventor.components.runtime;

import android.app.Activity;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.google.appinventor.components.annotations.*;
import com.github.mikephil.charting.charts.Chart;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.ChartModel;

@SimpleObject
@UsesLibraries(libraries = "mpandroidchart.jar")
public abstract class ChartBase<T extends Chart, D extends DataSet> extends AndroidViewComponent implements ComponentContainer {

    protected T view;

    private String description;
    private int backgroundColor;

    /**
     * Creates a new ChartBase component.
     *
     * @param container container, component will be placed in
     */
    protected ChartBase(ComponentContainer container) {
        super(container);
    }

    /**
     * Initializes the Base Chart object instance.
     */
    protected void initChart() {
        // Adds the view to the designated container
        container.$add(this);

        // Set default values
        BackgroundColor(Component.COLOR_DEFAULT);
        Description("");
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

//    /**
//     * Getter method for the Chart view object.
//     *
//     * @return  Chart view casted to appropriate type
//     */
//    public T getChart() {
//        return view;
//    }

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
        view.getDescription().setText(description);
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

        view.setBackgroundColor(argb);
    }

    /**
     * Refreshes the Chart to react to Data Set changes.
     */
    public void Refresh() {
        // Only refresh data itself if data exists on Chart
        if (view.getData() != null) {
            view.getData().notifyDataChanged();
        }

        view.notifyDataSetChanged();
        view.invalidate();
    }

    /**
     * Creates a new Chart Model object instance.
     *
     * @return  Chart Model instance
     */
    public abstract ChartModel createChartModel();

    /**
     * Updates the Data object instance of the Chart, if there is no data on
     * the Chart object in the first place.
     *
     * @param data  Data object instance to update Chart with
     */
    public void updateData(ChartData data) {
        // If data is null and this method is called, that means that
        // the Data has been updated. However, we must also make sure
        // that there is at least 1 data set in the Data object.
        if (view.getData() == null && data.getDataSetCount() != 0) {
            view.setData(data); // Safe set
        }
    }
}
