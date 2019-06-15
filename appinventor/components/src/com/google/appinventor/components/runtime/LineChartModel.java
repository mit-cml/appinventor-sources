package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LineChartModel extends ChartModel<LineDataSet, LineData> {
    /**
     * Initializes a new LineChartModel object instance.
     *
     * @param data  Line Chart Data object instance
     */
    public LineChartModel(LineData data) {
        super(data);
        dataset = new LineDataSet(new ArrayList<Entry>(), "");
        dataset.setDrawCircleHole(false);
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
                float xValue = Float.parseFloat(entries[i-1]);
                float yValue = Float.parseFloat(entries[i]);
                Entry entry = new Entry(xValue, yValue);
                values.add(entry);
            } catch (NumberFormatException e) {
                return; // Do not update entries; Invalid input.
            }
        }

        // Sort the Entries by X value.
        Collections.sort(values, new Comparator<Entry>() {
            @Override
            public int compare(Entry entry, Entry t1) {
                return Float.compare(entry.getX(), t1.getX());
            }
        });

        dataset.setValues(values);
    }
}
