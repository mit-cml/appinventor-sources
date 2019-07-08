package com.google.appinventor.components.runtime;

import android.app.Activity;

public class AreaChartView extends LineChartView {
    public AreaChartView(Activity context) {
        super(context);
    }

    @Override
    public ChartDataModel createChartModel() {
        return new AreaChartDataModel(data);
    }
}
