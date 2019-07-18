package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.util.YailList;
import java.util.Arrays;

@DesignerComponent(version = 1,
    description = "A component that holds (x, y)-coordinate based data",
    category = ComponentCategory.CHARTS,
    iconName = "images/web.png")
@SimpleObject
public final class CoordinateData extends ChartDataBase {
    /**
     * Creates a new Coordinate Data component.
     */
    public CoordinateData(Chart chartContainer) {
        super(chartContainer);
    }

    /**
     * Adds entry to the Data Series.
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction(description = "Adds (x, y) point to the Coordinate Data.")
    public void AddEntry(float x, float y) {
        // Create a 2-tuple, and add the tuple to the Data Series
        YailList pair = YailList.makeList(Arrays.asList(x, y));
        chartDataModel.addEntryFromTuple(pair);

        // Refresh Chart with new data
        refreshChart();
    }

    /**
     * Imports data from a CSV file component, with the specified column names.
     *
     * @param csvFile  CSV File component to import from
     * @param xValueColumn  x-value column name
     * @param yValueColumn  y-value column name
     */
    @SimpleFunction(description = "Imports data from the specified CSVFile component, given the names of the " +
        "X and Y value columns. Passing in empty text for any of the column parameters will result" +
        " in the usage of the default option of entry 1 having the value of 0, entry 2 having the value of" +
        " 1, and so forth.")
    public void ImportFromCSV(final CSVFile csvFile, String xValueColumn, String yValueColumn) {
        // Construct a YailList of columns from the specified parameters
        YailList columns = YailList.makeList(Arrays.asList(xValueColumn, yValueColumn));

        importFromCSVAsync(csvFile, columns);
    }
}
