package com.google.appinventor.components.runtime.util;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class LineChartModel extends ChartModel<LineDataSet> {
    /**
     * Initializes a new LineChartModel object instance with an
     * empty dataset.
     */
    public LineChartModel() {
        super(new LineDataSet(new ArrayList<Entry>(), ""));
    }

    /**
     * Initializes a new LineChartModel object instance.
     *
     * @param dataset  Line Chart Data Set object instance
     */
    public LineChartModel(LineDataSet dataset) {
        super(dataset);
    }

    /**
     * Adds a (x, y) entry to the Line Data Set.
     *
     * @param x  x value
     * @param y  y value
     */
    public void addEntry(float x, float y) {
        Entry entry = new Entry(x, y);
        dataset.addEntryOrdered(entry);
    }

    @Override
    public void setColor(int argb) {
        super.setColor(argb);
        dataset.setCircleColor(argb);
    }
}
