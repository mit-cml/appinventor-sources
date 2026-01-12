// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.runtime.util.ChartDataSourceUtil;
import com.google.appinventor.components.runtime.util.YailList;
import gnu.mapping.Symbol;

import java.util.ArrayList;
import java.util.List;

public abstract class DataModel<E> {
  /**
   * Local List of entries; The modifications of the Data are made
   * directly to these Entries, which are meant to be detached from
   * the Dataset object itself to prevent exceptions & crashes due
   * to asynchronous operations.
   */
  protected List<E> entries;

  /**
   * Limit the maximum allowed real-time data entries
   * Since real-time data comes in fast, the case of
   * multi-data source input is unhandled since it's
   * better to avoid it.
   */
  protected int maximumTimeEntries = 200;

  /**
   * Enum used to specify the criterion to use for entry filtering/comparing.
   */
  public enum EntryCriterion {
    All, // Return all entries
    XValue,
    YValue
  }

  /**
   * Initializes a new DataModel object instance.
   */
  protected DataModel() {
    entries = new ArrayList<>();
  }

  /**
   * Adds an entry from a specified tuple.
   *
   * @param tuple Tuple representing the entry to add
   */
  public abstract void addEntryFromTuple(YailList tuple);

  /**
   * Deletes all the entries in the DataModel.
   */
  public abstract void clearEntries();

  /**
   * Returns the size of the tuples that this Data Series
   * accepts.
   *
   * @return tuple size (integer)
   */
  protected abstract int getTupleSize();

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
      List<String> tupleEntries = new ArrayList<>();

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
   * @param list List containing tuples
   */
  public void importFromList(List<?> list) {
    // Iterate over all the entries of the List
    for (Object entry : list) {
      YailList tuple = null;

      if (entry instanceof YailList) {
        // Convert entry to YailList
        tuple = (YailList) entry;
      } else if (entry instanceof List) {
        // List has to be converted to a YailList
        tuple = YailList.makeList((List<?>) entry);
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
   * @param values List of values to remove
   */
  public void removeValues(List<?> values) {
    // Iterate all the entries of the generic List)
    for (Object entry : values) {
      YailList tuple = null;

      // Entry is a List; Possibly a tuple
      if (entry instanceof YailList) {
        tuple = (YailList) entry;
      } else if (entry instanceof List) {
        // Create a tuple from the entry
        tuple = YailList.makeList((List<?>) entry);
      } else if (entry instanceof Symbol) {
        continue;  // Skip *list* header
      }

      if (tuple == null) {
        continue;
      }

      // Attempt to remove entry
      removeEntryFromTuple(tuple);
    }
  }

  /**
   * Imports data from the specified list of columns.
   * Tuples are formed from the rows of the combined
   * columns in order of the columns.
   *
   * <p>The first element is skipped, since it is assumed that it
   * is the column name.
   *
   * @param columns columns to import data from
   */
  public void importFromColumns(YailList columns, boolean hasHeaders) {
    // Get a YailList of tuples from the specified columns
    YailList tuples = getTuplesFromColumns(columns, hasHeaders);

    // Use the generated tuple list in the importFromList method to
    // import the data.
    importFromList(tuples);
  }

  /**
   * Constructs and returns a List of tuples from the specified Columns List.
   * The Columns List is expected to be a List containing Lists, where each
   * List corresponds to a column, the first entry of which is the header/name
   * of the column (hence it is skipped in generating data)
   *
   * @param columns List of columns to generate tuples from
   * @return Generated List of tuples from the columns
   */
  public YailList getTuplesFromColumns(YailList columns, boolean hasHeaders) {
    // Determine the (maximum) row count of the specified columns
    int rows = ChartDataSourceUtil.determineMaximumListSize(columns);

    List<YailList> tuples = new ArrayList<>();

    // Generate tuples from the columns
    for (int i = hasHeaders ? 1 : 0; i < rows; ++i) {
      ArrayList<String> tupleElements = new ArrayList<>();

      // Add entries to the tuple from all i-th values (i-th row)
      // of the data columns.
      for (int j = 0; j < columns.size(); ++j) {
        Object value = columns.getObject(j);

        // Invalid column specified; Add default value (minus one to
        // compensate for the skipped value)
        if (!(value instanceof YailList)) {
          tupleElements.add(getDefaultValue(i - 1));
          continue;
        }

        // Safe-cast value to YailList
        YailList column = (YailList) value;

        if (column.size() > i) { // Entry exists in column
          // Add entry from column
          tupleElements.add(column.getString(i));
        } else if (column.size() == 0) { // Column empty (default value should be used)
          // Use default value instead (we use an index minus one to componsate
          // for the skipped initial value)
          tupleElements.add(getDefaultValue(i - 1));
        } else { // Column too small
          // Add blank entry (""), up for the addEntryFromTuple method
          // to interpret.
          tupleElements.add("");

          // TODO: Make this a user-configurable flag
          // Use default value instead
          //tupleElements.add(getDefaultValue(i));
        }
      }

      // Create the YailList tuple representation and add it to the
      // list of tuples used.
      YailList tuple = YailList.makeList(tupleElements);
      tuples.add(tuple);
    }

    // Return result as YailList
    return YailList.makeList(tuples);
  }

  /**
   * Removes an entry from the Data Series from the specified
   * tuple (provided the entry exists).
   *
   * @param tuple Tuple representing the entry to remove
   */
  public abstract void removeEntryFromTuple(YailList tuple);

  /**
   * Removes the entry in the specified index, provided that the
   * index is within bounds.
   *
   * @param index Index of the Entry to remove
   */
  public abstract void removeEntry(int index);

  /**
   * Checks whether an entry exists in the Data Series.
   *
   * @param tuple Tuple representing the entry to look for
   * @return true if the Entry exists, false otherwise
   */
  public abstract boolean doesEntryExist(YailList tuple);

  /**
   * Finds and returns all the entries by the specified criterion and value.
   *
   * <p>The entries are returned as tuple (YailList) representations.
   *
   * @param value     value to use for comparison
   * @param criterion criterion to use for comparison
   * @return YailList of entries represented as tuples matching the specified conditions
   */
  public abstract YailList findEntriesByCriterion(String value, EntryCriterion criterion);

  /**
   * Returns all the entries of the Data Series in the form of tuples (YailLists).
   *
   * @return YailList of all entries represented as tuples
   */
  public YailList getEntriesAsTuples() {
    // Use the All criterion to get all the Entries
    return findEntriesByCriterion("0", EntryCriterion.All);
  }

  /**
   * Check whether the entry matches the specified criterion.
   *
   * @param entry     entry to check against
   * @param criterion criterion to check with (e.g. x value)
   * @param value     value to use for comparison (as a String)
   * @return true if the entry matches the criterion
   */
  protected abstract boolean isEntryCriterionSatisfied(Entry entry,
      EntryCriterion criterion, String value);

  /**
   * Creates an Entry from the specified tuple.
   *
   * @param tuple Tuple representing the entry to create
   * @return new Entry object instance representing the specified tuple
   */
  public abstract Entry getEntryFromTuple(YailList tuple);

  /**
   * Returns a YailList tuple representation of the specified entry.
   *
   * @param entry Entry to convert to tuple
   * @return tuple (YailList) representation of the Entry
   */
  public abstract YailList getTupleFromEntry(Entry entry);

  /**
   * Finds the index of the specified Entry in the Data Series.
   * Returns -1 if the Entry does not exist.
   *
   * <p>TODO: Primarily used due to equals not implemented in MPAndroidChart (needed for specific
   * TODO: operations). In the future, this method will probably become obsolete if it ever gets
   * TODO: fixed (post-v3.1.0).
   *
   * @param entry Entry to find
   * @return index of the entry, or -1 if entry is not found
   */
  public abstract int findEntryIndex(Entry entry);

  /**
   * Adds the specified entry as a time entry to the Data Series.
   *
   * <p>The method handles additional logic for removing excess values
   * if the count exceeds the threshold.
   *
   * @param tuple tuple representing the time entry
   */
  public abstract void addTimeEntry(YailList tuple);

  /**
   * Sets the maximum time entries to be kept in the Data Series.
   *
   * @param entries number of entries to keep
   */
  public void setMaximumTimeEntries(int entries) {
    maximumTimeEntries = entries;
  }

  /**
   * Sets the default styling properties of the Data Series.
   */
  protected void setDefaultStylingProperties() {
    /*
        The method body is left empty to not require data models
        which do not need any default styling properties to override
        the method by default.
     */
  }

  /**
   * Returns default tuple entry value to use when a value
   * is not present.
   *
   * @param index index for the value
   * @return value corresponding to the specified index
   */
  protected String getDefaultValue(int index) {
    // Return value which directly corresponds to the index
    // number. So default values go as 0, 1, 2, ..., N
    return index + "";
  }

  /**
   * Checks equality between two entries.
   *
   * @param e1 first Entry to compare
   * @param e2 second Entry to compare
   * @return true if the entries are equal
   */
  // REMARK
  // The reason why this method is needed is due to the equals()
  // and equalTo() methods not being implemented fully to fit
  // the requirements of the comparison done in the models.
  // equalTo() does not check label equality (for Pie Charts)
  // and equals() checks memory references instead of values.
  protected boolean areEntriesEqual(Entry e1, Entry e2) {
    return e1.equalTo(e2);
  }

  /**
   * Returns the entries of the Chart Data Model.
   *
   * @return List of entries of the Chart Data Model (Data Series)
   */
  public abstract List<E> getEntries();
}
