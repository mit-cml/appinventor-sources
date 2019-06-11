package com.google.appinventor.components.runtime.util;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartModel extends ChartModel<LineDataSet> {
    /**
     * Initializes a new LineChartModel object instance.
     *
     * @param data  Line Chart Data object instance
     */
    public LineChartModel(LineData data) {
        super(data);
        dataset = new LineDataSet(new ArrayList<Entry>(), "");
        this.data.addDataSet(dataset); // Safe add
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

    @Override
    public void setElements(String elements) {
        String[] entries = elements.split(",");

        List<Entry> values = new ArrayList<Entry>();

        for (int i = 1; i < entries.length; i += 2) {
            try {
                float xValue = Float.parseFloat(entries[i]);
                float yValue = Float.parseFloat(entries[i-1]);
                Entry entry = new Entry(xValue, yValue);
                values.add(entry);
            } catch (NumberFormatException e) {
                return; // Do not update entries; Invalid input.
            }
        }

        dataset.setValues(values);
    }
}
