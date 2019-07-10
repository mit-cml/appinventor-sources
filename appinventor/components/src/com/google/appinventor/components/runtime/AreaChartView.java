package com.google.appinventor.components.runtime;

import android.app.Activity;

public class AreaChartView extends LineChartViewBase {
    public AreaChartView(Activity context) {
        super(context);

        // In order for the fill under the Chart to work on SDK < 18,
        // hardware acceleration has to be disabled.
        chart.setHardwareAccelerationEnabled(false);
    }

    @Override
    public ChartDataModel createChartModel() {
        return new AreaChartDataModel(data);
    }
}
