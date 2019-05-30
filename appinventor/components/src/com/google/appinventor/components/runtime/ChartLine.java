package com.google.appinventor.components.runtime;

import android.view.View;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;

@DesignerComponent(version = 1,
    category = ComponentCategory.CHARTS,
    description = "A component that allows visualizing data using lines")
@SimpleObject
public final class ChartLine extends ChartBase<LineChart, ChartDataLine> {

    /**
     * Creates a new Line Chart component.
     *
     * @param container container, component will be placed in
     */
    public ChartLine(ComponentContainer container) {
        super(container);

        view = new LineChart(container.$context());

        initChart();
    }

    @Override
    public View getView() {
        return view;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR,
            description = "")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT + ":com.google.appinventor.components.runtime.ChartDataLine")
    public void ChartData(ChartDataLine data) {
        this.data = data;
        view.setData(data.getChartData());
        view.invalidate();
    }
}
