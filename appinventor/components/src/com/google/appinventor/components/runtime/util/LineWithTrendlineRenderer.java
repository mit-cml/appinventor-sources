// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.util.Arrays;

public class LineWithTrendlineRenderer extends LineChartRenderer {
  private static final String LOG_TAG = LineWithTrendlineRenderer.class.getSimpleName();
  private static final boolean DEBUG = false;

  public LineWithTrendlineRenderer(
      LineDataProvider chart,
      ChartAnimator animator,
      ViewPortHandler viewPortHandler) {
    super(chart, animator, viewPortHandler);
  }

  /**
   * Draws the provided ScatterDataSet with a line of best fit.
   *
   * @param c Canvas to draw on
   */
  public void drawData(Canvas c) {
    LineData scatterData = mChart.getLineData();
    for (ILineDataSet dataSet : scatterData.getDataSets()) {
      if (dataSet.isVisible() && dataSet instanceof HasTrendline) {
        drawTrendline(c, dataSet);
      }
    }

    super.drawData(c);
  }

  protected void drawTrendline(Canvas c, ILineDataSet dataSet) {
    if (dataSet instanceof HasTrendline) {
      final Transformer trans = this.mChart.getTransformer(dataSet.getAxisDependency());
      if (DEBUG) {
        Log.d(LOG_TAG, "chart minX = " + mChart.getXChartMin());
        Log.d(LOG_TAG, "chart maxX = " + mChart.getXChartMax());
        Log.d(LOG_TAG, "Drawing line of best fit for " + dataSet);
      }
      HasTrendline<?> hasTrendline = (HasTrendline<?>) dataSet;
      if (!hasTrendline.isVisible()) {
        return;
      }
      final Paint p = new Paint();
      p.setStyle(Paint.Style.STROKE);
      p.setStrokeWidth(hasTrendline.getLineWidth());
      p.setColor(hasTrendline.getColor());
      if (DEBUG) {
        Log.d(LOG_TAG, "color = " + p.getColor());
      }
      p.setAlpha((hasTrendline.getColor() >> 24) & 0xFF);
      if (DEBUG) {
        Log.d(LOG_TAG, "alpha = " + p.getAlpha());
      }
      p.setPathEffect(hasTrendline.getDashPathEffect());
      float[] lineBuffer = hasTrendline.getPoints(mChart.getXChartMin(), mChart.getXChartMax(),
          mChart.getWidth());
      if (DEBUG) {
        Log.d(LOG_TAG, "values = " + Arrays.toString(lineBuffer));
      }
      trans.pointValuesToPixel(lineBuffer);
      if (DEBUG) {
        Log.d(LOG_TAG, "translated values = " + Arrays.toString(lineBuffer));
      }
      c.drawLines(lineBuffer, 0, lineBuffer.length, p);
    }
  }
}
