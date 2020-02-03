// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.BarChart;
import org.pepstock.charba.client.configuration.Axis;
import org.pepstock.charba.client.configuration.CartesianLinearAxis;
import org.pepstock.charba.client.configuration.LineOptions;
import org.pepstock.charba.client.enums.Position;

/**
 * Base class for Chart view classes that have an axis.
 * @param <C>  Parameter of the Chart class to use on class extensions
 */
public abstract class MockAxisChartView<C extends AbstractChart> extends MockChartView<C> {
  protected CartesianLinearAxis xAxis;
  protected CartesianLinearAxis yAxis;

  protected String[] labels = new String[0];

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // Construct an x and a y axis to be able to modify properties
    // of the two Axis.
    xAxis = new CartesianLinearAxis(chartWidget);
    yAxis = new CartesianLinearAxis(chartWidget);

    // Set fixed step size of the x and y axis to 1.
    xAxis.getTicks().setStepSize(1);
    yAxis.getTicks().setStepSize(1);
  }

  /**
   * Enables or disables the Chart grid.
   *
   * @param enabled Indicates whether the grid should be enabled.
   */
  public void setGridEnabled(boolean enabled) {
    xAxis.getGrideLines().setDisplay(enabled);
    yAxis.getGrideLines().setDisplay(enabled);
  }

  /**
   * Updates the custom X axis Labels of the Chart View.
   *
   * @param labels New Array of custom X Axis labels
   */
  public void updateLabels(String[] labels) {
    this.labels = labels;
  }
}
