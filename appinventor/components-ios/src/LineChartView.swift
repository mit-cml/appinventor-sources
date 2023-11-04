// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

open class LineChartView: LineChartViewBase {
  override init(_ chartComponent: Chart) {
    super.init(chartComponent)
  }
  
  // TODO: fix self
  // Cannot convert value of type 'AIComponentKit.LineChartView' to expected argument type 'Charts.LineChartView'
  public override func createChartModel() -> ChartDataModel<ChartDataEntry, ChartData, ChartViewBase> {
    return LineChartDataModel(data: data as! LineChartData, view: self) as! ChartDataModel<ChartDataEntry, ChartData, ChartViewBase>
  }

}

