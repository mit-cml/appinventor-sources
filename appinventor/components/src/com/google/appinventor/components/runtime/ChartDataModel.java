// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Base class to represent Chart Data Models. The class (and subclasses)
 * are used to handle the data part of the Chart component. One model
 * represents a single Data Series in a Chart (e.g. one line in a Line Chart
 * or one list of points in a Scatter Chart).
 *
 * @param <E> MPAndroidChart class for Entry type.
 * @param <T> MPAndroidChart class for DataSet collection.
 * @param <D> MPAndroidChart class for ChartData series collection.
 * @param <C> MPAndroidChart class for Chart view.
 * @param <V> Type of the view that renders the data model.
 */
public abstract class ChartDataModel<
    E extends Entry,
    T extends IDataSet<E>,
    D extends ChartData<T>,
    C extends com.github.mikephil.charting.charts.Chart<D>,
    V extends ChartView<E, T, D, C, V>> extends DataModel<E> {
  protected D data;
  protected T dataset;
  protected V view;

  /**
   * Initializes a new ChartDataModel object instance.
   *
   * @param data Chart data instance
   * @param view Chart View to link model to
   */
  protected ChartDataModel(D data, V view) {
    this.data = data;
    this.view = view;

    entries = new ArrayList<>();
  }

  /**
   * Returns the size of the tuples that this Data Series
   * accepts.
   *
   * @return tuple size (integer)
   */
  protected abstract int getTupleSize();

  /**
   * Returns the Data Series of the Data Model.
   * The method is made synchronized to avoid concurrent
   * access between threads, which can cause exceptions
   * if multiple threads try to modify the Dataset object
   * at the same time.
   *
   * @return Data Series object of the Data model
   */
  public T getDataset() {
    return dataset;
  }

  public D getData() {
    return data;
  }

  /**
   * Changes the color of the data set.
   *
   * @param argb new color
   */
  public void setColor(int argb) {
    if (dataset instanceof DataSet) {
      ((DataSet<?>) dataset).setColor(argb);
    }
  }

  /**
   * Changes the colors of the Data Series from the passed in Colors List.
   *
   * @param colors List of colors to set to the Data Series
   */
  public void setColors(List<Integer> colors) {
    // With regards to the Colors property setting for the
    // ScatterChartDataModel, currently an issue exists:
    // https://github.com/PhilJay/MPAndroidChart/issues/4483
    // which sets the same color to 2 points at once.
    if (dataset instanceof DataSet) {
      ((DataSet<?>) dataset).setColors(colors);
    }
  }

  /**
   * Changes the label of the data set.
   *
   * @param text new label text
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
   * Removes an entry from the Data Series from the specified
   * tuple (provided the entry exists).
   *
   * @param tuple Tuple representing the entry to remove
   */
  public void removeEntryFromTuple(YailList tuple) {
    // Construct an entry from the specified tuple
    Entry entry = getEntryFromTuple(tuple);

    if (entry != null) {
      // TODO: The commented line should be used instead. However, the library does not yet
      // TODO: implement equals methods in it's entries as of yet, so the below method fails.
      // dataset.removeEntry(entry);

      // Get the index of the entry
      int index = findEntryIndex(entry);

      removeEntry(index);
    }
  }

  /**
   * Removes the entry in the specified index, provided that the
   * index is within bounds.
   *
   * @param index Index of the Entry to remove
   */
  public void removeEntry(int index) {
    // Entry exists; remove it
    if (index >= 0) {
      entries.remove(index);
    }
  }

  /**
   * Checks whether an entry exists in the Data Series.
   *
   * @param tuple Tuple representing the entry to look for
   * @return true if the Entry exists, false otherwise
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
   * <p>The entries are returned as tuple (YailList) representations.
   *
   * @param value     value to use for comparison
   * @param criterion criterion to use for comparison
   * @return YailList of entries represented as tuples matching the specified conditions
   */
  public YailList findEntriesByCriterion(String value, EntryCriterion criterion) {
    List<YailList> entries = new ArrayList<>();

    for (Entry entry : this.entries) {
      // Check whether the provided criterion & value combination are satisfied
      // according to the current Entry
      if (isEntryCriterionSatisfied(entry, criterion, value)) {
        // Criterion satisfied; Add entry to resulting List
        entries.add(getTupleFromEntry(entry));
      }
    }

    return YailList.makeList(entries);
  }

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
  protected boolean isEntryCriterionSatisfied(Entry entry, EntryCriterion criterion,
      String value) {
    boolean criterionSatisfied = false;

    switch (criterion) {
      case All: // Criterion satisfied no matter the value, since all entries should be returned
        criterionSatisfied = true;
        break;

      case XValue: // Criterion satisfied based on x value match with the value
        // PieEntries and regular entries require different
        // handling sine PieEntries have String x values
        if (entry instanceof PieEntry) {
          // Criterion is satisfied for a Pie Entry only if
          // the label is equal to the specified value
          PieEntry pieEntry = (PieEntry) entry;
          criterionSatisfied = pieEntry.getLabel().equals(value);
        } else {
          // X value is a float, so it has to be parsed and
          // compared. If parsing fails, the criterion is
          // not satisfied.
          try {
            float xValue = Float.parseFloat(value);
            float compareValue = entry.getX();

            // Since Bar Chart grouping applies offsets to x values,
            // and the x values are expected to be integers, the
            // value has to be floored.
            if (entry instanceof BarEntry) {
              compareValue = (float) Math.floor(compareValue);
            }

            criterionSatisfied = (compareValue == xValue);
          } catch (NumberFormatException e) {
            // Do nothing (value already false)
          }
        }
        break;

      case YValue: // Criterion satisfied based on y value match with the value
        try {
          // Y value is always a float, therefore the String value has to
          // be parsed.
          float yValue = Float.parseFloat(value);
          criterionSatisfied = (entry.getY() == yValue);
        } catch (NumberFormatException e) {
          // Do nothing (value already false)
        }
        break;

      default:
        throw new IllegalArgumentException("Unknown criterion: " + criterion);
    }

    return criterionSatisfied;
  }
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
  public int findEntryIndex(Entry entry) {
    for (int i = 0; i < entries.size(); ++i) {
      Entry currentEntry = entries.get(i);

      // Check whether the current entry is equal to the
      // specified entry. Note that (in v3.1.0), equals()
      // does not yield the same result.
      if (areEntriesEqual(currentEntry, entry)) {
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
    entries.clear();
  }

  /**
   * Adds the specified entry as a time entry to the Data Series.
   *
   * <p>The method handles additional logic for removing excess values
   * if the count exceeds the threshold.
   *
   * @param tuple tuple representing the time entry
   */
  public void addTimeEntry(YailList tuple) {
    // If the entry count of the Data Series entries exceeds
    // the maximum allowed time entries, then remove the first one
    if (entries.size() >= maximumTimeEntries) {
      entries.remove(0);
    }

    // Add entry from the specified tuple
    // TODO: Support for multi-dimensional case (currently tuples always consist
    // TODO: of two elements)
    addEntryFromTuple(tuple);
  }

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
  public List<E> getEntries() {
    return Collections.unmodifiableList(entries);
  }
}
