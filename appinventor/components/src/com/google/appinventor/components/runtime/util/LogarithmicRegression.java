// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

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
  public float[] computePoints(Map<String, Object> results, float xMin, float xMax, int viewWidth,
      int steps) {
    if (!results.containsKey("slope")) {
      return new float[0];  // we aren't ready yet...
    }
    double m = (Double) results.get("slope");
    double b = (Double) results.get("intercept");
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
