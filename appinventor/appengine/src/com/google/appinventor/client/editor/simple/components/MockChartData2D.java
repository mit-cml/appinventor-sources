// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

import java.util.Arrays;

/**
 * Mock Chart Data component for 2D data.
 * Applies to Area, Bar, Line, Pie, Scatter charts.
 *
 * <p>In the future, this would also apply to Radar/Spider-web charts.
 */
public class MockChartData2D extends MockChartData {
  public static final String TYPE = "ChartData2D";

  public static final String PROPERTY_NAME_SOURCE = "Source";

  /**
   * Creates a new MockChartData2D component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockChartData2D(SimpleEditor editor) {
    super(editor, TYPE, images.chartData2D());

    // Initialize dataFileColumns to default values.
    // Since ChartData2D consists of 2 entries,
    // the List is initialized with 2 entries.
    dataFileColumns = Arrays.asList("", "");
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (PROPERTY_NAME_SOURCE.equals(propertyName)) {
      editor.refreshPropertiesPanel();
    }
  }
}
