// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

class BarChartView: AxisChartView {
  // Constants for starting value and spacing for groups of bars
  private let startXValue: Double = 0.0
  private let groupSpace: Double = 0.08
  private var barSpace: Double = 0.0
  private var barWidth: Double = 0.3
  private var barCharts = [DGCharts.BarChartView]()

  override init(_ chartComponent: Chart) {
    super.init(chartComponent)

    let chart = DGCharts.BarChartView()
    self.chart = chart
    data = DGCharts.BarChartData()
    chart.data = data

    initializeDefaultSettings()
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func getView() -> UIView {
    return chart!
  }

  public override func createChartModel() -> ChartDataModel {
    let model = BarChartDataModel(data: data as! BarChartData, view: self) as ChartDataModel

    // Recalculate Bar Space and Width
    self.recalculateBarSpaceAndWidth()

    return model
  }

  private func recalculateBarSpaceAndWidth() {
    let dataSetCount = chart?.data?.dataSetCount ?? 0

    if dataSetCount > 1 {
      let x = (1.0 - groupSpace) / Double(dataSetCount)

      self.barSpace = x * 0.1
      self.barWidth = x * 0.9
      //chart?.data?.barWidth = barWidth
    }

    if dataSetCount == 2 {
      chart!.xAxis.centerAxisLabelsEnabled = true
    }
  }

  override func initializeDefaultSettings() {
    super.initializeDefaultSettings()

    //    chart.frame = self.bounds
    //    chart?.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    //
    //    chart!.xAxis.granularity = 1.0
  }

  func refresh(model: BarChartDataModel, entries: Array<DGCharts.ChartDataEntry>) {
    let dataset : ChartDataSet = model.dataset ?? BarChartDataSet()
    dataset.replaceEntries(entries)
    dataset.drawValuesEnabled = true
    self.chart?.data?.notifyDataChanged()
    self.chart?.notifyDataSetChanged()
    self.chart?.setNeedsDisplay()
  }

  // MARK: iOS Helpers

  public var barChart: DGCharts.BarChartView? {
    return chart as? DGCharts.BarChartView
  }

  private func regroupBars() {
    guard let chartData = chart?.data, chartData.dataSetCount > 1 else {
      // If there is only a single data series in the bar chart, no action is taken.
      return
    }

    // Group bar chart bars with the current parameters
    barChart?.groupBars(fromX: startXValue, groupSpace: groupSpace, barSpace: barSpace)

    // Determine the maximum number of entries between bar data sets.
    // This value is needed to apply the maximum value of the axis.
    let maxEntries = chartData.dataSets.reduce(0) { max($0, $1.entryCount) }

    // Update the x axis to start from the start x value
    chart?.xAxis.axisMinimum = startXValue

    // Set the maximum value for the x axis based on maximum entries and the group
    // width of the grouped bars. The calculation is based directly on the example
    // presented in the Charts library example activities.
    chart?.xAxis.axisMaximum = startXValue + barWidth * Double(maxEntries)
  }

}
