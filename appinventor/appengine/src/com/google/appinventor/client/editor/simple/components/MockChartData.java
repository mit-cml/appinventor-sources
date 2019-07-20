package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidCsvColumnSelectorProperty;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

public abstract class MockChartData extends MockVisibleComponent implements CSVFileChangeListener {
    private static final String PROPERTY_COLOR = "Color";
    private static final String PROPERTY_LABEL = "Label";
    private static final String PROPERTY_PAIRS = "ElementsFromPairs";
    private static final String PROPERTY_CHART_SOURCE = "Source";
    private static final String PROPERTY_CSV_X_COLUMN = "CsvXColumn";
    private static final String PROPERTY_CSV_Y_COLUMN = "CsvYColumn";

    // Represents the Chart data icon
    private Image iconWidget;

    protected MockChart chart;
    protected MockChartDataModel chartDataModel;
    protected MockComponent dataSource;

    // Properties for selecting the CSVFile source columns to import from
    protected String csvXColumn = "";
    protected String csvYColumn = "";

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
                propertyName.equals(PROPERTY_NAME_WIDTH) ||
                propertyName.equals(PROPERTY_CSV_X_COLUMN) ||
                propertyName.equals(PROPERTY_CSV_Y_COLUMN)) {
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

    /**
     * Sets the Source property of the Chart Data component.
     * Responsible for showing/hiding properties according to the
     * attached Source component type.
     *
     * @param source  Name of the new Source component attached
     */
    private void setSourceProperty(String source) {
        // Get the newly attached Source component
        dataSource = editor.getComponents().getOrDefault(source, null);

        // Hide ELements from Pairs property if a Data Source has been set
        hideProperty(PROPERTY_PAIRS, (dataSource == null));

        // Handle CSV-related property responses
        handleCSVPropertySetting();

        // If the Data Source is now null, set back the
        // currentElements property
        if (dataSource == null) {
            onPropertyChange(PROPERTY_PAIRS, currentElements);
        }

        // If the component is currently selected, re-select it to refresh
        // the Properties panel.
        if (isSelected()) {
            onSelectedChange(true);
        }
    }

    /**
     * Sets the CSV X Column property of the Chart Data component.
     * After setting the property, the CSV data is then re-imported
     * (if possible)
     * @param column  new column name
     */
    private void setCSVXColumnProperty(String column) {
        this.csvXColumn = column;
        updateCSVData();
    }

    /**
     * Sets the CSV Y Column property of the Chart Data component.
     * After setting the property, the CSV data is then re-imported
     * (if possible)
     * @param column  new column name
     */
    private void setCSVYColumnProperty(String column) {
        this.csvYColumn = column;
        updateCSVData();
    }

    /**
     * Re-imports data into the Chart from the attached CSVFile source
     * based on the current CSV column properties.
     */
    protected abstract void updateCSVData();

    private void handleCSVPropertySetting() {
        // Show the CSVColumns property only if the attached Source component is of type CSVFile
        boolean showCSVColumns = (dataSource instanceof MockCSVFile);

        // Hide or show the CsvColumns property depending on condition
        hideProperty(PROPERTY_CSV_X_COLUMN, showCSVColumns);
        hideProperty(PROPERTY_CSV_Y_COLUMN, showCSVColumns);

        if (showCSVColumns) {
            YoungAndroidCsvColumnSelectorProperty xEditor =
                (YoungAndroidCsvColumnSelectorProperty)
                    properties.getProperty(PROPERTY_CSV_X_COLUMN).getEditor();

            YoungAndroidCsvColumnSelectorProperty yEditor =
                (YoungAndroidCsvColumnSelectorProperty)
                    properties.getProperty(PROPERTY_CSV_Y_COLUMN).getEditor();

            xEditor.changeSource((MockCSVFile)dataSource);
            yEditor.changeSource((MockCSVFile)dataSource);

            ((MockCSVFile)dataSource).addColumnChageListener(this);
            onColumnsChange((MockCSVFile)dataSource);
        }
    }

    /**
     * Hides or shows the specified property of the Component.
     *
     * @param property  Property key
     * @param hide  will hide the property if set to true, will show it otherwise
     */
    private void hideProperty(String property, boolean hide) {
        // Get the current type flags of the Property
        int type = properties.getProperty(property).getType();

        if (hide) {
            type &= ~EditableProperty.TYPE_INVISIBLE; // AND with all bits except INVISIBLE flag
        } else {
            type |= EditableProperty.TYPE_INVISIBLE; // OR with INVISIBLE flag to add invisibility
        }

        // Set the new type
        properties.getProperty(property).setType(type);
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
        } else if (propertyName.equals(PROPERTY_CHART_SOURCE)) {
            setSourceProperty(newValue);
        } else if (propertyName.equals(PROPERTY_CSV_X_COLUMN)) {
            setCSVXColumnProperty(newValue);
        } else if (propertyName.equals(PROPERTY_CSV_Y_COLUMN)) {
            setCSVYColumnProperty(newValue);
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
        // Re-set all Chart Data properties to provide effect on
        // attached Chart component.
        for (EditableProperty property : properties) {
            onPropertyChange(property.getName(), property.getValue());
        }
    }

    @Override
    public void onColumnsChange(MockCSVFile csvFile) {
        if (!dataSource.equals(csvFile)) {
            return;
        }

        updateCSVData();
    }
}
