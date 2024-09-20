// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

open class LineChartView: LineChartViewBase {
  override init(_ chartComponent: Chart) {
    super.init(chartComponent)
  }
  
  public override func createChartModel() -> ChartDataModel {
    return LineChartDataModel(data: data as! LineChartData, view: self) as ChartDataModel
  }

}
