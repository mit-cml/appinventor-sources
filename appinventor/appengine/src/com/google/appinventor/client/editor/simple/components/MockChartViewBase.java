package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.enums.Position;

public abstract class MockChartViewBase<C extends AbstractChart> {
    protected C chartWidget;

    protected void initializeDefaultSettings() {
        chartWidget.getOptions().setMaintainAspectRatio(false); // Fill panel
        chartWidget.getOptions().getTitle().setDisplay(true); // Display title
        chartWidget.getOptions().getLegend().getLabels().setBoxWidth(20); // Reduce label size
        chartWidget.getOptions().getLegend().setPosition(Position.BOTTOM); // Position legend at the bottom

        chartWidget.setWidth("100%"); // Fill root panel with Chart Widget's width
    }

    /**
     * Returns the underlying Chart widget object.
     *
     * @return  Chart widget object of this Chart View
     */
    public C getChartWidget() {
        return chartWidget;
    }

    public void setTitle(String text) {
        chartWidget.getOptions().getTitle().setText(text);
    }

    public void setBackgroundColor(String value) {
        if (MockComponentsUtil.isDefaultColor(value)) {
            value = "&HFFFFFFFF";  // white
        }
        MockComponentsUtil.setWidgetBackgroundColor(chartWidget, value);
    }
}
