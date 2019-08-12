package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.data.BarDataset;
import org.pepstock.charba.client.data.Data;

public class MockBarChartDataModel extends MockChartDataModel<BarDataset> {
  public MockBarChartDataModel(Data chartData) {
    super(chartData);

    // Create the Data Series object
    dataSeries = new BarDataset();

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
