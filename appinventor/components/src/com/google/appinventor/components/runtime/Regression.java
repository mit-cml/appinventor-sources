// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.LOBFValues;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;
import gnu.lists.LList;

import java.util.ArrayList;
import java.util.List;

/**
 * A data science component to apply different regression models.
 * The component only requires a data source to apply the model on.
 *
 * The component is only responsible for the statistical calculations and
 * provides the following properties for line of best fit:
 * "slope", "Yintercept", "correlation coefficient", and "predictions"
 *
 * To draw the line of best fit use the drawing block in ChartData2D component
 */
@DesignerComponent(version = YaVersion.REGRESSION_COMPONENT_VERSION,
    description = "A component that contains regression models",
    category = ComponentCategory.DATASCIENCE,
    iconName = "images/regression.png",
    nonVisible = true)
@SimpleObject
@SuppressWarnings("checkstyle:JavadocParagraph")
public final class Regression extends DataCollection<ComponentContainer, DataModel<?>> {

  /**
   * Creates a new Regression component.
   */
  public Regression(ComponentContainer container) {
    super(container);
  }

  /**
   * Calculates the line of best fit.
   *
   * @param xEntries - the list of x values
   * @param yEntries - the list of y values
   * @return list. 1st element of the list is the slope, 2nd element is the intercept, 3rd
   *     correlation coefficient, 4th element is the line of best fit prediction values
   */
  public static YailDictionary computeLineOfBestFit(final YailList xEntries,
      final YailList yEntries) {
    LList xValues = (LList) xEntries.getCdr();
    List<Double> x = castToDouble(xValues);

    LList yValues = (LList) yEntries.getCdr();
    List<Double> y = castToDouble(yValues);

    if (xValues.size() != yValues.size()) {
      throw new IllegalStateException("Must have equal X and Y data points");
    }
    if (xValues.isEmpty() || xValues.isEmpty()) {
      throw new IllegalStateException("List must have at least one element");
    }
    int n = xValues.size();

    double sumx = 0.0;
    double sumy = 0.0;
    double sumXY = 0.0;
    double squareSumX = 0.0;
    double squareSumY = 0.0;
    for (int i = 0; i < n; i++) {
      sumx += x.get(i);
      sumXY = sumXY + x.get(i) * y.get(i);
      sumy += y.get(i);
      squareSumX = squareSumX + x.get(i) * x.get(i);
      squareSumY = squareSumY + y.get(i) * y.get(i);
    }
    double xmean = sumx / n;
    double ymean = sumy / n;

    double xxmean = 0.0;
    double xymean = 0.0;
    List<Double> predictions = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      xxmean += (x.get(i) - xmean) * (x.get(i) - xmean);
      xymean += (x.get(i) - xmean) * (y.get(i) - ymean);
    }
    double slope = xymean / xxmean;
    double intercept = ymean - slope * xmean;

    for (int i = 0; i < n; i++) {
      double prediction = slope * x.get(i) + intercept;
      predictions.add(prediction);
    }

    // use formula for calculating correlation coefficient.
    double corr = (n * sumXY - sumx * sumy)
        / (Math.sqrt((n * squareSumX - sumx * sumx) * (n * squareSumY - sumy * sumy)));

    YailDictionary resultDic = new YailDictionary();
    resultDic.put("slope",slope);
    resultDic.put("Yintercept", intercept);
    resultDic.put("correlation coefficient", corr);
    resultDic.put("predictions", predictions);

    return resultDic;
  }

  /**
   * Returns one of the Line of Best Fit values.
   * A value could be "slope", "Yintercept", "correlation coefficient", "predictions" or a
   * dictionary when AllValues is specified.
   *
   * @param xList - the list of x values
   * @param yList - the list of y values
   * @param value - the string name of the line of best fit property
   * @return Double slope
   */
  @SimpleFunction(description = "Returns one of the Line of Best Fit values. A value could be"
      + "\"slope\", \"Yintercept\", \"correlation coefficient\"or \"predictions\". The block "
      + "returns the complete dictionary with all values if no specific value string is provided")
  public Object CalculateLineOfBestFitValue(final YailList xList, final YailList yList,
      @Options(LOBFValues.class) String value) {
    YailDictionary result = computeLineOfBestFit(xList, yList);
    if (result.containsKey(value)) {
      return result.get(value);
    } else {
      return result;
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


