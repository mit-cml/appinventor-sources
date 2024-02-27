//
//  BarChartView.swift
//  AIComponentKit
//
//  Created by David Kim on 2/14/24.
//  Copyright © 2024 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation
import DGCharts

class BarChartView: AxisChartView {
  // Constants for starting value and spacing for groups of bars
  private let startXValue: Double = 0.0
  private let groupSpace: Double = 0.08
  private var barSpace: Double = 0.0
  private var barWidth: Double = 0.3

  override init(_ chartComponent: Chart) {
    super.init(chartComponent)

    chart = DGCharts.BarChartView()
    data = DGCharts.BarChartData()
    chart?.data = data

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

//  override func initializeDefaultSettings() {
//    super.initializeDefaultSettings()
//
//    chart.frame = self.bounds
//    chart?.autoresizingMask = [.flexibleWidth, .flexibleHeight]
//
//    chart!.xAxis.granularity = 1.0
//  }

  func refresh(model: BarChartDataModel, entries: Array<DGCharts.ChartDataEntry>) {
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
      let dataset : ChartDataSet = model.dataset ?? BarChartDataSet()
      dataset.replaceEntries(entries)
      dataset.drawValuesEnabled = true
      self.chart?.data?.notifyDataChanged()
      self.chart?.notifyDataSetChanged()
      self.chart?.setNeedsDisplay()
    }
  }

//  private func regroupBars() {
//    let dataSetCount = chart?.data?.dataSetCount ?? 0
//
//    if dataSetCount > 1 {
//      chart.groupBars(fromX: startXValue, groupSpace: groupSpace, barSpace: barSpace)
//
//      let maxEntries = chart?.data?.dataSets.max(by: { $0.entryCount < $1.entryCount })?.entryCount ?? 0
//      chart!.xAxis.axisMinimum = startXValue
//      let groupWidth = chart.data?.groupWidth(groupSpace: groupSpace, barSpace: barSpace) ?? 0
//      chart!.xAxis.axisMaximum = startXValue + groupWidth * Double(maxEntries)
//    }
//  }
}
