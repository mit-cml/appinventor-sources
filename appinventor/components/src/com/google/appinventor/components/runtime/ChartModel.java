package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.google.appinventor.components.runtime.util.YailList;

public abstract class ChartModel<T extends DataSet, D extends ChartData> {
    protected D data;
    protected T dataset;

    /**
     * Initializes a new ChartModel object instance.
     *
     * @param data  Chart data instance
     */
    protected ChartModel(D data) {
        this.data = data;
    }

    public T getDataset() {
        return dataset;
    }

    public ChartData getData() {
        return data;
    }

    /**
     * Adds (x, y) entry to the data set.
     *
     * @param x  x value
     * @param y  y value
     */
    public abstract void addEntry(float x, float y);

    /**
     * Changes the color of the data set.
     *
     * @param argb  new color
     */
    public void setColor(int argb) {
        dataset.setColor(argb);
    }

    /**
     * Changes the label of the data set.
     *
     * @param text  new label text
     */
    public void setLabel(String text) {
        dataset.setLabel(text);
    }

    /**
     * Sets the elements of the Data Series from a CSV-formatted String.
     *
     * @param elements String in CSV format
     */
    public abstract void setElements(String elements);

    /**
     * Adds elements to the Data Series from a specified TinyDB component
     *
     * @param tinyDB  TinyDB component to import data from
     */
    public abstract void importFromTinyDB(TinyDB tinyDB);

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
     * Adds an entry from a specified tuple.
     * @param tuple  Tuple representing the entry to add
     */
    public abstract void addEntryFromTuple(YailList tuple);

    /**
     * Deletes all the entries in the Data Series.
     */
    public void clearEntries() {
        dataset.clear();
    }
}
