package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;

import java.util.ArrayList;

@DesignerComponent(version = 1,
    description = "",
    category = ComponentCategory.CHARTS,
    nonVisible = true,
    iconName = "images/web.png")
@SimpleObject
public final class ChartDataLine extends ChartDataBase<LineData> {
    /**
     * Creates a new Line Chart Data component.
     *
     * @param form the container that this component will be placed in
     */
    public ChartDataLine(Form form) {
        super(form);

        // Instantiate new LineDataSet object
        chartData = new LineData();
    }

    /**
     * Adds entry to the Line Data Series
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction(description = "Adds (x, y) point to the Line Data.")
    public void AddEntry(int x, int y) {
        Entry entry = new Entry(x, y);

        if (chartData.getDataSetCount() == 0) {
            LineDataSet dataSet = new LineDataSet(new ArrayList<Entry>(), "Data");
            dataSet.addEntry(entry);
            chartData.addDataSet(dataSet);
        } else {
            chartData.addEntry(entry, 0);
        }

        refreshCharts();
    }
}
