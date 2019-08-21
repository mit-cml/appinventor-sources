package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;

public class LineChartDataModel extends LineChartBaseDataModel {
  /**
   * Initializes a new LineChartDataModel object instance.
   *
   * @param data Line Chart Data object instance
   */
  public LineChartDataModel(LineData data) {
    super(data);
  }
}
