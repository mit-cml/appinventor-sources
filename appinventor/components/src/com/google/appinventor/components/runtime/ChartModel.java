package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;

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
}
