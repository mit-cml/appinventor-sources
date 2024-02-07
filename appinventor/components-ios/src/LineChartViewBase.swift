// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

open class LineChartViewBase: PointChartView {

  override init(_ chartComponent: Chart) {
    super.init(chartComponent)
    
    chart = DGCharts.LineChartView(frame: CGRect(x: 0.0, y: 0.0, width: 1.0, height: 1.0)) // do i need to add a form
    data = DGCharts.LineChartData()
    chart?.data = data
    
    initializeDefaultSettings()
  }
  
}
