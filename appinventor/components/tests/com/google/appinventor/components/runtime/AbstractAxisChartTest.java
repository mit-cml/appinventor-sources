// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Abstract test class for Axis based Charts.
 *
 * <p>Contains test cases related to functionality specific for
 * Axis based Charts.</p>
 */
@SuppressWarnings("checkstyle:MemberName")
public abstract class AbstractAxisChartTest<
    C extends BarLineChartBase<?>,
    V extends AxisChartView<?, ?, ?, C, V>
    > extends AbstractChartTest<C, V> {
  protected ValueFormatter xAxisValueFormatter;

  /**
   * Test case to ensure that setting the Legend Enabled
   * property to false actually hides the Legend.
   */
  @Test
  public void testSetLegendEnabledFalse() {
    boolean value = false;
    chartComponent.LegendEnabled(value);

    assertEquals(value, chart.getLegend().isEnabled());
  }

  /**
   * Test case to ensure that setting the Grid Enabled
   * property to false actually hides the Grid.
   */
  @Test
  public void testSetGridEnabledFalse() {
    boolean value = false;
    chartComponent.GridEnabled(value);

    assertEquals(value, chart.getXAxis().isDrawGridLinesEnabled());
    assertEquals(value, chart.getAxisLeft().isDrawGridLinesEnabled());
  }

  /**
   * Test case to ensure that getting a formatted value for
   * a whole number when no label exists for it returns the
   * raw value.
   */
  @Test
  public void testLabelFormattingWholeNumber() {
    String value = xAxisValueFormatter.getFormattedValue(5);
    String expectedValue = "5.0";

    assertEquals(expectedValue, value);
  }

  /**
   * Test case to ensure that getting a formatted value for
   * a number with a decimal part when no label exists for it
   * returns the raw value.
   */
  @Test
  public void testLabelFormattingDecimalNumber() {
    String value = xAxisValueFormatter.getFormattedValue(1.3f);
    String expectedValue = "1.3";

    assertEquals(expectedValue, value);
  }

  /**
   * Test case to ensure that getting a formatted value for
   * a negative number with a decimal part when no label
   * exists for it returns the raw value.
   */
  @Test
  public void testLabelFormattingNegativeDecimalNumber() {
    String value = xAxisValueFormatter.getFormattedValue(-2.1f);
    String expectedValue = "-2.1";

    assertEquals(expectedValue, value);
  }

  /**
   * Test case to ensure that setting the custom Labels
   * List with a single entry formats the x value of 0
   * with the label properly.
   */
  @Test
  public void testSetLabelsSingle() {
    List<String> labelValues = new ArrayList<String>() {{
        add("A");
      }};
    YailList labels = YailList.makeList(labelValues);

    setLabelsHelper(labels);
  }

  /**
   * Test case to ensure that setting the custom Labels
   * List with multiple entries formats the x values properly
   * with the label values.
   */
  @Test
  public void testSetLabelsMultiple() {
    List<String> labelValues = new ArrayList<String>() {{
        add("A");
        add("B");
        add("E");
      }};
    YailList labels = YailList.makeList(labelValues);

    setLabelsHelper(labels);
  }

  /**
   * Test case to ensure that setting the custom Labels
   * List with multiple entries formats the x values properly
   * with the label values. The test additionally makes sure
   * that the value that falls outside the range of the Labels
   * list is formatted to the raw value.
   */
  @Test
  public void testSetLabelsMultipleAfterLabels() {
    List<String> labelValues = new ArrayList<String>() {{
        add("A");
        add("C");
        add("Test");
        add("Test2");
      }};
    YailList labels = YailList.makeList(labelValues);

    setLabelsHelper(labels);

    String value = xAxisValueFormatter.getFormattedValue(4);
    String expectedValue = "4.0";
    assertEquals(expectedValue, value);
  }

  /**
   * Test case to ensure that getting a formatted value for
   * a negative number which equals the x axis minimum value
   * formats the value to the single existing custom x axis label.
   */
  @Test
  public void testLabelFormattingNegativeNumberEqualMin() {
    final float minValue = -1f;
    final float checkValue = -1f;
    checkSingleLabelHelper(minValue, checkValue, true);
  }

  /**
   * Test case to ensure that getting a formatted value for
   * a negative number which corresponds to the minimum
   * x axis value integer representation properly formats
   * the value to the single existing custom x axis label.
   */
  @Test
  public void testLabelFormattingMinValueRounding() {
    final float minValue = -1.6f;
    final float checkValue = -1f;
    checkSingleLabelHelper(minValue, checkValue, true);
  }

  /**
   * Test case to ensure that getting a formatted value for
   * a positive number which corresponds to the minimum
   * x axis value integer representation properly formats
   * the value to the single existing custom x axis label.
   */
  @Test
  public void testLabelFormattingPositiveValueNonZero() {
    final float minValue = 5.4f; // Floored to 5
    final float checkValue = 5f;
    checkSingleLabelHelper(minValue, checkValue, true);
  }


  /**
   * Helper method that sets the corresponding Labels
   * List property to the Chart component, asserts
   * that the property has been set successfully, and
   * verifies that the x values are then formatted with the
   * labels correctly.
   * @param labels  Labels List to apply to the Chart component.
   */
  private void setLabelsHelper(YailList labels) {
    // Set x axis minimum to 0 to simulate the effects of
    // having an x axis starting from 0.
    chart.getXAxis().setAxisMinimum(0f);

    chartComponent.Labels(labels);

    assertEquals(labels, chartComponent.Labels());

    for (int i = 0; i < labels.size(); ++i) {
      String expectedLabel = labels.getString(i);
      String actualLabel = xAxisValueFormatter.getFormattedValue(i);

      assertEquals(expectedLabel, actualLabel);
    }
  }

  /**
   * Helper method that sets a single custom x axis label
   * to the Chart component, sets the x axis minimum value
   * to the specified value, and asserts that the specified
   * value is (or is not) formatted to the custom label.
   * @param minValue  Minimum x axis value to use
   * @param formatValue  Value to format
   * @param isLabel  True if the value should be formatted to the custom label
   */
  private void checkSingleLabelHelper(float minValue, float formatValue, boolean isLabel) {
    // Set x axis minimum to the specified value to simulate
    // the effects of having the x axis start from the specified value.
    chart.getXAxis().setAxisMinimum(minValue);

    // Create and set a single custom label to the Chart
    String label = "Label";
    YailList labels = YailList.makeList(Collections.singletonList(label));
    chartComponent.Labels(labels);

    String value = xAxisValueFormatter.getFormattedValue(formatValue);
    assertEquals(isLabel, (label.equals(value)));
  }
}
