// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

open class PointChartView: AxisChartView {
  
  override init(_ chartComponent: Chart) {
    super.init(_chartComponent)
  }

  public override func initializeDefaultSettings() {
    super.initializeDefaultSettings()
    
    // TODO: learn how to setLayoutParams
    // Since the Chart is stored in a RelativeLayout, settings are needed to fill the Layout
    var temp: GroupView = UILayoutGuide(
    chart.layoutGuides = UILayoutGuide(
  }
      
  public func getView() -> (UIView) {
    return chart
  }
}
