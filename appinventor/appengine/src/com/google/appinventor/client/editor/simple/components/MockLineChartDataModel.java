// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;

/**
 * Chart Data Model for the Mock Line Chart view.
 * <p>
 * Responsible for handling data operations on the Data
 * of the Line Chart.
 */
public class MockLineChartDataModel extends MockLineChartBaseDataModel {

  /**
   * Creates a new MockLineChartDataModel instance.
   *
   * @param chartData Data object of the Chart View.
   */
  public MockLineChartDataModel(Data chartData) {
    super(chartData);
  }
}