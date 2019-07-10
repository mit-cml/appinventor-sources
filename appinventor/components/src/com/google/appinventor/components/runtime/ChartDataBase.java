package com.google.appinventor.components.runtime;

import android.util.Log;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.YailList;

@SimpleObject
public abstract class ChartDataBase implements Component {
    protected Chart container;
    protected ChartDataModel chartDataModel;

    private String label;
    private int color;

    private YailList csvColumns;
    private ChartDataFile dataFile;

    /**
     * Creates a new Chart Data component.
     */
    protected ChartDataBase(Chart chartContainer) {
        this.container = chartContainer;
        chartContainer.addDataComponent(this);
        initChartData();
    }

    protected ChartDataBase(ChartDataFile chartDataFile) {
        this((Chart)chartDataFile.container);
        this.dataFile = chartDataFile;
    }

    /**
     * Returns the data series color as an alpha-red-green-blue integer.
     *
     * @return  background RGB color with alpha
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public int Color() {
        return color;
    }

    /**
     * Specifies the data series color as an alpha-red-green-blue integer.
     *
     * @param argb  background RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
    @SimpleProperty
    public void Color(int argb) {
        color = argb;
        chartDataModel.setColor(color);
        refreshChart();
    }

    /**
     * Returns the label text of the data series.
     *
     * @return  label text
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public String Label() {
        return label;
    }

    /**
     * Specifies the text for the data series label.
     *
     * @param text  label text
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
            defaultValue = "")
    @SimpleProperty
    public void Label(String text) {
        this.label = text;
        chartDataModel.setLabel(text);
        refreshChart();
    }

    /**
     * Specifies the elements of the entries that the Chart should have.
     * @param elements  Comma-separated values of Chart entries alternating between x and y values.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description="To be done (non-functional for now)",  category = PropertyCategory.BEHAVIOR)
    public void ElementsFromPairs(String elements) {
        // Base case:  nothing to add
        if (elements.equals("")) {
            return;
        }

        chartDataModel.setElements(elements);
        refreshChart();
    }

    /**
     * Initializes the Chart Data object by setting
     * the default properties and initializing the
     * corresponding ChartDataModel object instance.
     */
    public void initChartData() {
        // Creates a ChartDataModel based on the current
        // Chart type being used.
        chartDataModel = container.createChartModel();

        // Set default values
        Color(Component.COLOR_BLACK);
        Label("");
    }

    /**
     * Adds elements to the Data component from a specified List of tuples.
     *
     * @param list  YailList of tuples.
     */
    @SimpleFunction(description = "Imports data from a list of entries" +
      "Data is not overwritten.")
    public void ImportFromList(YailList list) {
        chartDataModel.importFromList(list);
        refreshChart();
    }

    /**
     * Removes all the entries from the Data Series.
     */
    @SimpleFunction(description = "Clears all of the data.")
    public void Clear() {
        chartDataModel.clearEntries();
        refreshChart();
    }


    /**
     * Imports data from a CSV file component, with the specified column names.
     *
     * Experimental for now.
     *
     * @param csvFile  CSV File component to import form
     * @param xValueColumn  x-value column name
     * @param yValueColumn  y-value column name
     */
    @SimpleFunction(description = "Work in progress")
    public void ImportFromCSV(CSVFile csvFile, String xValueColumn, String yValueColumn) {
        // Get x and y value YailLists
        YailList xValues = csvFile.getColumn(xValueColumn);
        YailList yValues = csvFile.getColumn(yValueColumn);

        if(xValues == null || yValues == null) {
            return;
        }

        // Import the CSV pair to the Data Series
        //ImportFromLists(xValues, yValues);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description="To be done (non-functional for now)",  category = PropertyCategory.BEHAVIOR,
                userVisible = false)
    public void CsvColumns(String columns) {
        try {
            this.csvColumns = CsvUtil.fromCsvRow(columns);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        chartDataModel.importFromCSV(dataFile, csvColumns);
        refreshChart();
    }

    /**
     * Refreshes the Chart view object.
     */
    protected void refreshChart() {
        container.refresh();
    }

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return null;
    }
}
