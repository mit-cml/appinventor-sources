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
        dataset.setFillColor(argb); // Change fill color
    }

    @Override
    public void setColors(List<Integer> colors) {
        super.setColors(colors);

        // If the colors List is non-empty, use the first color
        // as the fill color.
        if (!colors.isEmpty()) {
            dataset.setFillColor(colors.get(0));
        }
    }

    @Override
    protected void setDefaultStylingProperties() {
        super.setDefaultStylingProperties();
        dataset.setDrawFilled(true); // Enable fill underneath the lines
        dataset.setFillAlpha(100); // Set fill color to be transparent (value of 100 out of 255)
    }
}
