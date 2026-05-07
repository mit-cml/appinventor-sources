// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.LineType;

import java.util.Comparator;

import org.pepstock.charba.client.data.DataPoint;
import org.pepstock.charba.client.enums.SteppedLine;

/**
 * Chart Data Model for Mock Line Chart based views.
 */
public abstract class MockLineChartBaseDataModel<V extends MockLineChartViewBase<V>>
    extends MockPointChartDataModel<V> {
  /**
   * Creates a new MockLineChartBaseDataModel instance.
   *
   * @param view Chart View to link the model to.
   */
  public MockLineChartBaseDataModel(V view) {
    super(view);
  }

  @Override
  protected void setDefaultStylingProperties() {
    dataSeries.setBorderWidth(1);
    dataSeries.setLineTension(0);
    dataSeries.setShowLine(true);
  }

  @Override
  protected void postDataImportAction() {
    // No data points generated, fallback to default option.
    if (dataSeries.getDataPoints().isEmpty()) {
      setDefaultElements();
    } else {
      // Since we are dealing with an underlying Scatter Data Series
      // from the Charba library (which makes representing Line Charts
      // more conveniently than using the Line Chart Data Series), sorting
      // is a must, because otherwise, the Chart will not look representative.
      // Consider adding: (1, 2), (5, 3), (2, 5). We want the x = 2
      // value to be continuous on the Line Chart, rather than
      // going outside the Chart, which would happen since we
      // are using a Scatter Chart.
      dataSeries.getDataPoints().sort(Comparator.comparingDouble(DataPoint::getX));
    }
  }

  /**
   * Changes the Line type of the Data Series.
   *
   * <p>The following values are used:
   * 0 - Linear
   * 1 - Curved
   * 2 - Stepped
   *
   * @param type new Line type value (integer)
   */
  public void setLineType(LineType type) {
    switch (type) {
      case Linear:
        dataSeries.setSteppedLine(SteppedLine.FALSE); // Disable stepped line
        dataSeries.setLineTension(0); // Disable curved line
        break;

      case Curved:
        dataSeries.setSteppedLine(SteppedLine.FALSE); // Disable stepped line
        dataSeries.setLineTension(0.5); // Set 50% Line Tension (enable curve)
        break;

      case Stepped:
        dataSeries.setSteppedLine(SteppedLine.BEFORE); // Enable stepped line
        dataSeries.setLineTension(0); // Disable curved line
        break;

      default:
        throw new IllegalArgumentException("Unknown line type: " + type);
    }
  }
}
