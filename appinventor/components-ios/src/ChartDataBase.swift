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
  var _colors: YailList<AnyObject> = []
  var _label: String?

  var dataFileColumns: Array<String> = []
  var useSheetHeaders: Bool?
  var sheetColumns: Array<String> = []
  var webColumns: Array<String> = []
  var dataSourceKey: String?
  var colors: YailList<AnyObject>?
  var dataSource: DataSource?
  var lastDataSourceValue: AnyObject?
  var _elements: String? // elements designer property
  var _initialized = false // keep track whether the screen has already been intialized
  var _tick : Int = 0

  @objc public init(_ chartContainer: Chart) {
    self._container = chartContainer
    super.init()
    chartContainer.addDataComponent(self)
    initChartData()
    DataSourceKey("")
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
  
  @objc public func Initialize() {
    _initialized = true
    if dataSource != nil {
      // Source(dataSource)
    } else{
      ElementsFromPairs = _elements!

    }
  }
  
  func Colors() -> YailList<AnyObject> {
    refreshChart()
    return _colors
  }
  
  func Colors(_ colors: YailList<AnyObject>) {
    var resultColors: Array<Int> = []
    for i in colors {
      var color: NSString = "\(i)" as NSString
      var colorValue: CLong = CLong(color.longLongValue)
      var two: CLong = 2
      if colorValue > Int.max {
        colorValue = colorValue + two * CLong(Int.min)
      }
      print("colorvalue", colorValue)
      resultColors.append(colorValue)
    }
    _colors = resultColors as! YailList<AnyObject>
    // TODO: how should i change resultColors to UICOLOR
    _chartDataModel?.setColors((resultColors as! [UIColor]))
    refreshChart()
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
  
  func initChartData() {
    _chartDataModel = _container.chartView?.createChartModel()

    // set default values
    Color = uiColorFromHex(rgbValue: 0xFF000000)
    Label = ""
    
    //TODO: do i need gesture recognizers
    //_chartDataModel?.view.chart?.gestureRecognizers =
    //_chartDataModel?.view.chart?.gest
  }
  
  func LineType(_ type: LineType) {
    // Only change the Line Type if the Chart Data Model is a
    // LineChartBaseDataModel (other models do not support changing the Line Type_
    if let _chartDataModel = _chartDataModel as? LineChartDataModel {
      _chartDataModel.setLineType(type)
    }
  }
  
  // TODO: CANT FIND WHERE COPY IS DEFINED IN JAVA CODE
  func copy(with zone: NSZone? = nil) -> Any {
    //let copy = ChartDataBase()
    //return copy
    return -1
  }
  
  func SpreadsheetUseHeaders(_ useHeaders: Bool) {
    useSheetHeaders = useHeaders
  }
  
  func onDataSourceValueChange(_ component: DataSource, _ key: String?, _ newValue: AnyObject?) {
    
  }
  
  func onReceiveValue(_ component: RealTimeDataSource, _ key: String?, _ value: AnyObject?) {
    
  }
  
  func DataSourceKey(_ key: String) {
    dataSourceKey = key
  }
  
  var dispatchDelegate: HandlesEventDispatching?


  
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
    _container.chartView?.refresh(model: _chartDataModel!)
  }
  
  /**
   * Comma separated list of Elements to use for the data series. Values are formatted
   * as follows: x1,y1,x2,y2,x3,y3. Values are taken in pairs, and an entry is formed
   * from the x and y values.
   *
   * @param elements Comma-separated values of Chart entries alternating between x and y values.
   */
  
  @objc open var ElementsFromPairs: String {
    get {
      return ""
    }
    set(elements) {
      _elements = elements
      if elements.isEmpty || elements == "" || !_initialized {
        return
      }
      DispatchQueue.main.async {
        self._chartDataModel?.setElements(elements)
        self.refreshChart()
      }
    }
  }
  
  // Removes all the entries from the Data Series
  @objc func Clear(){
    DispatchQueue.main.async {
      self._chartDataModel?.clearEntries()
      // refresh chart with new data
      self.refreshChart()
    }
  }
  
  // datafile and web not implemented in swift yet
  @objc func ChangeDataSource(_ source: DataSource, _ keyValue: String){
    fatalError("Data sources are not implemented in IOS")
  }
  
  // datafile and web not implemented in swift yet
  @objc func RemoveDataSource(){
    fatalError("Data sources are not implemented in IOS")
  }
  
  // Returns a List of entries with x values matching the specified x value. A single entry is represented as a List of values of the entry
  @objc func GetEntriesWithXValue(_ x: String) -> YailList<AnyObject>{
    /*DispatchQueue.main.sync {
      return self._chartDataModel?.findEntriesByCriterion(x, EntryCriterion.XValue)
    }
    // Undefined behavior: return empty list
    return []*/
    let group = DispatchGroup()
    group.enter()
    var holder: YailList<AnyObject> = []
    // avoid deadlocks by not using .main queue here
    DispatchQueue.global(qos: .default).async {
      holder = (self._chartDataModel?.findEntriesByCriterion(x, EntryCriterion.XValue))!
      print("holder", holder)
      group.leave()
    }
    group.wait()
    // Undefined behavior: return empty list
    return holder
  }
  
  // Returns a List of entries with y values matching the specified y value. A single entry is represented as a List of values of the entry
  @objc func GetEntriesWithYValue(_ y: String) -> YailList<AnyObject>{
    /*DispatchQueue.main.sync {
      // use Y Value as criterion to filter entries
      return self._chartDataModel?.findEntriesByCriterion(y, EntryCriterion.YValue)
    }
     return holder
     */
    
    let group = DispatchGroup()
    group.enter()
    var holder: YailList<AnyObject> = []
    // avoid deadlocks by not using .main queue here
    DispatchQueue.global(qos: .default).async {
      holder = (self._chartDataModel?.findEntriesByCriterion(y, EntryCriterion.YValue))!
      print("holder", holder)
      group.leave()
    }
    group.wait()
    // Undefined behavior: return empty list
    return holder
  }
  
  // Returns all the entries of the Data Series. A single entry is represented as a List of values of the entry
  @objc func GetAllEntries() -> YailList<AnyObject>{
    let group = DispatchGroup()
    group.enter()
    var holder: YailList<AnyObject> = []
    // avoid deadlocks by not using .main queue here
    DispatchQueue.global(qos: .default).async {
      holder = (self._chartDataModel?.getEntriesAsTuples())!
      print("holder", holder)
      group.leave()
    }
    group.wait()
    return holder
    
    /*
     Original:
     DispatchQueue.main.sync {
      return self._chartDataModel?.getEntriesAsTuples()
    }
    // Undefined behavior: return empty list
    return []*/
  }
  
  @objc func ImportFromTinyDB(){
    fatalError("TinyDB is not implemented in IOS")
  }
  
}
