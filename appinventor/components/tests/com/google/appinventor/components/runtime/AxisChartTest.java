package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Abstract test class for Axis based Charts.
 *
 * Contains test cases related to functionality specific for
 * Axis based Charts.
 */
public abstract class AxisChartTest<V extends AxisChartView,
    C extends BarLineChartBase> extends AbstractChartTest<V, C> {
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
}
