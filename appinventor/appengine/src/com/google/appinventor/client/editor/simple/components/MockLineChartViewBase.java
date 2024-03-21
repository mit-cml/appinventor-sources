// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Chart View base class for Line Chart based Charts.
 */
public abstract class MockLineChartViewBase<V extends MockLineChartViewBase<V>>
    extends MockPointChartView<V> {
  /**
   * Creates a new MockLineChartViewBase object instance.
   */
  protected MockLineChartViewBase() {
    super();
  }
}
