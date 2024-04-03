// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.List;
import java.util.Map;

public class ExponentialRegression extends OlsTrendLine {
  @Override
  protected double[] xVector(double x) {
    return new double[] {
      1,
      x
    };
  }

  @Override
  protected boolean logY() {
    return true;
  }

  @Override
  protected int size() {
    return 2;
  }

  @Override
  public Map<String, Object> compute(List<Double> x, List<Double> y) {
    Map<String, Object> result = super.compute(x, y);
    result.remove("x^2");
    double m = (Double) result.remove("slope");
    double i = (Double) result.remove("intercept");
    result.put("a", Math.exp(i));
    result.put("b", Math.exp(m));
    return result;
  }

  @Override
  public float[] computePoints(Map<String, Object> results, float xMin, float xMax, int viewWidth,
      int steps) {
    if (!results.containsKey("a")) {
      return new float[0];  // we aren't ready yet...
    }
    double a = (Double) results.get("a");
    double b = (Double) results.get("b");
    float[] result = new float[steps * 4];
    float lastX = Float.NEGATIVE_INFINITY;
    float lastY = Float.NEGATIVE_INFINITY;
    boolean first = true;
    for (int i = 0; i < steps; i++) {
      if (first) {
        first = false;
        lastX = (xMin + i * (xMax - xMin) / (float) steps);
        lastY = (float) (a * Math.pow(b, lastX));
      }
      result[4 * i] = lastX;
      result[4 * i + 1] = lastY;
      float newX = (xMin + (i + 1) * (xMax - xMin) / (float) steps);
      float newY = (float) (a * Math.pow(b, newX));
      result[4 * i + 2] = newX;
      result[4 * i + 3] = newY;
      lastX = newX;
      lastY = newY;
    }
    return result;
  }
}
