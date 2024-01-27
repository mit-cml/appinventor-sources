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

/**
 * This class wraps the Apache Commons Math library to produce a trendline using the ordinary least
 * squares (OLS) method. Subclasses must implement the abstract methods to provide the data to the
 * solver.
 */
public abstract class OlsTrendLine implements TrendlineCalculator {
  private static final boolean DEBUG = false;
  private static final double SIGNIFICANCE = 1e14;

  /**
   * Implement this method to provide the data to the solver. For example, a linear trendline would
   * provide a vector of [1, x] for each x value.
   *
   * @param x the independent variable
   * @return the terms used to compute the dependent variable
   */
  @SuppressWarnings("checkstyle:MethodName")
  protected abstract double[] xVector(double x);

  /**
   * Implement this method to specify whether the dependent variable should be transformed by
   * taking its log before computing the regression.
   *
   * @return true if the y value should be log-transformed, otherwise false.
   */
  protected abstract boolean logY();

  /**
   * Implement this method to specify the number of parameters to calculate. For example, a linear
   * trendline would return 2 (intercept and slope), while a quadratic trendline would return 3.
   *
   * @return the number of parameters to calculate
   */
  protected abstract int size(); // number of parameters to calculate

  /**
   * Computes the regression parameters, returning a map of parameter name to value. The map will
   * contain the following keys: intercept, slope, x^2, r^2.
   *
   * @param x the list of x values
   * @param y the list of y values
   * @return the results of the OLS regression
   */
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
    RealMatrix coef = MatrixUtils.createColumnRealMatrix(ols.estimateRegressionParameters());
    if (DEBUG) {
      System.err.println("coef = " + coef);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("intercept", round(coef.getEntry(0, 0)));
    result.put("slope", round(coef.getEntry(1, 0)));
    if (size() > 2) {
      result.put("x^2", round(coef.getEntry(2, 0)));
    }
    result.put("r^2", ols.calculateRSquared());
    return result;
  }

  private static double round(double value) {
    return Math.round(value * SIGNIFICANCE) / SIGNIFICANCE;
  }
}
