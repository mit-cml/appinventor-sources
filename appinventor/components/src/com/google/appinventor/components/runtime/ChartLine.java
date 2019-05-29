package com.google.appinventor.components.runtime;

import android.view.View;
import com.github.mikephil.charting.charts.LineChart;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;

@DesignerComponent(version = 1,
    category = ComponentCategory.CHARTS,
    description = "A component that allows visualizing data using lines")
@SimpleObject
public final class ChartLine extends ChartBase<LineChart> {

    /**
     * Creates a new Line Chart component.
     *
     * @param container container, component will be placed in
     */
    public ChartLine(ComponentContainer container) {
        super(container);
    }

    @Override
    public View getView() {
        return view;
    }
}
