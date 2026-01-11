// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;
import gnu.lists.LList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A ChartData2D component represents a single two-dimensional Data Series in the Chart component,
 * for example, a single Line in the case of a Line Chart, or a single Bar in the case of a Bar
 * Chart. The Data component is responsible for handling all the data of the Chart. The entries
 * of the Data component correspond of an x and a y value.
 * The component is attached directly to a Chart component by dragging it onto the Chart.
 */
@DesignerComponent(version = YaVersion.CHART_DATA_2D_COMPONENT_VERSION,
    description = "A component that holds (x, y)-coordinate based data",
    category = ComponentCategory.CHARTS,
    iconName = "images/web.png")
@SimpleObject
@SuppressWarnings("checkstyle:JavadocParagraph")
public final class ChartData2D extends ChartDataBase {
  /**
   * Creates a new Coordinate Data component.
   */
  public ChartData2D(Chart chartContainer) {
    super(chartContainer);
  }

  /**
   * Adds an entry with the specified x and y value. Values can be specified as text,
   * or as numbers. For Line, Scatter, Area and Bar Charts, both values should represent a number.
   * For Bar charts, the x value is rounded to the nearest integer.
   * For Pie Charts, the x value is a text value.
   *
   * @param x - x value of entry
   * @param y - y value of entry
   */
  @SimpleFunction()
  public void AddEntry(final String x, final String y) {
    // Entry should be added via the Thread Runner asynchronously
    // to guarantee the order of data adding (e.g. CSV data
    // adding could be happening when this method is called,
    // so the task should be queued in the single Thread Runner)
    synchronized (this) {
      final AtomicBoolean isDone = new AtomicBoolean(false);
      threadRunner.execute(new Runnable() {
        @Override
        public void run() {
          try {
            // Create a 2-tuple, and add the tuple to the Data Series
            YailList pair = YailList.makeList(Arrays.asList(x, y));

            YailList labelList = container.Labels();
            int indexList = labelList.indexOf(x);
            if (indexList > -1){
              pair = YailList.makeList(Arrays.asList(indexList, y));
            }

            dataModel.addEntryFromTuple(pair);

            // Refresh Chart with new data
            onDataChange();
          } finally {
            synchronized (ChartData2D.this) {
              isDone.set(true);
              ChartData2D.this.notifyAll();
            }
          }
        }
      });
      try {
        if (!isDone.get()) {
          wait();
        }
      } catch (InterruptedException e) {
        // Just continue
      }
    }
  }

  /**
   * Removes an entry with the specified x and y value, provided it exists.
   * See {@link #AddEntry(String, String)} for an explanation of the valid entry values.
   *
   * @param x - x value of entry
   * @param y - y value of entry
   */
  @SimpleFunction()
  public void RemoveEntry(final String x, final String y) {
    // Entry should be deleted via the Thread Runner asynchronously
    // to guarantee the order of data adding (e.g. CSV data
    // adding could be happening when this method is called,
    // so the task should be queued in the single Thread Runner)
    synchronized (ChartData2D.this) {
      final AtomicBoolean isDone = new AtomicBoolean(false);
      threadRunner.execute(new Runnable() {
        @Override
        public void run() {
          try {
            // Create a 2-tuple, and remove the tuple from the Data Series
            YailList pair = YailList.makeList(Arrays.asList(x, y));

            YailList labelList = container.Labels();
            int indexList = labelList.indexOf(x);
            Entry currEntry = null;
            if (indexList > -1){ // do we have labels in the x value?
              pair = YailList.makeList(Arrays.asList(indexList, y));

             // currEntry = dataModel.getEntryFromTuple(pair);
            }
            currEntry = dataModel.getEntryFromTuple(pair);
            int index = dataModel.findEntryIndex(currEntry);

            dataModel.removeEntryFromTuple(pair);
            // Refresh Chart with new data
            resetHighlightAtIndex(index);
            onDataChange();
          } finally {
            synchronized (ChartData2D.this) {
              isDone.set(true);
              ChartData2D.this.notifyAll();
            }
          }
        }
      });
      try {
        if (!isDone.get()) {
          wait();
        }
      } catch (InterruptedException e) {
        // Just continue
      }
    }
  }

  /**
   * Returns a boolean value specifying whether an entry with the specified x and y
   * values exists. The boolean value of true is returned if the value exists,
   * and a false value otherwise. See {@link #AddEntry(String, String)}
   * for an explanation of the valid entry values.
   *
   * @param x - x value of entry
   * @param y - y value of entry
   * @return true if entry exists
   */
  @SuppressWarnings("TryWithIdenticalCatches")
  @SimpleFunction(description = "Checks whether an (x, y) entry exists in the Coordinate Data."
      + "Returns true if the Entry exists, and false otherwise.")
  public boolean DoesEntryExist(final String x, final String y) {
    try {
      return threadRunner.submit(new Callable<Boolean>() {
        @Override
        public Boolean call() {
          // Create a 2-tuple, and check whether the entry exists
          YailList pair = YailList.makeList(Arrays.asList(x, y));
          return dataModel.doesEntryExist(pair);
        }
      }).get();
    } catch (InterruptedException e) {
      Log.e(this.getClass().getName(), e.getMessage());
    } catch (ExecutionException e) {
      Log.e(this.getClass().getName(), e.getMessage());
    }

    // Exceptions thrown (behavior undefined): Assume entry not found
    return false;
  }

  /**
   * Draws the line of best fit.
   *
   * @param xList - the list of x values
   * @param yList - the list of y values
   */
  @SimpleFunction(description = "Draws the corresponding line of best fit on the graph")
  @Deprecated
  public void DrawLineOfBestFit(final YailList xList, final YailList yList) {
    List<?> predictions = (List<?>) Regression.computeLineOfBestFit(castToDouble((LList) xList.getCdr()),
        castToDouble((LList) yList.getCdr()))
        .get("predictions");
    final List<List<?>> predictionPairs = new ArrayList<>();
    List<?> xValues = (List<?>) xList.getCdr();
    for (int i = 0; i < xValues.size(); i++) {
      predictionPairs.add(Arrays.asList(xValues.get(i), predictions.get(i)));
    }
    YailList predictionPairsList = YailList.makeList(predictionPairs);
    dataModel.importFromList(predictionPairsList);
    if (dataModel.getDataset() instanceof LineDataSet) {
      ((LineDataSet) dataModel.getDataset()).setDrawCircles(false);
      ((LineDataSet) dataModel.getDataset()).setDrawValues(false);
    }
    onDataChange();
  }

  private static class AnomalyManager {
    Set<Integer> indexes = new HashSet<>();
    Set<Double> xValues = new HashSet<>();

    @Override
    public String toString() {
      return "{indexes: " + indexes + ", xValues: " + xValues + "}";
    }
  }

  /**
   * Highlights all given data points on the Chart in the color of choice.
   *
   * @param dataPoints - the list of data points. A data point inside this list is a pair of point
   *                   index and point value: [[point index, point value],...,[,]]
   * @param color - the highlight color chosen by the user
   */
  @SimpleFunction(description = "Highlights data points of choice on the Chart in the color of "
      + "choice. This block expects a list of data points, each data point is an index, value pair")
  public void HighlightDataPoints(final YailList dataPoints, int color) {
    List<?> dataPointsList = (LList) dataPoints.getCdr();
    if (!dataPoints.isEmpty()) {
      List<?> entries = dataModel.getEntries();
      Map<Double, AnomalyManager> anomalyMap = new HashMap<>();
      int i = 0;
      for (Entry entry : dataModel.getEntries()) {
        if (!anomalyMap.containsKey((double) entry.getY())) {
          anomalyMap.put((double) entry.getY(), new AnomalyManager());
        }
        anomalyMap.get((double) entry.getY()).xValues.add((double) entry.getX());
        anomalyMap.get((double) entry.getY()).indexes.add(i++);
      }
      int[] highlights = new int[entries.size()];
      Arrays.fill(highlights, dataModel.getDataset().getColor());

      for (Object dataPoint: dataPointsList) {
        if (!(dataPoint instanceof YailList)) {
          continue;
        }
        Number y = (Number) ((YailList) dataPoint).getObject(1);
        AnomalyManager anomalyManager = anomalyMap.get(y.doubleValue());
        if (anomalyManager == null) {
          continue;
        }
        Number x = (Number) ((YailList) dataPoint).getObject(0);
        if (anomalyManager.xValues.contains(x.doubleValue())
            || anomalyManager.indexes.contains(x.intValue() - 1)) {
          for (Integer index : anomalyManager.indexes) {
            highlights[index] = color;
          }
        }
      }
      ((LineDataSet) dataModel.getDataset()).setCircleColors(highlights);
      onDataChange();
    } else {
      throw new IllegalStateException("Anomalies list is Empty. Nothing to highlight!");
    }
  }

  public List<Double> getYValues() {
    List<Double> yValues = new ArrayList<>();
    for (Entry entry : dataModel.getEntries()) {
      yValues.add((double) entry.getY());
    }
    return yValues;
  }

  private void resetHighlightAtIndex(int index) {
    if (dataModel.getDataset() instanceof LineDataSet) {
      List<Integer> defaultColors = ((LineDataSet) dataModel.getDataset()).getCircleColors();
      defaultColors.remove(index);
    }
  }
}

