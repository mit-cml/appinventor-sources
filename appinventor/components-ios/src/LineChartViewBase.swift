// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

open class LineChartViewBase<E: Charts.ChartDataEntry, D: Charts.ChartData, V: Charts.ChartViewBase> {
  let data: D
  let dataset: Charts.ChartDataSet
  let view: V
  var _entries = [E]()
  var maximumTimeEntries: Int32 = 200
  
  init(data: D, dataset: Charts.ChartDataSet, view: V) {
    self.data = data
    self.dataset = dataset
    self.view = view
  }
  
}
