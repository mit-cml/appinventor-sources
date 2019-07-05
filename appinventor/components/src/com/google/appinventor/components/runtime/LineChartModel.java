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

    @Override
    protected int getTupleSize() {
        return 2;
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
