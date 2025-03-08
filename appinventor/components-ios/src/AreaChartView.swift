// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2024 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

class AreaChartView: LineChartViewBase {
  override init(_ chartComponent: Chart) {
    super.init(chartComponent)
  }

  override func createChartModel() -> ChartDataModel {
    return AreaChartDataModel(data: self.data as! DGCharts.LineChartData, view: self)
  }
}
