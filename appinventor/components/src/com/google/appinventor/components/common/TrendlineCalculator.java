// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.List;
import java.util.Map;

/**
 * The TrendlineCalculator interface defines the methods that must be implemented by a class
 * that computes a best fit model from a set of data points.
 */
public interface TrendlineCalculator {
  /**
   * Compute the trendline for the given x and y values. The x and y lists must be the same length.
   *
   * @param x the list of x values
   * @param y the list of y values
   * @return a map containing the results of the computation. The specific keys will vary depending
   *         on the underlying model.
   */
  Map<String, Object> compute(List<Double> x, List<Double> y);

  /**
   * Compute an Android canvas compatible float array that contains the points of the trendline
   * to be drawn on a Chart. The result is a float array containing the x and y coordinates of
   * each line segment.
   *
   * @param results the results from a previous call {@link #compute(List, List)}
   * @param xMin the x value of the left edge of the chart
   * @param xMax the x value of the right edge of the chart
   * @param viewWidth the width of the chart view, in pixels
   * @param steps the number of segments to compute
   * @return a float array containing the points of the trendline of the form
   *         x0, y0, x1, y1, x1, y1, x2, y2 ...
   */
  float[] computePoints(Map<String, Object> results, float xMin, float xMax, int viewWidth,
      int steps);
}
