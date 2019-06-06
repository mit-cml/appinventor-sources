package com.google.appinventor.components.runtime;

import android.app.Activity;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.google.appinventor.components.annotations.*;
import com.github.mikephil.charting.charts.Chart;
import com.google.appinventor.components.common.PropertyTypeConstants;

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
        view.getData().notifyDataChanged();
        view.notifyDataSetChanged();
        view.invalidate();
    }


    /**
     * Adds new data set to the Chart data.
     *
     * @param dataSet - data set to add
     */
    public abstract void AddDataSet(D dataSet);
}
