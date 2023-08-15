// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

class LineChartDataModel: LineChartBaseDataModel<LineChartView> {
  init(data: Charts.LineChartData, view: Charts.LineChartView) {
    super.init(data: data, view: view)
  }
}

