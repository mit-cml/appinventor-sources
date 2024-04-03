// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * The ChartComponent interface provides methods common to all chart components. Any component
 * intended to be added to a {@link Chart} MUST implement this interface.
 */
public interface ChartComponent extends Component {
  /**
   * Initialize the component's chart data object.
   */
  void initChartData();
}
