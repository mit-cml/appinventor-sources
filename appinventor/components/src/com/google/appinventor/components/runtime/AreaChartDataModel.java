package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineData;

import java.util.List;

public class AreaChartDataModel extends LineChartBaseDataModel {
    /**
     * Initializes a new AreaChartDataModel object instance.
     *
     * @param data Line Chart Data object instance
     */
    public AreaChartDataModel(LineData data) {
        super(data);
    }

    @Override
    public void setColor(int argb) {
        super.setColor(argb);
        dataset.setFillColor(argb);
    }

    @Override
    public void setColors(List<Integer> colors) {
        super.setColors(colors);
        dataset.setFillColor(colors.get(0));
    }

    @Override
    protected void setDefaultStylingProperties() {
        super.setDefaultStylingProperties();
        dataset.setDrawFilled(true);
        dataset.setFillAlpha(150);
    }
}
