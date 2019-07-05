package com.google.appinventor.components.runtime;

import android.graphics.Color;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Arrays;

@DesignerComponent(version = 1,
    description = "A component that holds (x, y)-coordinate based data",
    category = ComponentCategory.CHARTS,
    iconName = "images/web.png")
@SimpleObject
public final class CoordinateData extends ChartDataBase {
    /**
     * Creates a new Coordinate Data component.
     */
    public CoordinateData(ChartBase chartContainer) {
        super(chartContainer);
    }

    /**
     * Adds entry to the Data Series.
     *
     * @param x - x value of entry
     * @param y - y value of entry
     */
    @SimpleFunction(description = "Adds (x, y) point to the Coordinate Data.")
    public void AddEntry(float x, float y) {
        // Create a 2-tupl1, and add the tuple to the Data Series
        YailList pair = YailList.makeList(Arrays.asList(x, y));
        chartModel.addEntryFromTuple(pair);

        // Refresh Chart with new data
        refreshChart();
    }
}
