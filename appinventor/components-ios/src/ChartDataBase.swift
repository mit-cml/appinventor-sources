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

  var _chartDataModel: ChartDataModel?
  var _container: Chart
  var _color: UIColor?
  var _label: String?

  

  @objc public init(_ chartContainer: Chart) {
    self._container = chartContainer
    super.init()
    chartContainer.addDataComponent(self)
    initChartData()
    // do i need the executor
  }
  
  @objc open var Color: UIColor {
    get {
      return _color!
    }
    set {
      _color = newValue
      _chartDataModel?.setColor(_color!)
     refreshChart()
    }
  }
  
  @objc open var Label: String {
    get {
      return _label!
    }
    set {
      _label = newValue
      _chartDataModel?.setLabel(newValue)
     refreshChart()
    }
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
    _chartDataModel = _container.chartView?.createChartModel()
    
    // set default values
    Color = uiColorFromHex(rgbValue: 0xFF000000)
    Label = ""
    
    //do i need gesture recognizers
    //_chartDataModel?.view.chart?.gestureRecognizers =
    //_chartDataModel?.view.chart?.gest
  }
  
  func uiColorFromHex(rgbValue: Int) -> UIColor {
      
      // &  binary AND operator to zero out other color values
      // >>  bitwise right shift operator
      // Divide by 0xFF because UIColor takes CGFloats between 0.0 and 1.0
      
      let red =   CGFloat((rgbValue & 0xFF0000) >> 16) / 0xFF
      let green = CGFloat((rgbValue & 0x00FF00) >> 8) / 0xFF
      let blue =  CGFloat(rgbValue & 0x0000FF) / 0xFF
      let alpha = CGFloat(1.0)
      
      return UIColor(red: red, green: green, blue: blue, alpha: alpha)
  }
  
  func refreshChart() {
    _container.chartView?.refresh()
  }
}
