// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

class LineChartDataModel: LineChartBaseDataModel {
  public init(data: DGCharts.LineChartData, view: LineChartView) {
    super.init(data: data, view: view)
  }

  public override init(data: DGCharts.LineChartData, view: LineChartViewBase, dataset: DGCharts.ChartDataSet) {
    super.init(data: data, view: view, dataset: dataset)
  }
}

