package com.google.appinventor.components.runtime;

import android.graphics.Color;
import com.github.mikephil.charting.data.Entry;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;

import java.util.ArrayList;

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
        boolean addDataset = (chartModel.getDataset().getEntryCount() == 0);

        chartModel.addEntry(x, y);

        // Data set was empty before. We should add it to the Chart.
        if (addDataset) {
            container.AddDataSet(chartModel.getDataset()); // Safe add. DataModel guarantees proper types.
        }

        // Refresh Chart with new data
        container.Refresh();
    }
}
