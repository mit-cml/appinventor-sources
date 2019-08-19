package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidDataColumnSelectorProperty;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

import java.util.List;

public abstract class MockChartData extends MockVisibleComponent implements DataFileChangeListener {
    private static final String PROPERTY_COLOR = "Color";
    private static final String PROPERTY_LABEL = "Label";
    private static final String PROPERTY_POINT_SHAPE = "PointShape";
    private static final String PROPERTY_PAIRS = "ElementsFromPairs";
    private static final String PROPERTY_CHART_SOURCE = "Source";
    private static final String PROPERTY_CHART_SOURCE_VALUE = "DataSourceValue";
    private static final String PROPERTY_DATA_FILE_X_COLUMN = "DataFileXColumn";
    private static final String PROPERTY_DATA_FILE_Y_COLUMN = "DataFileYColumn";
    private static final String PROPERTY_WEB_X_COLUMN = "WebXColumn";
    private static final String PROPERTY_WEB_Y_COLUMN = "WebYColumn";

    // Represents the Chart data icon
    private Image iconWidget;

    protected MockChart chart;
    protected MockChartDataModel chartDataModel;
    protected MockComponent dataSource;

    // Stores the DataFileColumn properties (in order) to import from
    protected List<String> dataFileColumns;

    private String currentElements = "";
    private int pointShape = 0;

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

        // Change the visibilities of the styling properties
        // according to Chart type
        changeStylingPropertiesVisibility();

        // Refresh the Chart view
        refreshChart();
    }

    @Override
    protected boolean isPropertyVisible(String propertyName) {
        // Hide HEIGHT and WIDTH properties (not needed for Chart Data)
        // Chart Source related properties should be hidden by default,
        // as they are only shown upon certain conditions (e.g. DataFileColumn
        // properties are only shown when the Source component is a DataFile)
        if (propertyName.equals(PROPERTY_NAME_HEIGHT) ||
                propertyName.equals(PROPERTY_NAME_WIDTH) ||
                propertyName.equals(PROPERTY_DATA_FILE_X_COLUMN) ||
                propertyName.equals(PROPERTY_DATA_FILE_Y_COLUMN) ||
                propertyName.equals(PROPERTY_CHART_SOURCE_VALUE) ||
                propertyName.equals(PROPERTY_WEB_X_COLUMN) ||
                propertyName.equals(PROPERTY_WEB_Y_COLUMN) ||
                propertyName.equals(PROPERTY_POINT_SHAPE)) {
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
        setSourceProperty(""); // Unset the Source property to remove listener references
        chartDataModel.removeDataSeriesFromChart(); // Remove the Data Series from the Chart
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
        // If a Data Source was previously assigned and is of type DataFile,
        // de-attach this Data component from it
        if (dataSource instanceof MockDataFile) {
            ((MockDataFile)dataSource).removeDataFileChangeListener(this);
        }

        // Get the newly attached Source component
        dataSource = editor.getComponents().getOrDefault(source, null);

        changeSourcePropertiesVisibility();

        // If the Data Source is now null, set back the
        // currentElements property.
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
     * Sets the Data File X Column property of the Chart Data component.
     * After setting the property, the data is then re-imported
     * (if possible)
     * @param column  new column name
     */
    private void setDataFileXColumnProperty(String column) {
        // The X column is the first element of the dataFileColumns list
        dataFileColumns.set(0, column);
        updateDataFileData();
    }

    private void setPointShapeProperty(String newValue) {
        this.pointShape = Integer.parseInt(newValue);

        if (chartDataModel instanceof MockScatterChartDataModel) {
            ((MockScatterChartDataModel)chartDataModel).changePointShape(pointShape);
        }
    }

    /**
     * Sets the DataFile Y Column property of the Chart Data component.
     * After setting the property, the data is then re-imported
     * (if possible)
     * @param column  new column name
     */
    private void setDataFileYColumnProperty(String column) {
        // The Y column is the second element of the dataFileColumns list
        dataFileColumns.set(1, column);
        updateDataFileData();
    }

    /**
     * Imports data into the Chart from the attached DataFile source
     * based on the current DataFile column properties.
     */
    protected void updateDataFileData() {
        // DataSource is not of instance MockDataFile. Ignore event call
        if (!(dataSource instanceof MockDataFile)) {
            return;
        }

        // Get the columns from the MockDataFile using the local DataFile Column properties (safe cast)
        List<List<String>> columns = ((MockDataFile)(dataSource)).getColumns(dataFileColumns);

        // Import the data to the Data Series
        chartDataModel.setElementsFromColumns(columns);
        refreshChart();
    }

    /**
     * Changes the visibilities of Chart Data Source related properties according
     * to the current attached Data Source.
     */
    private void changeSourcePropertiesVisibility() {
        // Hide Elements from Pairs property if a Data Source has been set
        showProperty(PROPERTY_PAIRS, (dataSource == null));

        // Hide or show the Web column properties depending on condition
        boolean showWebColumns = (dataSource != null && dataSource.getType().equals("Web"));
        showProperty(PROPERTY_WEB_X_COLUMN, showWebColumns);
        showProperty(PROPERTY_WEB_Y_COLUMN, showWebColumns);

        // Handle DataFile-related property responses
        handleDataFilePropertySetting();

        // Show Data Source Value only if the Data Source is non-null and not of type MockDataFile or Web
        boolean showDataSourceValue = (dataSource != null &&
            !(dataSource instanceof MockDataFile || showWebColumns));

        showProperty(PROPERTY_CHART_SOURCE_VALUE, showDataSourceValue);
    }

    private void changeStylingPropertiesVisibility() {
        boolean showScatterChartProperties =
            chartDataModel instanceof MockScatterChartDataModel;

        showProperty(PROPERTY_POINT_SHAPE, showScatterChartProperties);
    }

    /**
     * Handles properties with regards to a DataFile source upon
     * changing the Data Source of the Data component.
     *
     * The method shows/hides the DataFile X and Y Column properties
     * depending on the attached Source (if it's a DataFile, then
     * the properties will be shown, and hidden otherwise)
     * If the properties are shown, the Column selectors
     * are updated to track the new data source, and the
     * data in the Data Series is updated.
     */
    private void handleDataFilePropertySetting() {
        // Show the DataFileColumns property only if the attached Source component is of type DataFile
        boolean showDataFileColumns = (dataSource instanceof MockDataFile);

        // Hide or show the DataFileColumns property depending on condition
        showProperty(PROPERTY_DATA_FILE_X_COLUMN, showDataFileColumns);
        showProperty(PROPERTY_DATA_FILE_Y_COLUMN, showDataFileColumns);

        // Get the Column property selectors
        YoungAndroidDataColumnSelectorProperty xEditor =
            (YoungAndroidDataColumnSelectorProperty)
                properties.getProperty(PROPERTY_DATA_FILE_X_COLUMN).getEditor();

        YoungAndroidDataColumnSelectorProperty yEditor =
            (YoungAndroidDataColumnSelectorProperty)
                properties.getProperty(PROPERTY_DATA_FILE_Y_COLUMN).getEditor();

        if (showDataFileColumns) {
            // Add the current Data component as a DataFileChangeListener to the DataFile
            ((MockDataFile)dataSource).addDataFileChangeListener(this);

            // Update the data of the Data component to represent the DataFile
            onColumnsChange((MockDataFile)dataSource);

            // Update the Source of the column selectors
            xEditor.changeSource((MockDataFile)dataSource);
            yEditor.changeSource((MockDataFile)dataSource);
        } else {
            // Remove data sources from the property editors (since Data Source
            // is not a DataFile)
            xEditor.changeSource(null);
            yEditor.changeSource(null);
        }
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
        } else if (propertyName.equals(PROPERTY_POINT_SHAPE)) {
          setPointShapeProperty(newValue);
          refreshChart();
        } else if (propertyName.equals(PROPERTY_CHART_SOURCE)) {
            setSourceProperty(newValue);
        } else if (propertyName.equals(PROPERTY_DATA_FILE_X_COLUMN)) {
            setDataFileXColumnProperty(newValue);
        } else if (propertyName.equals(PROPERTY_DATA_FILE_Y_COLUMN)) {
            setDataFileYColumnProperty(newValue);
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
    public void onColumnsChange(MockDataFile dataFile) {
        if (!dataSource.equals(dataFile)) {
            return;
        }

        updateDataFileData();
    }
}
