// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

class LineChartView: LineChartViewBase {
  override init(_ chartComponent: Chart) {
    super.init(_chartComponent)
  }
  
  // TODO: interpret what is public ChartDataModel
  override class ChartDataModel {
    createChartModel() {
      // TODO: why is self from aicompanion app
      return LineChartDataModel(data: data as! LineChartData, view: self)
    }
  }
}

