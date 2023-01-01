// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

import Foundation
import Charts

public class ChartView {
  unowned let _chartComponent: Chart
  var form: Form {
    return _chartComponent.form!
  }
  var chart: Charts.ChartViewBase

  public init(_ chartComponent: Chart) {
    _chartComponent = chartComponent
  }

  public var backgroundColor: UIColor? {
    get {
      return chart.backgroundColor
    }
    set {
      chart.backgroundColor = newValue
    }
  }
}
