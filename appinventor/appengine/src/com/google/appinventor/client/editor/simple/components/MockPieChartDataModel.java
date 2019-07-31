package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.Data;
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

  }

  @Override
  protected String getDefaultTupleEntry(int index) {
    return index + "";
  }

  @Override
  public void clearEntries() {

  }

  @Override
  protected void postDataImportAction() {

  }

  @Override
  protected int getTupleSize() {
    return 2;
  }
}
