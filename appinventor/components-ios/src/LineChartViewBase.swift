// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

open class LineChartViewBase: PointChartView {

  override init(_ chartComponent: Chart) {
    super.init(chartComponent)

    let chart = DGCharts.LineChartView()
    self.chart = chart
    chart.renderer = LineWithTrendlineRenderer(chart: chart, animator: chart.chartAnimator, viewPortHandler: chart.viewPortHandler)
    data = DGCharts.LineChartData()
    chart.data = data

    initializeDefaultSettings()
  }

}
