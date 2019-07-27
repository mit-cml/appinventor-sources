package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.util.YailList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

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
    public void AddEntry(final float x, final float y) {
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
    public void RemoveEntry(final float x, final float y) {
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
    public boolean DoesEntryExist(final float x, final float y) {
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

        return false;
    }


    @Override
    protected void importFromLocalCSVSource(final CSVFile dataSource) {
        ImportFromCSV(dataSource, csvXColumn, csvYColumn);
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
