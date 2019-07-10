package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;

public abstract class LineChartViewBase extends ChartView<LineChart, LineData> {
    /**
     * Instantiate a new LineChartViewBase in the given context.
     * @param context  Context to instantiate view in
     */
    protected LineChartViewBase(Activity context) {
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
    protected void initializeDefaultSettings() {
        super.initializeDefaultSettings();

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Position X axis to the bottom
        chart.getAxisRight().setDrawLabels(false); // Disable right Y axis so there's only one
    }
}
