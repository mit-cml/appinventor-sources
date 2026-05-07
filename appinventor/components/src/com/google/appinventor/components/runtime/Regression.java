// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.LOBFValues;
import com.google.appinventor.components.common.LinearRegression;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;
import gnu.lists.LList;

import java.util.List;

/**
 * A data science component to apply different regression models.
 * The component only requires a data source to apply the model on.
 *
 *   The component is only responsible for the statistical calculations and
 * provides the following properties for line of best fit:
 * "slope", "Yintercept", "correlation coefficient", and "predictions"
 *
 *   To draw the line of best fit use the drawing block in ChartData2D component
 */
@DesignerComponent(version = YaVersion.REGRESSION_COMPONENT_VERSION,
    description = "A component that contains regression models",
    category = ComponentCategory.DATASCIENCE,
    iconName = "images/regression.png",
    nonVisible = true)
@SimpleObject
@SuppressWarnings({"checkstyle:JavadocParagraph", "JavadocBlankLines"})
public final class Regression extends DataCollection<ComponentContainer, DataModel<?>> {
  private static final LinearRegression LINEAR_REGRESSION = new LinearRegression();

  /**
   * Creates a new Regression component.
   */
  public Regression(ComponentContainer container) {
    super(container);
  }

  /**
   * Calculates the line of best fit.
   *
   * @param x - the list of x values
   * @param y - the list of y values
   * @return list. 1st element of the list is the slope, 2nd element is the intercept, 3rd
   *     correlation coefficient, 4th element is the line of best fit prediction values
   */
  public static YailDictionary computeLineOfBestFit(List<Double> x, List<Double> y) {
    return new YailDictionary(LINEAR_REGRESSION.compute(x, y));
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
    YailDictionary result = computeLineOfBestFit(castToDouble((LList) xList.getCdr()),
        castToDouble((LList) yList.getCdr()));
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


