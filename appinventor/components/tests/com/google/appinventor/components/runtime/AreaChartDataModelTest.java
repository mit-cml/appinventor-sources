// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineData;
import org.junit.Before;

/**
 * Unit tests for the AreaChartModel class.
 */
public class AreaChartDataModelTest
    extends AbstractPointChartDataModelTest<AreaChartDataModel, LineData> {
  @Before
  @Override
  public void setup() {
    data = new LineData();
    model = new AreaChartDataModel(data, new AreaChartView(new Chart(getForm())));
  }
}
