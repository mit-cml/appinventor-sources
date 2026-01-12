// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.graphics.DashPathEffect;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;

/**
 * Classes that provide trend lines for visualizations should implement this interface.
 *
 * @param <T> the type of entry consumed by the trendline algorithm
 */
public interface HasTrendline<T extends Entry> extends IBarLineScatterCandleBubbleDataSet<T> {
  /**
   * Returns the points of the trendline between the xMin and xMax values.
   *
   * @param xMin the leftmost edge of the chart or the minimum x value of the data series
   * @param xMax the rightmost edge of the chart or the maximum x value of the data series
   * @param viewWidth the width of the chart view, in pixels
   * @return an array of line segments, where each segment is represented by four floats:
   *         x0, y0, x1, y1
   */
  float[] getPoints(float xMin, float xMax, int viewWidth);

  /**
   * Gets the color of the trendline.
   */
  int getColor();

  /**
   * Gets the dash path effect of the trendline.
   */
  DashPathEffect getDashPathEffect();

  /**
   * Gets the width of the trendline, in pixels.
   */
  float getLineWidth();

  /**
   * Gets the visibility of the trendline.
   */
  boolean isVisible();
}
