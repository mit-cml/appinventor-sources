package com.google.appinventor.components.runtime.util;

import com.github.mikephil.charting.data.DataSet;

public abstract class ChartModel<T extends DataSet> {
    protected T dataset;

    /**
     * Initializes a new ChartModel object instance.
     *
     * @param dataset  Chart dataset instance
     */
    protected ChartModel(T dataset) {
        this.dataset = dataset;
    }

    public T getDataset() {
        return dataset;
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
}
