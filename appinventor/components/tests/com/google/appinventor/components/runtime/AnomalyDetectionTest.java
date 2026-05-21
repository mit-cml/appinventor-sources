// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static junit.framework.Assert.assertEquals;

import com.google.appinventor.components.runtime.util.YailList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class AnomalyDetectionTest extends RobolectricTestBase {
  AnomalyDetection anomalyDetection;
  YailList xList;
  YailList yList;

  @Override
  public void setUp() {
    super.setUp();
    anomalyDetection = new AnomalyDetection(getForm());
    xList = YailList.makeList(Arrays.asList("1", "2", "3", "4", "5", "6"));
    yList = YailList.makeList(Arrays.asList("1", "2", "3", "2", "2", "88"));
  }

  @Test
  public void testDetectAnomalies() {
    List<List<?>> anomalies = anomalyDetection.DetectAnomalies(yList, 2);
    List<List<?>> expectedAnomalies = new ArrayList<>();
    expectedAnomalies.add(Arrays.asList(6, 88.0));
    assertEquals(expectedAnomalies.size(), anomalies.size());
    assertEquals(expectedAnomalies, anomalies);
  }

  @Test
  public void testDetectMultipleAnomalies() {
    YailList yList = YailList.makeList(Arrays.asList("1", "2", "78", "2", "2", "88"));
    List<List<?>> anomalies = anomalyDetection.DetectAnomalies(yList, 1);
    List<List<?>> expectedAnomalies = new ArrayList<>();
    expectedAnomalies.add(Arrays.asList(3, 78.0));
    expectedAnomalies.add(Arrays.asList(6, 88.0));
    assertEquals(expectedAnomalies.size(), anomalies.size());
    assertEquals(expectedAnomalies, anomalies);
  }

  @Test
  public void testGetAnomalyIndex() {
    double index = AnomalyDetection.getAnomalyIndex(YailList.makeList(Arrays.asList(6, 88f)));
    double expectedIndex = 6;
    assertEquals(expectedIndex, index, 0.01f);
  }

  @Test
  public void testCleanData() {
    List<?> cleanData = anomalyDetection.CleanData(
        YailList.makeList(Arrays.asList(6, 88.0)), xList, yList);
    List<?> expectedList = Arrays.asList(
        YailList.makeList(Arrays.asList(1.0, 1.0)),
        YailList.makeList(Arrays.asList(2.0, 2.0)),
        YailList.makeList(Arrays.asList(3.0, 3.0)),
        YailList.makeList(Arrays.asList(4.0, 2.0)),
        YailList.makeList(Arrays.asList(5.0, 2.0))
    );
    assertEquals(expectedList.size(), cleanData.size());
    assertEquals(expectedList, cleanData);
  }
}
