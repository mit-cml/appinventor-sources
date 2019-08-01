package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.output.OdeLog;
import org.pepstock.charba.client.data.Data;
import org.pepstock.charba.client.data.Labels;
import org.pepstock.charba.client.data.PieDataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockPieChartDataModel extends MockChartDataModel<PieDataset> {
  // Local references of the colors and labels properties have to be
  // kept in the Data Model class to be able to add to them and set them.
  // This is due to the API for changing colors and labels being quite limited (in v2.5)
  private List<String> colors = new ArrayList<String>();
  private List<String> labels = new ArrayList<String>();
  private String color = "";

  // Since labels of the Legend are global per entry, the
  // view has to be kept in the Data Model. It is
  // required to notify the view of the deletion of the
  // Pie Chart Data Model to handle the Legend creation.
  private MockPieChartView view;

  /**
   * Creates a new Mock Pie Chart Model object instance, linking it with
   * the Data object of a specific Chart.
   *
   * @param chartData Chart Data object to link to
   * @Param view  Mock Pie Chart View to link Data Model to
   */
  public MockPieChartDataModel(Data chartData, MockPieChartView view) {
    super(chartData);

    // Set a local reference to the parent Mock Pie Chart View
    this.view = view;

    // Create the Data Series object
    dataSeries = new PieDataset();

    // Set the default style properties for the Data Series
    setDefaultStylingProperties();

    // Adds the Data Series to the Chart.
    addDataSeriesToChart();
  }

  @Override
  public void changeColor(String color) {
    this.color = color;
    color = getHexColor(color);

    // Clear the old colors list
    colors.clear();

    // Add the same color for every entry
    // TODO: Possibly make a List of colors as a Designer property?
    for (int i = 0; i < dataSeries.getData().size(); ++i) {
      colors.add(color);
    }

    // Since the setBackgroundColor method accepts varargs as parameters,
    // we have to cast our result to a String array.
    dataSeries.setBackgroundColor(colors.toArray(new String[0]));
  }

  @Override
  protected void setDefaultElements() {
    final int values = 3; // Number of values to add

    // Get the index of the Data Series to use for default entries, and
    // multiply it by the number of values to use to create an offset
    int indexOffset = chartData.getDatasets().indexOf(this.dataSeries) * values;

    for (int i = 0; i < values; ++i) {
      // Construct the x and y values based on the data series
      // index offset and the loop index. This allows to differentiate
      // the default entries of each individual Data Series.
      double xValue = indexOffset + i + 1;
      double yValue = indexOffset + i + 1;

      // Add an entry based on the constructed values
      addEntryFromTuple(xValue + "", yValue + "");
    }
  }

  @Override
  protected void setDefaultStylingProperties() {

  }

  @Override
  public void addEntryFromTuple(String... tuple) {
    try {
      // First entry is a String
      String x = tuple[0];

      // Second entry is expected to be a double; attempt parsing
      Double y = Double.parseDouble(tuple[1]);

      // Add data entry
      if (dataSeries.getData().size() == 0) {
        dataSeries.setData(y);
      } else {
        dataSeries.getData().add(y);
      }

      // Add entry label (x value corresponds to the label)
      labels.add(x);
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
    labels.clear();
  }

  @Override
  protected void postDataImportAction() {
    // No data points generated, fallback to default option.
    if (dataSeries.getData().isEmpty()) {
      setDefaultElements();
    }

    // After importing the data, the colors have to be changed again
    // (since the new entry count might be different)
    // TODO: possible to optimize by tracking differences
    changeColor(this.color);
  }

  @Override
  protected int getTupleSize() {
    return 2;
  }

  /**
   * Returns a List of entry labels in order
   * @return  List of entry labels (Strings_
   */
  public List<String> getLabels() {
    return labels;
  }

  @Override
  public void removeDataSeriesFromChart() {
    // The view has to be notified of the removal of the Data Model
    view.removeDataModel(this);

    // Proceed with general removal from the Chart
    super.removeDataSeriesFromChart();
  }
}
