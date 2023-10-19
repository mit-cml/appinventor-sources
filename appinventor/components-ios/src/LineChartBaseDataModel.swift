// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

class LinechartBaseDataModel: PointChartDataModel {
  //<V: Charts.ChartViewBase>
  init(data: Charts.LineChartData, view: Charts.ChartViewBase) {
    super.init(data: data, view: view)
    var dataset: LineChartDataSet = LineChartDataSet(Array<ChartDataEntry>)
    data.dataSets = dataset
    setDefaultStylingProperties()
  }
}
