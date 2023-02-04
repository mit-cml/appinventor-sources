// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer;

import com.google.appinventor.components.common.PointStyle;

import org.junit.Before;
import org.junit.Test;

public class ScatterChartDataModelTest
    extends AbstractPointChartDataModelTest<ScatterChartDataModel, ScatterData> {
  @Before
  @Override
  public void setup() {
    data = new ScatterData();
    model = new ScatterChartDataModel(data, new ScatterChartView(new Chart(getForm())));
  }

  /**
   * Test case to ensure that changing the Point Shape to
   * the Circle shape properly updates the Point Shape.
   */
  @Test
  public void testSetPointShapeCircle() {
    PointStyle shape = PointStyle.Circle;
    ScatterChart.ScatterShape expectedShape = ScatterChart.ScatterShape.CIRCLE;

    setPointShapeHelper(shape, expectedShape);
  }

  /**
   * Test case to ensure that changing the Point Shape to
   * the Square shape properly updates the Point Shape.
   */
  @Test
  public void testSetPointShapeSquare() {
    PointStyle shape = PointStyle.Square;
    ScatterChart.ScatterShape expectedShape = ScatterChart.ScatterShape.SQUARE;

    setPointShapeHelper(shape, expectedShape);
  }

  /**
   * Test case to ensure that changing the Point Shape to
   * the Triangle shape properly updates the Point Shape.
   */
  @Test
  public void testSetPointShapeTriangle() {
    PointStyle shape = PointStyle.Triangle;
    ScatterChart.ScatterShape expectedShape = ScatterChart.ScatterShape.TRIANGLE;

    setPointShapeHelper(shape, expectedShape);
  }

  /**
   * Test case to ensure that changing the Point Shape to
   * the Cross shape properly updates the Point Shape.
   */
  @Test
  public void testSetPointShapeCross() {
    PointStyle shape = PointStyle.Cross;
    ScatterChart.ScatterShape expectedShape = ScatterChart.ScatterShape.CROSS;

    setPointShapeHelper(shape, expectedShape);
  }

  /**
   * Test case to ensure that changing the Point Shape to
   * the X shape properly updates the Point Shape.
   */
  @Test
  public void testSetPointShapeX() {
    PointStyle shape = PointStyle.X;
    ScatterChart.ScatterShape expectedShape = ScatterChart.ScatterShape.X;

    setPointShapeHelper(shape, expectedShape);
  }

  /**
   * Helper method that sets the specified Point Shape to the Charrt Data Model,
   * and then asserts that the Shape is equal to the expected shape.
   * @param shape  shape value to set to the Data Series (integer)
   * @param expectedShape  Expected Shape
   */
  private void setPointShapeHelper(PointStyle shape, ScatterChart.ScatterShape expectedShape) {
    model.setPointShape(shape);

    // Since we cannot get the point shape directly from the Data Set,
    // we need to get the renderer instead, and check whether the class
    // of the expected Renderer matches the class of the Dataset's Shape
    // Renderer (each Shape has it's own renderer)
    IShapeRenderer renderer = ScatterDataSet.getRendererForShape(expectedShape);
    assertEquals(renderer.getClass(), model.getDataset().getShapeRenderer().getClass());
  }
}
