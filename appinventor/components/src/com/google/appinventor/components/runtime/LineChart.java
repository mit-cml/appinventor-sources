package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;

@DesignerComponent(version = 1,
    category = ComponentCategory.CHARTS,
    description = "A component that allows visualizing data using lines")
@SimpleObject
public final class LineChart extends ChartBase<com.github.mikephil.charting.charts.LineChart, LineChartData> {

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

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT + ":com.google.appinventor.components.runtime.LineChartData")
    public void ChartData(LineChartData data) {
        // Remove this Chart from previous LineChartData component
        if (data != null) {
            data.removeChart(this);
        }

        // Add Chart to LineChartData component
        data.addChart(this);

        this.data = data;
        view.setData(this.data.getChartData());
        view.invalidate();
    }
}
