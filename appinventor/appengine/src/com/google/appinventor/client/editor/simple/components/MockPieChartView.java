package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.output.OdeLog;
import org.pepstock.charba.client.IsChart;
import org.pepstock.charba.client.PieChart;
import org.pepstock.charba.client.callbacks.LegendLabelsCallback;
import org.pepstock.charba.client.callbacks.TooltipBodyCallback;
import org.pepstock.charba.client.callbacks.TooltipCustomCallback;
import org.pepstock.charba.client.callbacks.TooltipFilterCallback;
import org.pepstock.charba.client.callbacks.TooltipItemSortCallback;
import org.pepstock.charba.client.callbacks.TooltipLabelCallback;
import org.pepstock.charba.client.callbacks.TooltipTitleCallback;
import org.pepstock.charba.client.colors.ColorBuilder;
import org.pepstock.charba.client.colors.IsColor;
import org.pepstock.charba.client.data.PieDataset;
import org.pepstock.charba.client.enums.InteractionMode;
import org.pepstock.charba.client.events.LegendClickEvent;
import org.pepstock.charba.client.events.LegendClickEventHandler;
import org.pepstock.charba.client.items.LegendLabelItem;
import org.pepstock.charba.client.items.TooltipBodyItem;
import org.pepstock.charba.client.items.TooltipItem;
import org.pepstock.charba.client.items.TooltipLabelColor;
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

    // TODO: Set tooltips to display X value as well (needs additional logic with labels)
    // Can be accomplished by:
    // chartWidget.getOptions().getTooltips().getCallbacks().setLabelCallback();
    chartWidget.getOptions().getTooltips().getCallbacks().setLabelCallback(new TooltipLabelCallback() {
      @Override
      public String onBeforeLabel(IsChart chart, TooltipItem item) {
        return "";
      }

      @Override
      public String onLabel(IsChart chart, TooltipItem item) {
        // Get the Data Series & entry indexes
        int seriesIndex = item.getDatasetIndex();
        int entryIndex = item.getIndex();

        // Get the corresponding MockPieChartDataModel
        MockPieChartDataModel model = dataModels.get(seriesIndex);

        // The x value can be found in the labels List, while the
        // y value can be found in the Data List of the Data Series
        String xValue = model.getLabels().get(entryIndex);
        double yValue = model.dataSeries.getData().get(entryIndex);

        // Format the label String which displays the x and the y values
        return xValue + ": " + yValue;
      }

      @Override
      public TooltipLabelColor onLabelColor(IsChart chart, TooltipItem item) {
        TooltipLabelColor color = new TooltipLabelColor();

        // Get the Data Series & entry indexes
        int seriesIndex = item.getDatasetIndex();
        int entryIndex = item.getIndex();

        // Get the corresponding Data Series & retrieve the color of
        // the corresponding entry
        PieDataset dataSeries = dataModels.get(seriesIndex).dataSeries;
        IsColor colorValue = dataSeries.getBackgroundColor().get(entryIndex);

        // Set the color value of the constructed TooltipLabelColor object and
        // return it
        color.setBackgroundColor(colorValue);
        return color;
      }

      @Override
      public IsColor onLabelTextColor(IsChart chart, TooltipItem item) {
        // Label text should always be white
        return ColorBuilder.build(255, 255, 255);
      }

      @Override
      public String onAfterLabel(IsChart chart, TooltipItem item) {
        return "";
      }
    });

    // Disable the Legend Click event which hides a data series (by setting
    // an empty event handler). The reason this is done is because the
    // click handler hides values across all the Datasets.
    // TODO: In the future, perhaps a solution could be devised to implement
    // TODO: altered logic to hide individual values. Alternatively, this
    // TODO: feature could be disabled on all Charts for consistency, or simply kept as is.
    chartWidget.addHandler(new LegendClickEventHandler() {
      @Override
      public void onClick(LegendClickEvent event) {
        // Do nothing
      }
    }, LegendClickEvent.TYPE);

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

  public void setPieRadius(int percent) {
    int cutoutPercentage = 100 - percent;
    chartWidget.getOptions().setCutoutPercentage(cutoutPercentage);
  }
}
