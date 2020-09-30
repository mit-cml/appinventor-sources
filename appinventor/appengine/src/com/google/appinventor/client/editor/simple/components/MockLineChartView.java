// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Chart View for the Line Chart
 * <p>
 * Responsible for the GUI of the Line Chart.
 */
public class MockLineChartView extends MockLineChartViewBase {
  /**
   * Creates a new MockLineChartView object instance.
   */
  public MockLineChartView() {
    super();
  }

  @Override
  public MockChartDataModel createDataModel() {
    return new MockLineChartDataModel(this);
  }
}
