package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.annotations.DesignerComponent;
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

        // TBD: Support multiple data sets, allow changing label
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), "Data");

        // Instantiate the LineData object
        chartData = new LineData(lineDataSet);
    }
}
