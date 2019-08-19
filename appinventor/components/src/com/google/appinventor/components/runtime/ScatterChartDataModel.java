package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;

public class ScatterChartDataModel extends PointChartDataModel<ScatterDataSet, ScatterData> {
  /**
   * Initializes a new ScatterChartDataModel object instance.
   *
   * @param data Chart data instance
   */
  public ScatterChartDataModel(ScatterData data) {
    super(data);
    dataset = new ScatterDataSet(new ArrayList<Entry>(), "");
    this.data.addDataSet(dataset); // Safe add
    setDefaultStylingProperties();
  }

  @Override
  public void addEntryFromTuple(YailList tuple) {
    // Construct an entry from the prvoided tuple
    Entry entry = getEntryFromTuple(tuple);

    // If entry constructed successfully, add it to the Data Series
    if (entry != null) {
      entries.add(entry);
    }
  }

  @Override
  protected void setDefaultStylingProperties() {
    getDataset().setScatterShape(ScatterChart.ScatterShape.CIRCLE);
  }

  public void setPointShape(int shape) {
    switch (shape) {
      case ComponentConstants.CHART_POINT_STYLE_CIRCLE:
        dataset.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        break;

      case ComponentConstants.CHART_POINT_STYLE_SQUARE:
        dataset.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        break;

      case ComponentConstants.CHART_POINT_STYLE_TRIANGLE:
        dataset.setScatterShape(ScatterChart.ScatterShape.TRIANGLE);
        break;

      case ComponentConstants.CHART_POINT_STYLE_CROSS:
        dataset.setScatterShape(ScatterChart.ScatterShape.CROSS);
        break;

      case ComponentConstants.CHART_POINT_STYLE_X:
        dataset.setScatterShape(ScatterChart.ScatterShape.X);
        break;

      default:
        dataset.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
    }
  }
}
