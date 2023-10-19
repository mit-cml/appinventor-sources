// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

class LineChartBaseDataModel: PointChartDataModel {
  init(data: DGCharts.LineChartData, view: DGCharts.LineChartView) {
    super.init(data: data, view: view)
    var dataset = LineChartDataSet()
  }
  public override func addEntryFromTuple(_ tuple: YailList<AnyObject>) {
    var entry: ChartDataEntry = getEntryFromTuple(tuple)
    if entry != nil { // TODO: how to compare it to nil
      
    }
  }
}

