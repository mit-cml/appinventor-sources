package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;

public class LineChartView extends LineChartViewBase {
    /**
     * Instantiate a new LineChartView in the given context.
     *
     * @param context Context to instantiate view in
     */
    public LineChartView(Form context) {
        super(context);
    }

    @Override
    public ChartDataModel createChartModel() {
        return new LineChartDataModel(data);
    }
}
