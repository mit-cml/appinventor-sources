// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;
import gnu.lists.LList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import gnu.lists.Pair;


/**
 * A data science component to apply different anomaly detection models
 * The component needs a data source to apply the model on
 */
@DesignerComponent(version = YaVersion.ANOMALY_COMPONENT_VERSION,
    description = "A component that contains anomaly detection models",
    category = ComponentCategory.DATASCIENCE,
    iconName = "images/web.png",
    nonVisible = true)
@SimpleObject
@SuppressWarnings("checkstyle:JavadocParagraph")
public final class AnomalyDetection extends DataCollection {
  /**
   * Creates a new Anomaly Detection component.
   */
  public AnomalyDetection(ComponentContainer container) {
    super();
    // Construct default dataFileColumns list with 2 entries
    dataFileColumns = Arrays.asList("", "");
    sheetsColumns = Arrays.asList("", "");
    webColumns = Arrays.asList("", ""); // Construct default webColumns list with 2 entries
  }

  /**
   * Calculates the mean and standard deviation of the data, and then checks each data point's Z-score against the threshold.
   * If a data point's Z-score is greater than the threshold, the data point is labeled as anomaly
   *
   * @param dataList - the data array represents the data you want to check for anomalies
   * @return List of detected anomaly data points
   */
  @SimpleFunction(description = "Simple anomaly detection.")
  public List DetectAnomalies(final YailList dataList) {
    return ComputeDetectAnomalies(dataList);
  }

  @SimpleFunction(description = "Simple anomaly detection with user defined threshold.")
  public List DetectAnomaliesWithThreshold(final YailList dataList, double threshold) {
    return ComputeDetectAnomalies(dataList, threshold);
  }

  public List ComputeDetectAnomalies(final YailList dataList, double... threshold) {
    ArrayList anomalies = new ArrayList<Pair>();

    LList dataListValues = (LList) dataList.getCdr();
    List<Double> data = castToDouble(dataListValues);

    // Calculate mean and standard deviation
    double sum = 0;
    for (int i = 0; i < data.size(); i++) {
      sum += data.get(i);
    }
    double mean = sum / data.size();
    // The variance
    double variance = 0;
    for (int i = 0; i < data.size(); i++) {
      variance += Math.pow(data.get(i) - mean, 2);
    }
    variance /= data.size();

    double sd = Math.sqrt(variance);

    // Detect anomalies using Z-score
    for (int i = 0; i < data.size(); i++) {
      double zScore = Math.abs((data.get(i) - mean) / sd); // The z-score is a measure of how many standard deviations a data point is away from the mean
      if (threshold.length == 0) {
        double defaultThreshold = 1; // Todo: define a default value if not defined by the user
        if (zScore > defaultThreshold) {
          anomalies.add(Arrays.asList(i + 1,data.get(i))); // We need to return the index in order to remove the x value at the same index when we remove anomalies (index starts at 0)
        }
      } else {
        if (zScore > threshold[0]) {
          anomalies.add(Arrays.asList(i + 1, data.get(i)));
        }
      }
    }
    return anomalies;
  }

  /**
   * Given a single anomaly: [(anomaly index, anomaly value)]
   *
   * 1. Iterate over the xList and delete value at anomaly index
   * 2. Iterate over the yList and delete the value at anomaly index with the same value as anomaly value
   * 3. combine the xList and yList after modification in a list of x and y pairs
   *
   * We assume x and y lists are the same size and are ordered
   *
   * @param anomaly - a single YailList tuple of anomaly index and value
   * @return List of combined x and y pairs without the anomaly pair
   */
  @SimpleFunction(description = "test")
  public List RemoveAnomaly(final YailList anomaly, YailList xList, YailList yList) {
    LList xValues = (LList) xList.getCdr();
    List xData = castToDouble(xValues);

    LList yValues = (LList) yList.getCdr();
    List yData = castToDouble(yValues);

    if (xData.size() != yData.size())
      throw new IllegalStateException("Must have equal X and Y data points");

    int index = (int) GetAnomalyIndex(anomaly);

    xData.remove(index - 1);
    yData.remove(index - 1);

    ArrayList cleanData = new ArrayList<Pair>();

    if (xData.size() == yData.size()) {
      for (int i = 0; i < xData.size(); i++) {
        cleanData.add(Arrays.asList(xData.get(i),yData.get(i)));
      }
    }
    return cleanData;
  }

  @SimpleFunction(description = "Gets the index of a single anomaly")
  public double GetAnomalyIndex(YailList anomaly){
    LList anomalyValue = (LList) anomaly.getCdr();
    List<Double> anomalyNr = castToDouble(anomalyValue);
    return anomalyNr.get(0);
  }
  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return null;
  }

  @Override
  public void refreshChart() {

  }
}
