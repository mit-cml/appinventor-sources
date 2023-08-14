// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static junit.framework.Assert.assertEquals;

import com.google.appinventor.components.common.LOBFValues;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Test;
import org.robolectric.android.util.concurrent.RoboExecutorService;

import java.util.Arrays;
import java.util.List;


public class RegressionTest extends RobolectricTestBase {
  Regression regression;
  LOBFValues value;
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
  public void testCalculateLineOfBestFitWrongInputSize() throws Exception{
    YailList xListWrongSize = YailList.makeList(Arrays.asList("1", "2", "3", "4", "5"));
    regression.CalculateLineOfBestFitValue(xListWrongSize, yList, value.Slope);
  }

  @Test
  public void testCalculateLineOfBestFitValueSlope(){
    Object slope = regression.CalculateLineOfBestFitValue(xList, yList, value.Slope);
    double expectedSlope = 12.4;
    assertEquals(expectedSlope, (double) slope,0.01f);
  }

  @Test
  public void testCalculateLineOfBestFitValueCorrCoef(){
    Object corrCoef = regression.CalculateLineOfBestFitValue(xList, yList, value.CorrCoef);
    double expectedCorrCoef = 0.66;
    assertEquals(expectedCorrCoef, (double) corrCoef,0.01f);
  }

  @Test
  public void testCalculateLineOfBestFitValueYintercept(){
    Object yIntercept = regression.CalculateLineOfBestFitValue(xList, yList, value.Yintercept);
    double expectedYintercept = -27.07;
    assertEquals(expectedYintercept, (double) yIntercept,0.01f);
  }

  @Test
  public void testCalculateLineOfBestFitPredictions(){
    List predictions = (List) regression.CalculateLineOfBestFitValue(xList, yList, value.Predictions);
    List expectedPredictions = Arrays.asList(-14.6667, -2.2667, 10.1333, 22.5333, 34.9333, 47.3333);
    for (int i = 0; i < expectedPredictions.size(); i++) {
      assertEquals((double) expectedPredictions.get(i), (double) predictions.get(i),0.01f);
    }
  }

}
