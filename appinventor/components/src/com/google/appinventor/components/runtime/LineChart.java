package com.google.appinventor.components.runtime;

import android.view.View;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;

@DesignerComponent(version = 1,
    category = ComponentCategory.CHARTS,
    description = "A component that allows visualizing data using lines")
@SimpleObject
public final class LineChart extends ChartBase<com.github.mikephil.charting.charts.LineChart, LineData> {

    /**
     * Creates a new Line Chart component.
     *
     * @param container container, component will be placed in
     */
    public LineChart(ComponentContainer container) {
        super(container);

        view = new com.github.mikephil.charting.charts.LineChart(container.$context());
        data = new LineData();
        view.setData(data);

        view.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Position X axis to the bottom
        view.getAxisRight().setDrawLabels(false); // Disable right Y axis so there's only one

        initChart();
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public LineChartModel createChartModel() {
        return new LineChartModel(data);
    }
}
