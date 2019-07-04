package com.google.appinventor.components.runtime;

import android.app.Activity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;

public class LineChartView extends ChartViewBase<LineChart, LineData> {

    public LineChartView(Activity context) {
        chart = new LineChart(context);
        data = new LineData();

        chart.setData(data);

        initializeDefaultSettings();
    }

    @Override
    public ChartDataModel createChartModel() {
        return new LineChartDataModel(data);
    }

    @Override
    protected void initializeDefaultSettings() {
        super.initializeDefaultSettings();

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Position X axis to the bottom
        chart.getAxisRight().setDrawLabels(false); // Disable right Y axis so there's only one
    }
}
