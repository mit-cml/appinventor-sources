package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.AbstractChart;
import org.pepstock.charba.client.BarChart;
import org.pepstock.charba.client.configuration.Axis;
import org.pepstock.charba.client.configuration.CartesianLinearAxis;
import org.pepstock.charba.client.configuration.LineOptions;
import org.pepstock.charba.client.enums.Position;

public abstract class MockAxisChartView<C extends AbstractChart> extends MockChartView<C> {
  protected CartesianLinearAxis xAxis;
  protected CartesianLinearAxis yAxis;

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    xAxis = new CartesianLinearAxis(chartWidget);
    yAxis = new CartesianLinearAxis(chartWidget);
  }

  public void setGridEnabled(boolean enabled) {
    xAxis.getGrideLines().setDisplay(enabled);
    yAxis.getGrideLines().setDisplay(enabled);
  }
}
