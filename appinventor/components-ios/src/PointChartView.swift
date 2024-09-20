// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2023 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import DGCharts

open class PointChartView: AxisChartView {

  override init(_ chartComponent: Chart) {
    super.init(chartComponent)
  }

  public override func initializeDefaultSettings() {
    super.initializeDefaultSettings()
    
    // TODO: learn how to setLayoutParams(), until now don't write
    // Since the Chart is stored in a RelativeLayout, settings are needed to fill the Layout
    //var temp: View
    //chart.layoutGuides = UILayoutGuide()
  }
      
  override public func getView() -> (UIView) {
    return chart!
  }
}
