package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class ChartDataModel<T extends DataSet, D extends ChartData> {
    protected D data;
    protected T dataset;

    /**
     * Enum used to specify the criterion to use for entry filtering/comparing.
     */
    public enum EntryCriterion {
        All, // Return all entries
        XValue,
        YValue;
    }

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
     * Imports data from a List object.
     * Valid tuple entries are imported, and the invalid entries are ignored.
     *
     * @param list  List containing tuples
     */
    public void importFromList(List list) {
      // Iterate over all the entries of the List
      for (Object entry : list) {
        YailList tuple = null;

        if (entry instanceof YailList) {
          // Convert entry to YailList
          tuple = (YailList) entry;
        } else if (entry instanceof List) {
          // List has to be converted to a YailList
          tuple = YailList.makeList((List) entry);
        }

        // Entry could be parsed to a YailList; Attempt importing from
        // the constructed tuple.
        if (tuple != null) {
          addEntryFromTuple(tuple);
        }
      }
    }

    /**
     * Removes the specified List of values, which are expected to be tuples.
     * Invalid entries are ignored.
     *
     * @param values  List of values to remove
     */
    public void removeValues(List values) {
        // Iterate all the entries of the generic List)
        for (Object entry : values) {
            YailList tuple = null;

            // Entry is a List; Possibly a tuple
            if (entry instanceof YailList) {
                tuple = (YailList) entry;
            } else if (entry instanceof List) {
                // Create a tuple from the entry
                tuple = YailList.makeList((List)entry);
            }

            // Attempt to remove entry
            removeEntryFromTuple(tuple);
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
     * Removes an entry from the Data Series from the specified
     * tuple (provided the entry exists)
     * @param tuple  Tuple representing the entry to remove
     */
    public void removeEntryFromTuple(YailList tuple) {
        // Construct an entry from the specified tuple
        Entry entry = getEntryFromTuple(tuple);

        if (entry != null) {
            // TODO: The commented line should be used instead. However, the library does not (yet) implement
            // TODO: equals methods in it's entries as of yet, so the below method fails.
            // dataset.removeEntry(entry);

            // Get the index of the entry
            int index = findEntryIndex(entry);

            // Entry exists; remove it
            if (index >= 0) {
                getDataset().removeEntry(index);
            }
        }
    }

    /**
     * Checks whether an entry exists in the Data Series.
     *
     * @param tuple  Tuple representing the entry to look for
     * @return  true if the Entry exists, false otherwise
     */
    public boolean doesEntryExist(YailList tuple) {
        // Construct the entry from the specified tuple
        Entry entry = getEntryFromTuple(tuple);

        // Get the index of the entry
        int index = findEntryIndex(entry);

        // Entry exists only if index is non-negative
        return index >= 0;
    }

    /**
     * Finds and returns all the entries by the specified criterion and value.
     *
     * The entries are returned as tuple (YailList) representations.
     *
     * @param value  value to use for comparison
     * @param criterion  criterion to use for comparison
     * @return  YailList of entries represented as tuples matching the specified conditions
     */
    public YailList findEntriesByCriterion(String value, EntryCriterion criterion) {
        List<YailList> entries = new ArrayList<YailList>();

        for (Object dataValue : getDataset().getValues()) {
            Entry entry = (Entry) dataValue;

            // Check whether the provided criterion & value combination are satisfied
            // according to the current Entry
            if (isEntryCriterionSatisfied(entry, criterion, value)) {
                // Criterion satisfied; Add enttry to resulting List
                entries.add(getTupleFromEntry(entry));
            }
        }

        return YailList.makeList(entries);
    }

    /**
     * Returns all the entries of the Data Series in the form of tuples (YailLists)
     *
     * @return  YailList of all entries represented as tuples
     */
    public YailList getEntriesAsTuples() {
        // Use the All criterion to get all the Entries
        return findEntriesByCriterion("0", EntryCriterion.All);
    }

    /**
     * Check whether the entry matches the specified criterion.
     *
     * @param entry  entry to check against
     * @param criterion  criterion to check with (e.g. x value)
     * @param value  value to use for comparison
     * @return  true if the entry matches the criterion
     */
    protected boolean isEntryCriterionSatisfied(Entry entry, EntryCriterion criterion, String value) {
        boolean criterionSatisfied = false;

        switch (criterion) {
            case All: // Criterion satisfied no matter the value, since all entries should be returned
                criterionSatisfied = true;
                break;

            case XValue: // Criterion satisfied based on x value match with the value
                try {
                    float xValue = Float.parseFloat(value);
                    criterionSatisfied = (entry.getX() == xValue);
                } catch (NumberFormatException e) {
                    // Do nothing (value already false)
                }
                break;

            case YValue: // Criterion satisfied based on y value match with the value
                try {
                    float yValue = Float.parseFloat(value);
                    criterionSatisfied = (entry.getY() == yValue);
                } catch (NumberFormatException e) {
                    // Do nothing (value already false)
                }
                break;
        }

        return criterionSatisfied;
    }

    /**
     * Creates an Entry from the specified tuple.
     *
     * @param tuple  Tuple representing the entry to create
     * @return  new Entry object instance representing the specified tuple
     */
    public abstract Entry getEntryFromTuple(YailList tuple);

    /**
     * Returns a YailList tuple representation of the specfied entry
     * @param entry  Entry to convert to tuple
     * @return  tuple (YailList) representation of the Entry
     */
    public abstract YailList getTupleFromEntry(Entry entry);

    /**
     * Finds the index of the specified Entry in the Data Series.
     * Returns -1 if the Entry does not exist.
     *
     * TODO: Primarily used due to equals not implemented in MPAndroidChart (needed for specific operations)
     * TODO: In the future, this method will probably become obsolete if it ever gets fixed (post-v3.1.0).
     *
     * @param entry  Entry to find
     * @return  index of the entry, or -1 if entry is not found
     */
    protected int findEntryIndex(Entry entry) {
        for (int i = 0; i < getDataset().getValues().size(); ++i) {
            Entry currentEntry = getDataset().getEntryForIndex(i);

            // Check whether the current entry is equal to the
            // specified entry. Note that (in v3.1.0), equals()
            // does not yield the same result.
            if (currentEntry.equalTo(entry)) {
                // Entry matched; Return
                return i;
            }
        }

        return -1;
    }

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
