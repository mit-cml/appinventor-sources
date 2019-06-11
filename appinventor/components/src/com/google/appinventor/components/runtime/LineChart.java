package com.google.appinventor.components.runtime;

import android.view.View;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.util.ChartModel;
import com.google.appinventor.components.runtime.util.LineChartModel;

import java.util.ArrayList;

@DesignerComponent(version = 1,
    category = ComponentCategory.CHARTS,
    description = "A component that allows visualizing data using lines")
@SimpleObject
public final class LineChart extends ChartBase<com.github.mikephil.charting.charts.LineChart, LineDataSet> {

    /**
     * Creates a new Line Chart component.
     *
     * @param container container, component will be placed in
     */
    public LineChart(ComponentContainer container) {
        super(container);

        view = new com.github.mikephil.charting.charts.LineChart(container.$context());

        initChart();
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public ChartModel createChartModel() {
        // Get the View's Data object instance
        LineData data = view.getData();

        // Data does not yet exist on the Chart
        if (data == null) {
            data = new LineData();
        }

        return new LineChartModel(data);
    }
}
