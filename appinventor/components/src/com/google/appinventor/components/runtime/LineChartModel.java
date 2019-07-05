package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

    @Override
    public void importFromTinyDB(TinyDB tinyDB) {
        Map<String, ?> map = tinyDB.getAllValues();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            try {
                String key = entry.getKey();
                String value = (String)entry.getValue();

                float x = Float.parseFloat(key);
                float y = Float.parseFloat(value);

                // Potential improvement here: If we are overriding data,
                // then it is more efficient to add everything to a List,
                // sort the list, and then set the list as the data set's
                // entries (O(n log n)). Since addEntry uses addEntryOrdered,
                // this loop will have O(n^2) complexity.
                addEntry(x, y);
            } catch (NumberFormatException e) {
                // Nothing happens: Do not add value on NumberFormatException
            }
        }
    }

    @Override
    public void addEntryFromTuple(YailList tuple) {
        try {
            String xValue = tuple.getString(0);
            String yValue = tuple.getString(1);

            try {
                float x = Float.parseFloat(xValue);
                float y = Float.parseFloat(yValue);

                addEntry(x, y);
            } catch (NumberFormatException e) {
                // Nothing happens: Do not add entry on NumberFormatException
            }
        } catch (Exception e) {
            // 2-tuples are invalid when null entries are present, or if
            // the number of entries is not sufficient to form a pair.
            // TODO: Show toast error notification
        }
    }
}
