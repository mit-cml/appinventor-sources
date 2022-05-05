// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Chart Data Model for the Mock Line Chart view.
 *
 * <p>Responsible for handling data operations on the Data
 * of the Line Chart.
 */
public class MockLineChartDataModel extends MockLineChartBaseDataModel<MockLineChartView> {

  /**
   * Creates a new MockLineChartDataModel instance.
   *
   * @param view  Chart View to link model to.
   */
  public MockLineChartDataModel(MockLineChartView view) {
    super(view);
  }
}
