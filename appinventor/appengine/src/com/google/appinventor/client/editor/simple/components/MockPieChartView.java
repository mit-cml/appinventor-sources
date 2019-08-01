package com.google.appinventor.client.editor.simple.components;

import org.pepstock.charba.client.IsChart;
import org.pepstock.charba.client.PieChart;
import org.pepstock.charba.client.callbacks.LegendLabelsCallback;
import org.pepstock.charba.client.callbacks.TooltipCustomCallback;
import org.pepstock.charba.client.data.PieDataset;
import org.pepstock.charba.client.items.LegendLabelItem;
import org.pepstock.charba.client.items.TooltipModel;

import java.util.ArrayList;
import java.util.List;

public class MockPieChartView extends MockChartView<PieChart> {
  // A local List of attached Data Models has to be kept to
  // construct Legend entries
  private List<MockPieChartDataModel> dataModels = new ArrayList<MockPieChartDataModel>();

  /**
   * Creates a new Mock Pie Chart View instance.
   */
  public MockPieChartView() {
    chartWidget = new PieChart();
    initializeDefaultSettings();
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    // Create a custom Legend generator for the Pie Chart
    chartWidget.getOptions().getLegend()
        .getLabels().setLabelsCallback(new LegendLabelsCallback() {
      @Override
      public LegendLabelItem[] generateLegendLabels(IsChart chart) {
        // Create a List to store Legend Items to add to the Char
        List<LegendLabelItem> legendItems = new ArrayList<LegendLabelItem>();

        // Create a List to store all the labels across all the Data Series
        List<String> dataLabels = new ArrayList<String>();

        // Iterate over all attached Data Models
        for (int i = 0; i < dataModels.size(); ++i) {
          MockPieChartDataModel model = dataModels.get(i);
          PieDataset dataset = model.dataSeries; // TODO: Use getter instead

          // Get the labels of the model and add them to the
          // data labels list storing all the labels
          List<String> labels = model.getLabels();
          dataLabels.addAll(labels);

          // Iterate over all the labels
          for (int j = 0; j < labels.size(); ++j) {
            // For each label, create a new legend item
            LegendLabelItem legendItem = new LegendLabelItem();
            legendItem.setDatasetIndex(i); // Set the dataset reference index
            legendItem.setIndex(j); // Set the entry index reference (deos not work as expected)
            legendItem.setText(labels.get(j)); // Set the text of the required label
            legendItem.setFillStyle(dataset.getBackgroundColor().get(j)); // Set the representative fill style

            // Add the legend item to the resulting Legend Items list
            legendItems.add(legendItem);
          }
        }

        // setLabels accepts varargs as a parameter. Data Labels result must
        // be cast to an array.
        chartWidget.getData().setLabels(dataLabels.toArray(new String[0]));

        // Cast result to array
        return legendItems.toArray(new LegendLabelItem[0]);
      }
    });
  }

  @Override
  public MockChartDataModel createDataModel() {
    // Create a new MockPieChartDataModel
    MockPieChartDataModel model = new MockPieChartDataModel(chartWidget.getData(), this);

    // Add the Data Model to the local List of Data Models
    dataModels.add(model);

    return model;
  }

  /**
   * Unlinks the specified Data Model from the Mock Pie Chart View.
   * @param model  model to unlink
   */
  public void removeDataModel(MockPieChartDataModel model) {
    dataModels.remove(model);
  }
}
