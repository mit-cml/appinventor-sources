package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.ScatterData;
import org.junit.Before;

public class ScatterChartDataModelTest
    extends PointChartDataModelTest<ScatterChartDataModel, ScatterData> {
  @Before
  @Override
  public void setup() {
    data = new ScatterData();
    model = new ScatterChartDataModel(data);
  }
}
