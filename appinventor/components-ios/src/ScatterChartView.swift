// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

open class ScatterChartView: PointChartView {

  override init(_ chartComponent: Chart) {
    super.init(chartComponent)

    let chart = DGCharts.ScatterChartView()
    self.chart = chart
    chart.renderer = ScatterWithTrendlineRenderer(chart: chart, animator: chart.chartAnimator, viewPortHandler: chart.viewPortHandler)
    data = DGCharts.ScatterChartData()
    chart.data = data

    initializeDefaultSettings()
  }

  public override func createChartModel() -> ChartDataModel {
    return ScatterChartDataModel(data: data as! ScatterChartData, view: self)
  }
}
