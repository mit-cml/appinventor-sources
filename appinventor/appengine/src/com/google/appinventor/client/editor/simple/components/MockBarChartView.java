package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.output.OdeLog;
import org.pepstock.charba.client.BarChart;
import org.pepstock.charba.client.configuration.CartesianLinearAxis;
import org.pepstock.charba.client.data.Dataset;

public class MockBarChartView extends MockChartView<BarChart> {
  public MockBarChartView() {
    chartWidget = new BarChart();
    initializeDefaultSettings();
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    CartesianLinearAxis axis = new CartesianLinearAxis(chartWidget);
    axis.getTicks().setSuggestedMin(0);

    chartWidget.getOptions().getScales().setYAxes(axis);
  }

  @Override
  public MockChartDataModel createDataModel() {
    return new MockBarChartDataModel(chartWidget.getData(), this);
  }

  public void updateLabels() {
    chartWidget.update();

    int labelCount = 0;

    for (Dataset dataset : chartWidget.getData().getDatasets()) {
      labelCount = Math.max(dataset.getData().size(), labelCount);
    }

    String[] labels = new String[labelCount];

    for (int i = 0; i < labelCount; ++i) {
      labels[i] = i + "";
    }

    chartWidget.getData().setLabels(labels);
  }
}
