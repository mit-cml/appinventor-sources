package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;

public class LineChartView extends ChartViewBase<LineChart, LineData> {

    /**
     * Instantiate a new LineChartView in the given context.
     * @param context  Context to instantiate view in
     */
    public LineChartView(Activity context) {
        chart = new LineChart(context);

        data = new LineData();
        chart.setData(data);

        initializeDefaultSettings();

        // Since the Chart is stored in a RelativeLayout, settings are
        // needed to fill the Layout.
        chart.setLayoutParams(new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
