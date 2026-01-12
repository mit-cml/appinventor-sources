// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.data.Dataset;
import org.pepstock.charba.client.enums.Position;

/**
 * Base class for handling individual Chart Views.
 * Since the Mock Chart is an individual component, this
 * class serves the purpose of abstracting away the Chart Widget
 * for each Chart type individually (e.g. Line, Bar Chart)
 *
 * @param <C>  Parameter representing the Chart of the Chart View (Charba Chart)
 */
public abstract class MockChartView
    <D extends Dataset, C extends AbstractChart<D>, V extends MockChartView<D, C, V>> {
  protected C chartWidget;

  /**
   * Sets the default (mainly style) settings of the Chart view.
   */
  protected void initializeDefaultSettings() {
    chartWidget.getOptions().setMaintainAspectRatio(false); // Fill panel
    chartWidget.getOptions().getTitle().setDisplay(true); // Display title
    chartWidget.getOptions().getLegend().getLabels().setBoxWidth(20); // Reduce label size
    chartWidget.getOptions().getLegend().setPosition(Position.BOTTOM); // Position legend at bottom

    chartWidget.setWidth("100%"); // Fill root panel with Chart Widget's width
  }

  /**
   * Returns the underlying Chart widget object.
   *
   * @return Chart widget object of this Chart View
   */
  public C getChartWidget() {
    return chartWidget;
  }

  /**
   * Changes the title of the Chart.
   *
   * @param text new Title
   */
  public void setTitle(String text) {
    chartWidget.getOptions().getTitle().setText(text);
  }

  /**
   * Changes the background color of the Chart.
   *
   * @param value new background color value (in hex)
   */
  public void setBackgroundColor(String value) {
    if (MockComponentsUtil.isDefaultColor(value)) {
      value = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(chartWidget, value);
  }

  /**
   * Changes the visibility of the Legend.
   *
   * @param enabled indicatees whether to enable the Legend.
   */
  public void setLegendEnabled(boolean enabled) {
    chartWidget.getOptions().getLegend().setDisplay(enabled);
  }

  /**
   * Creates a new MockChartDataModel object instance
   * representative of the MockChartView type.
   *
   * @return new MockChartDataModel instance
   */
  public abstract MockChartDataModel<D, V>
      createDataModel();
}
