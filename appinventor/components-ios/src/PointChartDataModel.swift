// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

open class PointChartDataModel<E: Charts.ChartDataEntry, D: BarLineScatterCandleBubbleChartData, V: ChartViewBase>: ChartData2DModel<E, D, V> {
  override init(data: D, view: V) {
    super.init(data: data, view: view)
  }
}
