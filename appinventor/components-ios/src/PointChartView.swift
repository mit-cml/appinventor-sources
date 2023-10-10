// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

open class PointChartView<E: Charts.ChartDataEntry, T: Charts.BarLineScatterCandleBubbleChartDataSet, D: Charts.BarLineScatterCandleBubbleChartDataSet, C: Charts.BarLineChartViewBase, V: PointChartView<E, T, D, C, V>> : AxisChartView<E, T, D, C, V> {
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
