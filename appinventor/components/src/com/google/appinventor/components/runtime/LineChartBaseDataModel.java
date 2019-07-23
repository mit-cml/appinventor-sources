package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;

public abstract class LineChartBaseDataModel extends PointChartDataModel<LineDataSet, LineData>  {
    /**
     * Initializes a new LineChartBaseDataModel object instance.
     *
     * @param data  Line Chart Data object instance
     */
    protected LineChartBaseDataModel(LineData data) {
        super(data);
        dataset = new LineDataSet(new ArrayList<Entry>(), "");
        this.data.addDataSet(dataset); // Safe add
        setDefaultStylingProperties();
    }

    /**
     * Adds a (x, y) entry to the Line Data Set.
     *
     * @param x  x value
     * @param y  y value
     */
    @Override
    public void addEntry(float x, float y) {
        Entry entry = new Entry(x, y);
        getDataset().addEntryOrdered(entry);
    }

    @Override
    public void setColor(int argb) {
        super.setColor(argb);
        getDataset().setCircleColor(argb);
    }

    @Override
    protected void setDefaultStylingProperties() {
        getDataset().setDrawCircleHole(false); // Draw full circle instead of a hollow one
    }
}
