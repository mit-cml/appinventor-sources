package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineData;
import org.junit.Before;

/**
 * Unit tests for the LineChartModel class.
 */
public class LineChartDataModelTest
    extends PointChartDataModelTest<LineChartDataModel, LineData> {
  @Before
  @Override
  public void setup() {
    data = new LineData();
    model = new LineChartDataModel(data);
  }
}
