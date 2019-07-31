package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.Labels;
import org.pepstock.charba.client.data.PieDataset;

public class MockPieChartDataModel extends MockChartDataModel<PieDataset> {
  /**
   * Creates a new Mock Pie Chart Model object instance, linking it with
   * the Data object of a specific Chart.
   *
   * @param chartData Chart Data object to link to
   */
  public MockPieChartDataModel(Data chartData) {
    super(chartData);

    // Create the Data Series object
    dataSeries = new PieDataset();

    // Set the default style properties for the Data Series
    setDefaultStylingProperties();

    // Adds the Data Series to the Chart.
    addDataSeriesToChart();
  }

  @Override
  public void changeColor(String color) {
    color = getHexColor(color);
    dataSeries.setBackgroundColor(color);
    dataSeries.setBorderColor(color);
  }

  @Override
  protected void setDefaultElements() {

  }

  @Override
  protected void setDefaultStylingProperties() {

  }

  @Override
  public void addEntryFromTuple(String... tuple) {
    try {
      String x = tuple[0];
      Double y = Double.parseDouble(tuple[1]);

      // Add data entry
      if (dataSeries.getData().size() == 0) {
        dataSeries.setData(y);
      } else {
        dataSeries.getData().add(y);
      }

      // Add entry label (legend entry)
      chartData.getLabels().add(x);
    } catch (NumberFormatException e) {
      // Wrong input. Do nothing.
    }
  }

  @Override
  protected String getDefaultTupleEntry(int index) {
    return index + "";
  }

  @Override
  public void clearEntries() {
    dataSeries.getData().clear();
    chartData.setLabels("");
    chartData.getLabels().remove(0);
  }

  @Override
  protected void postDataImportAction() {
    // No data points generated, fallback to default option.
    if (dataSeries.getData().isEmpty()) {
      setDefaultElements();
    }
  }

  @Override
  protected int getTupleSize() {
    return 2;
  }
}
