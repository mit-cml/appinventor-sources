// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static junit.framework.Assert.assertEquals;

import com.google.appinventor.components.common.LOBFValues;
import com.google.appinventor.components.runtime.util.YailList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class RegressionTest extends RobolectricTestBase {
  Regression regression;
  YailList xList;
  YailList yList;

  @Override
  public void setUp() {
    super.setUp();
    regression = new Regression(getForm());
    xList = YailList.makeList(Arrays.asList("1", "2", "3","4", "5", "6"));
    yList = YailList.makeList(Arrays.asList("1", "2", "3", "2", "2", "88"));
  }

  @Test(expected = Exception.class)
  public void testCalculateLineOfBestFitWrongInputSize() {
    YailList xListWrongSize = YailList.makeList(Arrays.asList("1", "2", "3", "4", "5"));
    regression.CalculateLineOfBestFitValue(xListWrongSize, yList,
        LOBFValues.Slope.toUnderlyingValue());
  }

  @Test
  public void testCalculateLineOfBestFitValueSlope() {
    Object slope = regression.CalculateLineOfBestFitValue(xList, yList,
        LOBFValues.Slope.toUnderlyingValue());
    double expectedSlope = 12.4;
    assertEquals(expectedSlope, (double) slope,0.01f);
  }

  @Test
  public void testCalculateLineOfBestFitValueCorrCoef() {
    Object corrCoef = regression.CalculateLineOfBestFitValue(xList, yList,
        LOBFValues.CorrCoef.toUnderlyingValue());
    double expectedCorrCoef = 0.66;
    assertEquals(expectedCorrCoef, (double) corrCoef,0.01f);
  }

  @Test
  public void testCalculateLineOfBestFitValueYintercept() {
    Object yIntercept = regression.CalculateLineOfBestFitValue(xList, yList,
        LOBFValues.Yintercept.toUnderlyingValue());
    double expectedYintercept = -27.07;
    assertEquals(expectedYintercept, (double) yIntercept,0.01f);
  }

  @Test
  public void testCalculateLineOfBestFitPredictions() {
    List<?> predictions = (List<?>) regression.CalculateLineOfBestFitValue(xList, yList,
        LOBFValues.Predictions.toUnderlyingValue());
    List<Double> expectedPredictions = Arrays.asList(
        -14.6667, -2.2667, 10.1333, 22.5333, 34.9333, 47.3333);
    for (int i = 0; i < expectedPredictions.size(); i++) {
      assertEquals(expectedPredictions.get(i), (Double) predictions.get(i),0.01f);
    }
  }

}
