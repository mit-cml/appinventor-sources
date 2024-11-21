// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

open class PieChartView: ChartView {

  private let rootView = UIView()
  private var pieCharts = [DGCharts.PieChartView]()
  private var pieHoleRadius: Int32 = 0
  private var _legendEntries = [LegendEntry]()
  private var bottomOffset: CGFloat = 0

  override init(_ chartComponent: Chart) {
    super.init(chartComponent)
    rootView.translatesAutoresizingMaskIntoConstraints = false
    let chart = DGCharts.PieChartView()
    chart.translatesAutoresizingMaskIntoConstraints = false
    chart.holeRadiusPercent = 0.0
    chart.transparentCircleRadiusPercent = 0.0
    chart.drawEntryLabelsEnabled = false
    self.chart = chart
  }

  public override func initializeDefaultSettings() {
    super.initializeDefaultSettings()

    guard let chart = chart else {
      // Not initialized yet
      return
    }

    chart.legend.drawInside = true
    chart.legend.setCustom(entries: _legendEntries)
  }

  public override func getView() -> UIView {
    return rootView
  }

  public override func createChartModel() -> ChartDataModel {
    let pieChart = createPieChartRing()
    pieChart.holeRadiusPercent = 0.0
    pieChart.drawEntryLabelsEnabled = false
    pieChart.transparentCircleRadiusPercent = 0.0
    let data = PieChartData()
    data.setDrawValues(false)
    data.dataSet?.drawValuesEnabled = false
    return PieChartDataModel(data: data, view: self, chart: pieChart)
  }

  public func resizePieRings() {
    var lastWidth: Int32 = 0
    var lastHeight: Int32 = 0

    let reductionFactor = (0.75 + Double(pieHoleRadius) / 100.0) / Double(pieCharts.count)

    let radius = Double(100 - pieHoleRadius)
    let newHoleRadius = 100.0 - radius * reductionFactor

    var count = 0
    for pieChart in pieCharts {
      let lastChart = count == pieCharts.count - 1

      changePieChartRadius(pieChart, newHoleRadius, lastChart)

      if count > 0 {
        let scalingFactor = newHoleRadius / 100.0
        lastWidth = Int32(Double(lastWidth) * scalingFactor)
        lastHeight = Int32(Double(lastHeight) * scalingFactor)
        changePieChartSize(pieChart, lastWidth, lastHeight)
      } else {
        lastHeight = Int32(pieChart.frame.height)
        lastWidth = Int32(pieChart.frame.width)
      }

      pieChart.setNeedsDisplay()
      count += 1
    }
  }

  public override func refresh(model: ChartDataModel, entries: Array<ChartDataEntry>) {
    guard let dataset = model.dataset as? PieChartDataSet else {
      return
    }

    dataset.replaceEntries(entries)

    chart?.legend.setCustom(entries: legendEntries)

    pieCharts.forEach { pieChart in
      if pieChart == self.chart || (pieChart.data?.dataSets.first as? ChartDataSet) == dataset {
        pieChart.data?.notifyDataChanged()
        pieChart.notifyDataSetChanged()
      }

      updatePieChartRingOffset(pieChart)

      pieChart.setNeedsDisplay()
    }
  }

  public func addLegendEntry(_ entry: LegendEntry) {
    _legendEntries.append(entry)
  }

  public func removeLegendEntry(_ entry: LegendEntry) {
    _legendEntries.removeAll { e in
      e == entry
    }
  }

  public func removeLegendEntries(_ entries: [LegendEntry]) {
    _legendEntries.removeAll { entry in
      entries.contains(entry)
    }
  }

  public var legendEntries: [LegendEntry] {
    return _legendEntries
  }

  public var pieRadius: Int32 {
    get {
      return 100 - pieHoleRadius
    }
    set {
      let percent = min(100, max(0, newValue))
      pieHoleRadius = 100 - percent
      resizePieRings()
    }
  }

  // MARK: iOS Helpers

  public var pieChart: DGCharts.PieChartView? {
    return chart as? DGCharts.PieChartView
  }

  // MARK: Private Implementation

  private func createPieChartRing() -> DGCharts.PieChartView {
    let pieChart: DGCharts.PieChartView

    if pieCharts.isEmpty {
      pieChart = chart as! DGCharts.PieChartView
    } else {
      pieChart = DGCharts.PieChartView()
      pieChart.chartDescription.enabled = true
      pieChart.legend.enabled = false
      pieChart.holeRadiusPercent = 0.0
    }

    setPieChartProperties(pieChart)

    pieCharts.append(pieChart)
    rootView.addSubview(pieChart)
    pieChart.widthAnchor.constraint(equalTo: rootView.widthAnchor).isActive = true
    pieChart.heightAnchor.constraint(equalTo: rootView.heightAnchor).isActive = true
    pieChart.centerXAnchor.constraint(equalTo: rootView.centerXAnchor).isActive = true
    pieChart.centerYAnchor.constraint(equalTo: rootView.centerYAnchor).isActive = true

    return pieChart
  }

  private func setPieChartProperties(_ chart: DGCharts.PieChartView) {
    chart.drawEntryLabelsEnabled = true
  }

  private func changePieChartRadius(_ pieChart: DGCharts.PieChartView, _ newHoleRadius: Double, _ lastChart: Bool) {
    if lastChart {
      if pieHoleRadius == 0 {
        pieChart.drawHoleEnabled = false
      } else {
        let delta = newHoleRadius - Double(pieHoleRadius)
        let setRadius = Double(pieHoleRadius) * (1.0 + delta / 100.0)
        pieChart.transparentCircleRadiusPercent = setRadius / 100.0
        pieChart.holeRadiusPercent = setRadius / 100.0
      }
    } else {
      pieChart.transparentCircleRadiusPercent = newHoleRadius / 100.0
      pieChart.holeRadiusPercent = newHoleRadius / 100.0
      pieChart.drawHoleEnabled = true
    }
  }

  private func changePieChartSize(_ pieChart: DGCharts.PieChartView, _ width: Int32, _ height: Int32) {
    pieChart.widthAnchor.constraint(equalToConstant: CGFloat(width)).isActive = true
    pieChart.heightAnchor.constraint(equalToConstant: CGFloat(height)).isActive = true
  }

  private func updatePieChartRingOffset(_ pieChart: DGCharts.PieChartView) {
    guard let chart = chart else {
      // Not initialized yet
      return
    }

    if chart == pieChart {
      let dpNeededHeight = chart.legend.neededHeight
      bottomOffset = dpNeededHeight / 2.5
      bottomOffset = min(25.0, bottomOffset)
    }

    pieChart.extraBottomOffset = bottomOffset
    pieChart.notifyDataSetChanged()
  }
}
