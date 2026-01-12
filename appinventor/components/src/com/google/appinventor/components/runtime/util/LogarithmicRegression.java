// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.List;
import java.util.Map;

public class LogarithmicRegression extends OlsTrendLine {
  @Override
  protected double[] xVector(double x) {
    return new double[] {
      1,
      Math.log(x)
    };
  }

  @Override
  protected boolean logY() {
    return false;
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
    result.put("a", i);
    result.put("b", m);
    return result;
  }

  @Override
  public float[] computePoints(Map<String, Object> results, float xMin, float xMax, int viewWidth,
      int steps) {
    if (!results.containsKey("b")) {
      return new float[0];  // we aren't ready yet...
    }
    if (xMax <= 0) {
      return new float[0];  // log is undefined for x <= 0
    }
    if (xMin <= 0) {
      xMin = Math.min(0.0001f, xMax / (steps + 1));  // log is undefined for x <= 0, so we use a small value
    }
    double m = (Double) results.get("b");
    double b = (Double) results.get("a");
    float[] result = new float[steps * 4];
    float lastX = Float.NEGATIVE_INFINITY;
    float lastY = Float.NEGATIVE_INFINITY;
    boolean first = true;
    for (int i = 0; i < steps; i++) {
      if (first) {
        first = false;
        lastX = (xMin + i * (xMax - xMin) / (float) steps);
        lastY = (float) (m * Math.log(lastX) + b);
      }
      result[4 * i] = lastX;
      result[4 * i + 1] = lastY;
      float newX = (xMin + (i + 1) * (xMax - xMin) / (float) steps);
      float newY = (float) (m * Math.log(newX) + b);
      result[4 * i + 2] = newX;
      result[4 * i + 3] = newY;
      lastX = newX;
      lastY = newY;
    }
    return result;
  }
}
