// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinearRegression implements TrendlineCalculator {
  /**
   * Calculates the line of best fit.
   *
   * @param x - the list of x values
   * @param y - the list of y values
   * @return list. 1st element of the list is the slope, 2nd element is the intercept, 3rd
   *     correlation coefficient, 4th element is the line of best fit prediction values
   */
  public Map<String, Object> compute(List<Double> x, List<Double> y) {
    if (x.isEmpty() || y.isEmpty()) {
      throw new IllegalStateException("List must have at least one element");
    }
    if (x.size() != y.size()) {
      throw new IllegalStateException("Must have equal X and Y data points");
    }
    int n = x.size();

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

    for (Double value : x) {
      double prediction = slope * value + intercept;
      predictions.add(prediction);
    }

    // use formula for calculating correlation coefficient.
    final double corr = (n * sumXY - sumx * sumy)
        / (Math.sqrt((n * squareSumX - sumx * sumx) * (n * squareSumY - sumy * sumy)));

    Map<String, Object> resultDic = new HashMap<>();
    resultDic.put("slope", slope);
    resultDic.put("Yintercept", intercept);
    resultDic.put("correlation coefficient", corr);
    resultDic.put("predictions", predictions);
    resultDic.put("Xintercepts", slope == 0 ? Double.NaN : -intercept / slope);
    resultDic.put("r^2", corr * corr);

    return resultDic;
  }

  @Override
  public float[] computePoints(Map<String, Object> results, float xMin, float xMax, int viewWidth,
      int steps) {
    if (!results.containsKey("slope")) {
      return new float[0];  // we aren't ready yet...
    }
    double m = (Double) results.get("slope");
    double b = (Double) results.get("Yintercept");
    return new float[] {
        xMin, (float) (m * xMin + b),
        xMax, (float) (m * xMax + b)
    };
  }
}
