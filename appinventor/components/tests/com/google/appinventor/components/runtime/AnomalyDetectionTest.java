// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static junit.framework.Assert.assertEquals;

import com.google.appinventor.components.runtime.util.YailList;
import gnu.lists.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AnomalyDetectionTest extends RobolectricTestBase {
  AnomalyDetection anomalyDetection = new AnomalyDetection(getForm());
  YailList xList = YailList.makeList(Arrays.asList("1","2","3","4","5","6"));
  YailList yList = YailList.makeList(Arrays.asList("1","2","3","2","2","88"));

  @Test
  public void testDetectAnomalies(){
    ArrayList anomalies = (ArrayList) anomalyDetection.DetectAnomalies( yList, 2);
    ArrayList expectedAnomalies = new ArrayList<Pair>();
    expectedAnomalies.add(Arrays.asList(6, 88f));
    assertEquals(expectedAnomalies.size(), anomalies.size());
    // Assert.assertEquals(expectedAnomalies, anomalies);
  }

  @Test
  public void testDetectMultipleAnomalies(){
    YailList yList = YailList.makeList(Arrays.asList("1","2","78","2","2","88"));
    ArrayList anomalies = (ArrayList) anomalyDetection.DetectAnomalies( yList, 1);
    ArrayList expectedAnomalies = new ArrayList<Pair>();
    expectedAnomalies.add(Arrays.asList(3, 78f));
    expectedAnomalies.add(Arrays.asList(6, 88f));
    assertEquals(expectedAnomalies.size(), anomalies.size());
  }

  @Test
  public void testGetAnomalyIndex(){
    double index = anomalyDetection.GetAnomalyIndex(YailList.makeList(Arrays.asList(6, 88f)));
    double expectedIndex = 6;
    assertEquals(expectedIndex, index, 0.01f);
  }

  @Test
  public void testCleanData(){
    List cleanData = anomalyDetection.CleanData(YailList.makeList(Arrays.asList(6, 88f)),xList, yList);
    YailList expectedList = YailList.makeList(
        Arrays.asList(
            YailList.makeList(
                Arrays.asList("1", "1")
            ),
            YailList.makeList(
                Arrays.asList("2", "2")
            ),
            YailList.makeList(
                Arrays.asList("3", "3")
            ),
            YailList.makeList(
                Arrays.asList("4", "2")
            ),
            YailList.makeList(
                Arrays.asList("5", "2")
            )
        )
    );
    assertEquals(expectedList.size(), cleanData.size());
  }
}
