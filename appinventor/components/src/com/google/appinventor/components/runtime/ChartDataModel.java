package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.List;

public abstract class ChartDataModel<T extends DataSet, D extends ChartData> {
    protected D data;
    protected T dataset;

    /**
     * Initializes a new ChartDataModel object instance.
     *
     * @param data  Chart data instance
     */
    protected ChartDataModel(D data) {
        this.data = data;
    }

    /**
     * Returns the size of the tuples that this Data Series
     * accepts.
     *
     * @return  tuple size (integer)
     */
    protected abstract int getTupleSize();

    /**
     * Returns the Data Series of the Data Model.
     * The method is made synchronized to avoid concurrent
     * access between threads, which can cause exceptions
     * if multiple threads try to modify the Dataset object
     * at the same time.
     * @return  Data Series object of the Data model
     */
    public synchronized T getDataset() {
        return dataset;
    }

    public ChartData getData() {
        return data;
    }

    /**
     * Changes the color of the data set.
     *
     * @param argb  new color
     */
    public void setColor(int argb) {
        getDataset().setColor(argb);
    }

    /**
     * Changes the label of the data set.
     *
     * @param text  new label text
     */
    public void setLabel(String text) {
        getDataset().setLabel(text);
    }

    /**
     * Sets the elements of the Data Series from a CSV-formatted String.
     *
     * @param elements String in CSV format
     */
    public void setElements(String elements) {
        // Get the expected number of tuples
        int tupleSize = getTupleSize();

        // Split all the CSV entries by comma
        String[] entries = elements.split(",");

        // Iterate over every tuple (by grouping entries)
        // We start from tupleSize - 1 since the (tupleSize - 1)-th
        // entry will be the last entry of the tuple.
        // The index is incremented by the tupleSize to move to the next
        // group of entries for a tuple.
        for (int i = tupleSize - 1; i < entries.length; i += tupleSize) {
            List<String> tupleEntries = new ArrayList<String>();

            // Iterate over all the tuple entries
            // First entry is in (i - tupleSize + 1)
            for (int j = tupleSize - 1; j >= 0; --j) {
                int index = i - j;
                tupleEntries.add(entries[index]);
            }

            // Add entry from the parsed tuple
            addEntryFromTuple(YailList.makeList(tupleEntries));
        }
    }

    /**
     * Imports data from a YailList which contains nested tuples
     *
     * @param list  YailList containing tuples
     */
    public void importFromList(YailList list) {
        // Iterate over all the tuples
        for (int i = 0; i < list.size(); ++i) {
            YailList tuple = (YailList)list.getObject(i);
            addEntryFromTuple(tuple);
        }
    }

    /**
     * Imports data from the specified list of columns with
     * the specified row size.
     *
     * The row size is used to create a column with default
     * values in case of an absence of data.
     *
     * @param columns  columns to import data from
     */
    public void importFromCSV(YailList columns) {
        if (columns == null) {
            return;
        }

        // Establish the row count of the specified columns
        int rows = 0;

        for (int i = 0; i < columns.size(); ++i) {
            YailList column = (YailList)columns.getObject(i);

            if (column.size() != 0) {
                rows = column.size();
                break;
            }
        }

        if (rows == 0) {
            // No rows exist. Do nothing.
            return;
        }

        // Initially, the final column List is created (empty
        // column Lists should be populated with default values)
        ArrayList<YailList> dataColumns = new ArrayList<YailList>();

        for (int i = 0; i < columns.size(); ++i) {
            // Get the column element
            YailList column = (YailList)columns.getObject(i);

            if (column.size() == 0) { // Column is empty, populate it with default values
                dataColumns.add(getDefaultValues(rows));
            } else { // Add the specified CSV column to the data columns to use for importing
                dataColumns.add(column);
            }
        }

        // Import from the finalized CSV columns.
        importFromCSVColumns(dataColumns, rows);
    }

    /**
     * Imports data from the specified set of CSV column data and
     * the specified number of rows.
     *
     * The first element is skipped, since it is assumed that it
     * is the column name.
     *
     * @param columns  List of fixed-width columns, each of which contain data
     * @param rows  Number of rows in the CSV (number of elements in the columns)
     */
    private void importFromCSVColumns(ArrayList<YailList> columns, int rows) {
        List<YailList> tuples = new ArrayList<YailList>();

        // Generate tuples from the columns
        for (int i = 1; i < rows; ++i) {
            ArrayList<String> tupleElements = new ArrayList<String>();

            // Add entries to the tuple from all i-th values (i-th row)
            // of the data columns.
            for (YailList column : columns) {
                tupleElements.add(column.getString(i));
            }

            // Create the YailList tuple representation and add it to the
            // list of tuples used.
            YailList tuple = YailList.makeList(tupleElements);
            tuples.add(tuple);
        }

        // Use the generated tuple list in the importFromList method to
        // import the data.
        importFromList(YailList.makeList(tuples));
    }

    /**
     * Adds an entry from a specified tuple.
     * @param tuple  Tuple representing the entry to add
     */
    public abstract void addEntryFromTuple(YailList tuple);

    /**
     * Deletes all the entries in the Data Series.
     */
    public void clearEntries() {
        getDataset().clear();
    }

    /**
     * Sets the default styling properties of the Data Series.
     */
    protected abstract void setDefaultStylingProperties();

    /**
     * Returns a YailList of the specified size containing the
     * default values for the Data Series.
     * To be used in the context of importing data from sources
     * where the values for a certain dimension are not present
     * (e.g. y values are present, but x values are not)
     *
     * @param size  Number of entries to return
     * @return  YailList of the specified number of entries containing the default values.
     */
    protected abstract YailList getDefaultValues(int size);
}
