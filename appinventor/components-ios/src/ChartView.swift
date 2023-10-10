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
  
  var data: Charts.ChartData
  
  // var uiHandler: Handler
  

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
  
  public var cDescription: String? {
    get {
      return chart.chartDescription.text
    }
    set {
      chart.chartDescription.text = newValue
    }
  }
  
  public var legendEnabled: Bool? {
    get {
      return chart.legend.enabled
    }
    set {
      chart.legend.enabled = newValue!
    }
  }
  
  public var createChartModel : ChartDataModel = ChartDataModel(data: <#ChartData#>, dataset: <#ChartDataSet#>, view: <#ChartViewBase#>)
  
  public func initializeDefaultSettings() {
    // Center the Legend
    chart.legend.horizontalAlignment = Legend.HorizontalAlignment.center
    chart.legend.wordWrapEnabled = true
  }
  
  /*
   Refreshes the Chart View to react to styling changes
   */
  // TODO: CHECK IF THIS IS RIGHT
  //<E: Charts.ChartDataEntry, D: Charts.ChartData, V: Charts.ChartViewBase>
  public func refresh() {
    chart.reloadInputViews()
  }
  
  /*
  public func refresh(model: ChartDataModel<Charts.ChartDataEntry, Charts.ChartData, Charts.ChartViewBase>) {
    var refreshTask: chart.ref
    chart.reloadInputViews()
  }
   */
}
