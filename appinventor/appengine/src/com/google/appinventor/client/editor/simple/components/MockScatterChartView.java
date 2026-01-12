// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.ScatterDataset;

/**
 * Chart View for the Scatter Chart.
 * Handles the UI of the Scatter Chart for the Mock Chart component.
 */
public class MockScatterChartView extends MockPointChartView<MockScatterChartView> {
  /**
   * Creates a new Mock Scatter Chart View object instance.
   */
  public MockScatterChartView() {
    super();
  }

  @Override
  public MockChartDataModel<ScatterDataset, MockScatterChartView> createDataModel() {
    return new MockScatterChartDataModel(this);
  }
}
