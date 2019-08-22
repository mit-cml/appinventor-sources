package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.data.Dataset;
import org.pepstock.charba.client.enums.Position;

import java.util.HashMap;

public abstract class MockChartView<C extends AbstractChart> {
    protected C chartWidget;

    /**
     * Sets the default (mainly style) settings of the Chart view.
     */
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

    /**
     * Changes the title of the Chart.
     * @param text  new Title
     */
    public void setTitle(String text) {
        chartWidget.getOptions().getTitle().setText(text);
    }

    /**
     * Changes the background color of the Chart.
     * @param value  new background color value (in hex)
     */
    public void setBackgroundColor(String value) {
        if (MockComponentsUtil.isDefaultColor(value)) {
            value = "&HFFFFFFFF";  // white
        }
        MockComponentsUtil.setWidgetBackgroundColor(chartWidget, value);
    }

    public void setLegendEnabled(boolean enabled) {
        chartWidget.getOptions().getLegend().setDisplay(enabled);
    }

    /**
     * Creates a new MockChartDataModel object instance
     * representative of the MockChartView type.
     * @return  new MockChartDataModel instance
     */
    public abstract MockChartDataModel createDataModel();
}
