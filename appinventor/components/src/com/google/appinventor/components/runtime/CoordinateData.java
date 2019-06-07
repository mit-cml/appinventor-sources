package com.google.appinventor.components.runtime;

import android.graphics.Color;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;

import java.util.ArrayList;

@DesignerComponent(version = 1,
    description = "A component that holds point-based data",
    category = ComponentCategory.CHARTS,
    iconName = "images/web.png")
@SimpleObject
public final class PointChartData extends ChartDataBase {
    protected ChartBase container = null;

    /**
     * Creates a new Line Chart Data component.
     */
    public PointChartData(ChartBase chartContainer) {
        this.container = chartContainer;

//        chartDataSet = new LineDataSet(new ArrayList<Entry>(), "Data");
//        chartDataSet.setColor(Color.BLACK);
//        chartDataSet.setCircleColor(Color.BLACK);
    }

    /**
     * Adds entry to the Line Data Series
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction(description = "Adds (x, y) point to the Line Data.")
    public void AddEntry(float x, float y) {
        boolean addDataset = (chartDataSet.getEntryCount() == 0);

        Entry entry = new Entry(x, y);
        chartDataSet.addEntryOrdered(entry);

        if (addDataset) {
            container.AddDataSet(chartDataSet);
        }

        container.Refresh();
    }
}
