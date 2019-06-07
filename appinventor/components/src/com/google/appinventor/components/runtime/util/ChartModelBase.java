package com.google.appinventor.components.runtime.util;

import com.github.mikephil.charting.data.DataSet;

public abstract class ChartModelBase<T extends DataSet> {
    protected T dataset;

    /**
     * Initializes a new ChartModelBase object instance.
     *
     * @param dataset  Chart dataset instance
     */
    protected ChartModelBase(T dataset) {
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
}
