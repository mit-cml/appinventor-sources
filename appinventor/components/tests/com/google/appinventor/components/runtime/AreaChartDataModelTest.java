package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineData;
import org.junit.Before;

/**
 * Unit tests for the AreaChartModel class.
 */
public class AreaChartDataModelTest extends LineChartDataModelBaseTest {
  @Before
  @Override
  public void setup() {
    data = new LineData();
    model = new AreaChartDataModel(data);
  }
}
