//
//  ChartDataBase.swift
//  AIComponentKit
//
//  Created by Evan Patton on 8/3/22.
//  Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.
//

import Foundation
import DGCharts

@objc class ChartDataBase: NSObject, Component, DataSourceChangeListener, ChartViewDelegate {

  var _chartDataModel: ChartDataModel<DGCharts.ChartDataEntry, DGCharts.ChartData, DGCharts.ChartViewBase, ChartView<DGCharts.ChartViewBase>>?
  var _container: Chart

  @objc public init(_ chartContainer: Chart) {
    self._container = chartContainer
    super.init()
    chartContainer.addDataComponent(self)
    initChartData()
    // do i need the executor
  }
  
  // TODO: CANT FIND WHERE COPY IS DEFINED IN JAVA CODE
  func copy(with zone: NSZone? = nil) -> Any {
    //let copy = ChartDataBase()
    //return copy
    return -1
  }
  
  func onDataSourceValueChange(_ component: DataSource, _ key: String?, _ newValue: AnyObject?) {
    
  }
  
  func onReceiveValue(_ component: RealTimeDataSource, _ key: String?, _ value: AnyObject?) {
    
  }
  
  var dispatchDelegate: HandlesEventDispatching?

  func initChartData() {
  }
}
