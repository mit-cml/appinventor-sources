// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.common.TrendlineCalculator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public abstract class OlsTrendLine implements TrendlineCalculator {
  private static final boolean DEBUG = false;
  private static final double SIGNIFICANCE = 1e14;

  protected abstract double[] xVector(double x); // create vector of values from x
  protected abstract boolean logY(); // set true to predict log of y (note: y must be positive)

  public Map<String, Object> compute(List<Double> x, List<Double> y) {
    if (x.isEmpty() || y.isEmpty()) {
      throw new IllegalStateException("List must have at least one element");
    }
    if (x.size() != y.size()) {
      throw new IllegalStateException("Must have equal X and Y data points");
    }
    double[][] xData = new double[x.size()][];
    for (int i = 0; i < x.size(); i++) {
      // the implementation determines how to produce a vector of predictors from a single x
      xData[i] = xVector(x.get(i));
    }
    double[] yData = new double[y.size()];
    if (logY()) { // in some models we are predicting ln y, so we replace each y with ln y
      int i = 0;
      for (Double value : y) {
        yData[i++] = Math.log(value);
      }
    } else {
      int i = 0;
      for (Double value : y) {
        yData[i++] = value;
      }
    }
    if (DEBUG) {
      System.err.println("xData = " + Arrays.toString(xData));
      System.err.println("yData = " + Arrays.toString(yData));
    }
    OLSMultipleLinearRegression ols = new OLSMultipleLinearRegression();
    ols.setNoIntercept(true); // let the implementation include a constant in xVector if desired
    ols.newSampleData(yData, xData); // provide the data to the model
    RealMatrix coef = MatrixUtils.createColumnRealMatrix(ols.estimateRegressionParameters()); // get our coefs
    if (DEBUG) {
      System.err.println("coef = " + coef);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("intercept", round(coef.getEntry(0, 0)));
    result.put("slope", round(coef.getEntry(1, 0)));
    result.put("x^2", round(coef.getEntry(2, 0)));
    result.put("r^2", ols.calculateRSquared());
    return result;
  }

  private static double round(double value) {
    return Math.round(value * SIGNIFICANCE) / SIGNIFICANCE;
  }
}
