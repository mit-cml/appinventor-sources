// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

open class LineChartViewBase: PointChartView {

  override init(_ chartComponent: Chart) {
    super.init(_chartComponent)
    
    super.chart = LineChartView(Form)
    
    super.data = LineChartData()
    super.chart.data = super.data
    
    initializeDefaultSettings()
  }
  
}
