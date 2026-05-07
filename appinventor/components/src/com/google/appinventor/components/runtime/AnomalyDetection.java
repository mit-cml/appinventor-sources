// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
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

/**
 * A data science component to apply different anomaly detection models.
 * The component only needs a data source to apply the model on.
 *
 * The anomaly detection models only return a list of anomalies.
 * ChartData2D component is needed to highlight the anomalies on a chart
 */
@DesignerComponent(version = YaVersion.ANOMALY_COMPONENT_VERSION,
    description = "A component that contains anomaly detection models",
    category = ComponentCategory.DATASCIENCE,
    iconName = "images/anomaly.png",
    nonVisible = true)
@SimpleObject
@SuppressWarnings("checkstyle:JavadocParagraph")
public final class AnomalyDetection extends DataCollection<ComponentContainer, DataModel<?>> {
  /**
   * Creates a new Anomaly Detection component.
   */
  public AnomalyDetection(ComponentContainer container) {
    super(container);
  }

  /**
   * Calculates the mean and standard deviation of the data, and then checks each data point's
   * Z-score against the threshold. If a data point's Z-score is greater than the threshold,
   * the data point is labeled as anomaly.
   *
   * @param dataList - the data array represents the data you want to check for anomalies
   * @return List of detected anomaly data points
   */
  @SimpleFunction(description = "Z-Score Anomaly Detection: checks each data point's Z-score"
      + "against the given threshold if a data point's Z-score is greater than the threshold, the "
      + "data point is labeled as anomaly and returned in a list of pairs (anomaly index, anomaly "
      + "value)")
  public List<List<?>> DetectAnomalies(final YailList dataList, double threshold) {
    List<List<?>> anomalies = new ArrayList<>();

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
      // The z-score is a measure of how many standard deviations a data point is away from the mean
      double zScore = Math.abs((data.get(i) - mean) / sd);
      if (zScore > threshold) {
        anomalies.add(Arrays.asList(i + 1, data.get(i)));
      }
    }
    return anomalies;
  }

  /**
   * Detects anomalies in the given chart data object by comparing Y values to the threshold based
   * on their standard deviation to the mean.
   *
   * @param chartData the ChartData2D object containing the data
   * @param threshold the threshold for the z-score to indicate an anomaly
   * @return a list of anomalies in the form of a list of pairs (x-coord, y-coord)
   */
  @SimpleFunction
  public List<List<?>> DetectAnomaliesInChartData(final ChartData2D chartData, double threshold) {
    List<Entry> entries = (List<Entry>) chartData.getDataValue(null);
    double sum = 0;
    for (Entry entry : entries) {
      sum += entry.getY();
    }
    double mean = sum / entries.size();
    double variance = 0;
    for (Entry entry : entries) {
      variance += Math.pow(entry.getY() - mean, 2);
    }
    variance /= entries.size();
    double sd = Math.sqrt(variance);
    List<List<?>> anomalies = new ArrayList<>();
    for (Entry entry : entries) {
      double zScore = Math.abs((entry.getY() - mean) / sd);
      if (zScore > threshold) {
        anomalies.add(Arrays.asList(entry.getX(), entry.getY()));
      }
    }
    return anomalies;
  }

  /**
   * Given a single anomaly: [(anomaly index, anomaly value)]
   *
   * 1. Iterate over the xList and delete value at anomaly index
   * 2. Iterate over the yList and delete the value at anomaly index with the same value as anomaly
   *    value
   * 3. combine the xList and yList after modification in a list of x and y pairs
   *
   * We assume x and y lists are the same size and are ordered.
   *
   * @param anomaly - a single YailList tuple of anomaly index and value
   * @return List of combined x and y pairs without the anomaly pair
   */
  @SimpleFunction(description = "Given a single anomaly and the x and y values of your data."
      + " This block will return the x and y value pairs of your data without the anomaly")
  public List<List<?>> CleanData(final YailList anomaly, YailList xList, YailList yList) {
    LList xValues = (LList) xList.getCdr();
    List<Double> xData = castToDouble(xValues);

    LList yValues = (LList) yList.getCdr();
    List<Double> yData = castToDouble(yValues);

    if (xData.size() != yData.size()) {
      throw new IllegalStateException("Must have equal X and Y data points");
    }
    if (xData.size() == 0 || yData.size() == 0) {
      throw new IllegalStateException("List must have at least one element");
    }
    int index = (int) getAnomalyIndex(anomaly);

    xData.remove(index - 1);
    yData.remove(index - 1);

    List<List<?>> cleanData = new ArrayList<>();

    if (xData.size() == yData.size()) {
      for (int i = 0; i < xData.size(); i++) {
        cleanData.add(YailList.makeList(Arrays.asList(xData.get(i),yData.get(i))));
      }
    }
    return cleanData;
  }

  /**
   * Given a single anomaly: [(anomaly index, anomaly value)] return the anomaly index.
   *
   *
   * @param anomaly - a single YailList tuple of anomaly index and value
   * @return double anomaly index
   */
  public static double getAnomalyIndex(YailList anomaly) {
    if (!anomaly.isEmpty()) {
      LList anomalyValue = (LList) anomaly.getCdr();
      List<Double> anomalyNr = castToDouble(anomalyValue);
      return anomalyNr.get(0);
    } else {
      throw new IllegalStateException("Must have equal X and Y data points");
    }
  }

  // MARK: Properties and methods not currently needed

  @Override
  public void ElementsFromPairs(String elements) {
  }

  @Override
  public void SpreadsheetUseHeaders(boolean useHeaders) {
  }

  @Override
  public void DataFileXColumn(String column) {
  }

  @Override
  public void WebXColumn(String column) {
  }

  @Override
  public void SpreadsheetXColumn(String column) {
  }

  @Override
  public void DataFileYColumn(String column) {
  }

  @Override
  public void WebYColumn(String column) {
  }

  @Override
  public void SpreadsheetYColumn(String column) {
  }

  @Override
  public void DataSourceKey(String key) {
  }

  @Override
  public <K, V> void Source(DataSource<K, V> dataSource) {
  }

  @Override
  public void ImportFromList(YailList list) {
  }

  @Override
  public void Clear() {
  }

  @Override
  public <K, V> void ChangeDataSource(DataSource<K, V> source, String keyValue) {
  }

  @Override
  public void RemoveDataSource() {
  }

  @Override
  public YailList GetEntriesWithXValue(String x) {
    return YailList.makeEmptyList();
  }

  @Override
  public YailList GetEntriesWithYValue(String y) {
    return YailList.makeEmptyList();
  }

  @Override
  public YailList GetAllEntries() {
    return YailList.makeEmptyList();
  }

  @Override
  public void ImportFromTinyDB(TinyDB tinyDB, String tag) {
  }

  @Override
  public void ImportFromCloudDB(CloudDB cloudDB, String tag) {
  }

  @Override
  public void ImportFromDataFile(DataFile dataFile, String xValueColumn,
      String yValueColumn) {
  }

  @Override
  public void ImportFromSpreadsheet(Spreadsheet spreadsheet, String xColumn, String yColumn,
      boolean useHeaders) {
  }

  @Override
  public void ImportFromWeb(Web web, String xValueColumn, String yValueColumn) {
  }

  @Override
  public void onDataChange() {

  }
}
