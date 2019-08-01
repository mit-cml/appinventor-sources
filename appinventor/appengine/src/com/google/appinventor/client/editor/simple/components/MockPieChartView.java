package com.google.appinventor.client.editor.simple.components;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.pepstock.charba.client.IsChart;
import org.pepstock.charba.client.PieChart;
import org.pepstock.charba.client.callbacks.LegendCallback;
import org.pepstock.charba.client.callbacks.LegendLabelsCallback;
import org.pepstock.charba.client.data.Dataset;
import org.pepstock.charba.client.data.PieDataset;
import org.pepstock.charba.client.items.DatasetMetaItem;
import org.pepstock.charba.client.items.LegendLabelItem;

import java.util.ArrayList;
import java.util.List;

public class MockPieChartView extends MockChartView<PieChart> {
  private List<MockPieChartDataModel> dataModels = new ArrayList<MockPieChartDataModel>();

  public MockPieChartView() {
    chartWidget = new PieChart();
    initializeDefaultSettings();
  }

  @Override
  protected void initializeDefaultSettings() {
    super.initializeDefaultSettings();

    chartWidget.getOptions().getLegend().getLabels().setLabelsCallback(new LegendLabelsCallback() {
      @Override
      public LegendLabelItem[] generateLegendLabels(IsChart chart) {
        List<LegendLabelItem> legendItems = new ArrayList<LegendLabelItem>();
        List<String> dataLabels = new ArrayList<String>();

        for (int i = 0; i < dataModels.size(); ++i) {
          MockPieChartDataModel model = dataModels.get(i);
          List<String> labels = model.getLabels();
          dataLabels.addAll(labels);

          PieDataset dataset = model.dataSeries;

          for (int j = 0; j < labels.size(); ++j) {
            LegendLabelItem legendItem = new LegendLabelItem();
            legendItem.setDatasetIndex(i);
            legendItem.setIndex(j);
            legendItem.setText(labels.get(j));
            legendItem.setFillStyle(dataset.getBackgroundColor().get(j));

            legendItems.add(legendItem);
          }
        }

        chartWidget.getData().setLabels(dataLabels.toArray(new String[0]));

        return legendItems.toArray(new LegendLabelItem[0]);
      }
    });
  }

  @Override
  public MockChartDataModel createDataModel() {
    MockPieChartDataModel model = new MockPieChartDataModel(chartWidget.getData(), this);
    dataModels.add(model);
    return model;
  }

  public void removeDataModel(MockPieChartDataModel model) {
    dataModels.remove(model);
  }
}
