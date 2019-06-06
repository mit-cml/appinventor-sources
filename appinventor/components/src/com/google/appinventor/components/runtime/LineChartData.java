package com.google.appinventor.components.runtime;

import android.graphics.Color;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;

import java.util.ArrayList;

@DesignerComponent(version = 1,
    description = "A component that holds data for Line Charts",
    category = ComponentCategory.CHARTS,
    iconName = "images/web.png")
@SimpleObject
public final class LineChartData extends ChartDataBase<LineData> {
    protected LineChart container = null;

    /**
     * Creates a new Line Chart Data component.
     */
    public LineChartData(LineChart lineChartContainer) {
        this.container = lineChartContainer;

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

        // TBD: Multiple dataset support
        // For now, this stores all the data in one Data Set.
        // The reason for this if statement is because passing in a DataSet with
        // no entries will cause exceptions, so if there are no entries initially,
        // the ChartData object should have no DataSet attached to it.
        if (chartData.getDataSetCount() == 0) {
            LineDataSet dataSet = new LineDataSet(new ArrayList<Entry>(), "Data");
            dataSet.setColor(Color.BLACK);
            dataSet.setCircleColor(Color.BLACK);
            dataSet.addEntry(entry);
            chartData.addDataSet(dataSet);
        } else {
            chartData.getDataSetByIndex(0).addEntryOrdered(entry);
            chartData.notifyDataChanged();
            // chartData.addEntry(entry, 0);
        }

        refreshCharts();
    }
}
