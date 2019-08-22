package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@DesignerComponent(version = YaVersion.CHART_DATA_2D_COMPONENT_VERSION,
    description = "A component that holds (x, y)-coordinate based data",
    category = ComponentCategory.CHARTS,
    iconName = "images/web.png")
@SimpleObject
public final class ChartData2D extends ChartDataBase {
    /**
     * Creates a new Coordinate Data component.
     */
    public ChartData2D(Chart chartContainer) {
        super(chartContainer);
        dataFileColumns = Arrays.asList("", ""); // Construct default dataFileColumns list with 2 entries
        webColumns = Arrays.asList("", ""); // Construct default webColumns list with 2 entries
    }

    /**
     * Adds entry to the Data Series.
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction(description = "Adds (x, y) point to the Coordinate Data.")
    public void AddEntry(final String x, final String y) {
        // Entry should be added via the Thread Runner asynchronously
        // to guarantee the order of data adding (e.g. CSV data
        // adding could be happening when this method is called,
        // so the task should be queued in the single Thread Runner)
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                // Create a 2-tuple, and add the tuple to the Data Series
                YailList pair = YailList.makeList(Arrays.asList(x, y));
                chartDataModel.addEntryFromTuple(pair);

                // Refresh Chart with new data
                refreshChart();
            }
        });
    }

    /**
     * Removes an entry from the Data Series.
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction(description = "Removes the first matching (x, y) point from the " +
        "Coordinate Data, if it exists.")
    public void RemoveEntry(final String x, final String y) {
        // Entry should be deleted via the Thread Runner asynchronously
        // to guarantee the order of data adding (e.g. CSV data
        // adding could be happening when this method is called,
        // so the task should be queued in the single Thread Runner)
        threadRunner.execute(new Runnable() {
            @Override
            public void run() {
                // Create a 2-tuple, and remove the tuple from the Data Series
                YailList pair = YailList.makeList(Arrays.asList(x, y));
                chartDataModel.removeEntryFromTuple(pair);

                // Refresh Chart with new data
                refreshChart();
            }
        });
    }

    /**
     * Checks whether an Entry exists in the Data Series.
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction(description = "Checks whether an (x, y) entry exists in the Coordinate Data." +
        "Returns true if the Entry exists, and false otherwise.")
    public boolean DoesEntryExist(final String x, final String y) {
        try {
            return threadRunner.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    // Create a 2-tuple, and check whether the entry exists
                    YailList pair = YailList.makeList(Arrays.asList(x, y));
                    return chartDataModel.doesEntryExist(pair);
                }
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Exceptions thrown (behavior undefined): Assume entry not found
        return false;
    }

    /**
     * Imports data from a Data file component, with the specified column names.
     *
     * @param dataFile  Data File component to import from
     * @param xValueColumn  x-value column name
     * @param yValueColumn  y-value column name
     */
    @SimpleFunction(description = "Imports data from the specified DataFile component, given the names of the " +
        "X and Y value columns. Passing in empty text for any of the column parameters will result" +
        " in the usage of the default option of entry 1 having the value of 0, entry 2 having the value of" +
        " 1, and so forth.")
    public void ImportFromDataFile(final DataFile dataFile, String xValueColumn, String yValueColumn) {
        // Construct a YailList of columns from the specified parameters
        YailList columns = YailList.makeList(Arrays.asList(xValueColumn, yValueColumn));

        importFromDataFileAsync(dataFile, columns);
    }

    /**
     * Imports data from a Web component, with the specified column names.
     *
     * @param web  Web component to import from
     * @param xValueColumn  x-value column name
     * @param yValueColumn  y-value column name
     */
    @SimpleFunction(description = "Imports data from the specified Web component, given the names of the " +
        "X and Y value columns. Empty columns are filled with default values (1, 2, 3, ... for Entry 1, 2, ...). " +
        "In order for the data importing to be successful, first the Get block must be called on the Web component " +
        "to load the data, after which this block can be executed. The usage of the gotValue event " +
        "in the Web component is unnecessary.")
    public void ImportFromWeb(final Web web, String xValueColumn, String yValueColumn) {
        // Construct a YailList of columns from the specified parameters
        YailList columns = YailList.makeList(Arrays.asList(xValueColumn, yValueColumn));

        importFromWebAsync(web, columns);
    }
}
