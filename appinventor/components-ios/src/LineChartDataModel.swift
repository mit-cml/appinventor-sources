// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

class LineChartDataModel: LineChartBaseDataModel<LineChartView> {
  init(data: DGCharts.LineChartData, view: DGCharts.LineChartView) {
    super.init(data: data, view: view)
  }
}

