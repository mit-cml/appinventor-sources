package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;

public abstract class MockChartData extends MockVisibleComponent {
    private static final String PROPERTY_COLOR = "Color";
    private static final String PROPERTY_LABEL = "Label";
    private static final String PROPERTY_PAIRS = "ElementsFromPairs";

    // Represents the Chart data icon
    private Image iconWidget;

    protected MockChart chart;
    protected MockChartDataModel chartDataModel;

    private String currentElements = "";

    /**
     * Creates a new instance of a Mock Chart Data component.
     *
     * @param editor editor of source file the component belongs to
     * @param type  type string of the component
     * @param icon  icon of the component
     */
    MockChartData(SimpleEditor editor, String type, ImageResource icon) {
        super(editor, type, icon);

        iconWidget = new Image(icon);
        iconWidget.setHeight("100");
        iconWidget.setWidth("60");

        initComponent(iconWidget);
    }

    /**
     * Adds the Mock Chart Data component to the specified Mock Chart component
     * @param chart  Chart Mock component to add the data to
     */
    public void addToChart(MockChart chart) {
        // Hide widget (MockChartData component handled by Chart)
        iconWidget.setVisible(false);
        iconWidget.setHeight("0");
        iconWidget.setWidth("0");

        // Set references for Chart view and Chart model
        this.chart = chart;
        this.chartDataModel = chart.createDataModel();

        // Set the properties to the Data Series
        setDataSeriesProperties();

        // Refresh the Chart view
        refreshChart();
    }

    @Override
    protected boolean isPropertyVisible(String propertyName) {
        // Hide HEIGHT and WIDTH properties (not needed for Chart Data)
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
    public void onRemoved() {
        super.onRemoved();
        chartDataModel.removeDataSeriesFromChart();
        refreshChart();
    }

    /**
     * Sets the current elements of the Chart Data component.
     *
     * @param text  new elements text
     */
    private void setElementsFromPairsProperty(String text){
        currentElements = text;

        chartDataModel.setElements(currentElements);
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        // No Chart Model exists (Data not yet added to Chart), simply
        // return from the method without processing property adding.
        if (chartDataModel == null) {
            return;
        }

        if (propertyName.equals(PROPERTY_COLOR)) {
            chartDataModel.changeColor(newValue);
            refreshChart();
        } else if (propertyName.equals(PROPERTY_LABEL)) {
            chartDataModel.changeLabel(newValue);
            refreshChart();
        } else if (propertyName.equals(PROPERTY_PAIRS)) {
            setElementsFromPairsProperty(newValue);
            refreshChart();
        }
    }

    /**
     * Refreshes the Chart view.
     */
    protected void refreshChart() {
        chart.refreshChart();
    }

    /**
     * Sets the properties for the Chart Data component.
     *
     * The need for this method is the fact that the component's
     * properties can only be set after the Data component has been added to
     * the Chart.
     */
    protected void setDataSeriesProperties() {
        setElementsFromPairsProperty(getPropertyValue(PROPERTY_PAIRS));
        this.chartDataModel.changeColor(getPropertyValue(PROPERTY_COLOR));
        this.chartDataModel.changeLabel(getPropertyValue(PROPERTY_LABEL));
    }
}
