// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import java.util.ArrayList;
import java.util.List;

import org.pepstock.charba.client.IsChart;
import org.pepstock.charba.client.PieChart;

import org.pepstock.charba.client.callbacks.LegendLabelsCallback;
import org.pepstock.charba.client.callbacks.TooltipLabelCallback;

import org.pepstock.charba.client.colors.ColorBuilder;
import org.pepstock.charba.client.colors.IsColor;

import org.pepstock.charba.client.data.PieDataset;

import org.pepstock.charba.client.events.LegendClickEvent;
import org.pepstock.charba.client.events.LegendClickEventHandler;

import org.pepstock.charba.client.items.LegendLabelItem;
import org.pepstock.charba.client.items.TooltipItem;
import org.pepstock.charba.client.items.TooltipLabelColor;

/**
 * Chart View for the Pie Chart. Responsible for the GUI of the Pie Chart.
 * @see com.google.appinventor.client.editor.simple.components.MockChartView
 */
public class MockPieChartView extends MockChartView<PieDataset, PieChart, MockPieChartView> {
  // A local List of attached Data Models has to be kept to
  // construct Legend entries
  private final List<MockPieChartDataModel> dataModels = new ArrayList<>();

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

    // Change the default handling & rendering of the Pie Chart view
    changeTooltipRendering();
    changeLegendClickHandling();
    changeLegendLabelHandling();
  }

  /**
   * Changes the way the tooltips are rendered.
   *
   * <p>The default option is to show the same x label
   * for all the Data Series, however, labels are
   * defined on a per-entry basis.
   *
   * <p>This method changes the Tooltip rendering to
   * render in "x: y" format for each entry.
   */
  private void changeTooltipRendering() {
    chartWidget.getOptions().getTooltips().getCallbacks()
        .setLabelCallback(new TooltipLabelCallback() {
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
            String x = model.getLabels().get(entryIndex);
            double y = model.getDataSeries().getData().get(entryIndex);

            // Format the label String which displays the x and the y values
            return x + ": " + y;
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
  }

  /**
   * Changes the onClick handler for the Legend labels.
   *
   * <p>This method makes it so that nothing happens upon
   * clicking a Legend label. The reason why this is
   * done is because the default click handler hides
   * values for all data series (and not just for
   * the one being clicked on)
   *
   * <p>TODO: In the future, perhaps a solution could be devised to implement
   * TODO: altered logic to hide individual values (e.g. set them to 0).
   * TODO: Alternatively, this feature could be disabled on all Charts for consistency,
   * TODO: or simply kept as is.
   */
  private void changeLegendClickHandling() {
    chartWidget.addHandler(new LegendClickEventHandler() {
      @Override
      public void onClick(LegendClickEvent event) {
        // Do nothing
      }
    }, LegendClickEvent.TYPE);
  }

  /**
   * Changes the way the Legend Labels are created.
   *
   * <p>By default, the same x label is applied to all the
   * Data Series. This method changes it so that for
   * each individual entry, a label is created and
   * constructed according to the entry.
   */
  private void changeLegendLabelHandling() {
    // Create a custom Legend generator for the Pie Chart
    chartWidget.getOptions().getLegend()
        .getLabels().setLabelsCallback(new LegendLabelsCallback() {
          @Override
          public LegendLabelItem[] generateLegendLabels(IsChart chart) {
            // Create a List to store Legend Items to add to the Char
            List<LegendLabelItem> legendItems = new ArrayList<>();

            // Create a List to store all the labels across all the Data Series
            List<String> dataLabels = new ArrayList<>();

            // Iterate over all attached Data Models
            for (int i = 0; i < dataModels.size(); ++i) {
              MockPieChartDataModel model = dataModels.get(i);
              PieDataset dataset = model.getDataSeries();

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
                // Set the representative fill style
                legendItem.setFillStyle(dataset.getBackgroundColor().get(j));

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
  public MockChartDataModel<PieDataset, MockPieChartView> createDataModel() {
    // Create a new MockPieChartDataModel
    MockPieChartDataModel model = new MockPieChartDataModel(this);

    // Add the Data Model to the local List of Data Models
    dataModels.add(model);

    return model;
  }

  /**
   * Unlinks the specified Data Model from the Mock Pie Chart View.
   *
   * @param model model to unlink
   */
  public void removeDataModel(MockPieChartDataModel model) {
    dataModels.remove(model);
  }

  /**
   * Changes the radius of the Pie Chart View.
   *
   * @param percent Percentage of the radius to fill in the Pie Chart
   */
  public void setPieRadius(int percent) {
    // Calculate & set hole radius
    int cutoutPercentage = 100 - percent;
    chartWidget.getOptions().setCutoutPercentage(cutoutPercentage);
  }
}
