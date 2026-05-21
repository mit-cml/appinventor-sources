// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineData;

import com.github.mikephil.charting.data.LineDataSet;
import java.util.List;

/**
 * Handles the data operations & model-specific styling for Area
 * Chart data for the Chart component.
 * @see com.google.appinventor.components.runtime.ChartDataModel
 */
public class AreaChartDataModel extends LineChartBaseDataModel<AreaChartView> {
  /**
   * Initializes a new AreaChartDataModel object instance.
   *
   * @param data Line Chart Data object instance
   * @param view Area Chart View to link model tow
   */
  public AreaChartDataModel(LineData data, AreaChartView view) {
    super(data, view);
  }

  @Override
  public void setColor(int argb) {
    super.setColor(argb);
    if (dataset instanceof LineDataSet) {
      ((LineDataSet) dataset).setFillColor(argb); // Change fill color
    }
  }

  @Override
  public void setColors(List<Integer> colors) {
    super.setColors(colors);

    // If the colors List is non-empty, use the first color
    // as the fill color.
    if (!colors.isEmpty()) {
      if (dataset instanceof LineDataSet) {
        ((LineDataSet) dataset).setFillColor(colors.get(0));
      }
    }
  }

  @Override
  protected void setDefaultStylingProperties() {
    super.setDefaultStylingProperties();
    dataset.setDrawFilled(true); // Enable fill underneath the lines
    if (dataset instanceof LineDataSet) {
      // Set fill color to be transparent (value of 100 out of 255)
      ((LineDataSet) dataset).setFillAlpha(100);
    }
  }
}
