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
  var _color: Int32 = AIComponentKit.Color.black.int32
  var _colors: YailList<AnyObject> = []
  var _label: String?

  var dataFileColumns: Array<String> = []
  var _lineType = AIComponentKit.LineType.Linear
  var _pointshape = PointStyle.Circle
  var sheetColumns: Array<String> = ["", ""]
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
  
  @objc open var Color: Int32 {
    get {
      return _color
    }
    set {
      _color = newValue
      _chartDataModel?.setColor(argbToColor(newValue))
     refreshChart()
    }
  }
  
  @objc public func Initialize() {
    print("in Initialize")
    _initialized = true
    if dataSource != nil {
      // Source(dataSource)
    } else if let elements = _elements {
      ElementsFromPairs = elements
    }
  }
  
  @objc open var Colors: YailList<AnyObject> {
    get {
      refreshChart()
      return _colors
    }
    set {
      var resultColors: Array<Int> = []
      for i in newValue {
        let color: NSString = "\(i)" as NSString
        var colorValue: CLong = CLong(color.longLongValue)
        let two: CLong = 2
        if colorValue > Int.max {
          colorValue = colorValue + two * CLong(Int.min)
        }
        resultColors.append(colorValue as Int)
      }
      
      // set _colors property to list version of resultColors
      let resultColorsList: YailList<AnyObject> = []
      for resultColor in resultColors{
        resultColorsList.add(resultColor)
      }
      _colors = resultColorsList
      
      // convert resultColors to NSUI
      var resultColorsUI: Array<NSUIColor> = []
      for resultColor in resultColors {
        resultColorsUI.append(argbToColor(Int32(resultColor)))
      }
      _chartDataModel?.dataset!.colors = resultColorsUI
      onDataChange()
    }
  }
  
  @objc open var Label: String {
    get {
      return _label!
    }
    set {
      _label = newValue
      print("_label in Label", _label)
      _chartDataModel?.setLabel(newValue)
     onDataChange()
    }
  }
  
  func initChartData() {
    print("in initChartData")
    _chartDataModel = _container.chartView?.createChartModel()

    // set default values
    Color = AIComponentKit.Color.black.int32
    Label = ""
  }

  @objc open var LineType: LineType {
    get {
      return _lineType
    }
    set {
      _lineType = newValue

      // Only change the Line Type if the Chart Data Model is a
      // LineChartBaseDataModel (other models do not support changing the Line Type_
      if let _chartDataModel = _chartDataModel as? LineChartDataModel {
        _chartDataModel.setLineType(_lineType)
      }
      refreshChart()
    }
  }

  @objc open var `PointShape`: PointStyle {
    get {
      return _pointshape
    }
    set {
      _pointshape = newValue
      //    // Only change the Line Type if the Chart Data Model is a
      //    // LineChartBaseDataModel (other models do not support changing the Line Type_
      if let _chartDataModel = _chartDataModel as? ScatterChartDataModel {
        _chartDataModel.setPointShape(_pointshape)
      }
      refreshChart()
    }
  }

  @objc public var SpreadsheetUseHeaders = false

  @objc public var SpreadsheetXColumn = "" {
    didSet {
      sheetColumns[0] = SpreadsheetXColumn
    }
  }

  @objc public var SpreadsheetYColumn = "" {
    didSet {
      sheetColumns[1] = SpreadsheetYColumn
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

  @objc func ImportFromList(_ list: [AnyObject]) {
    _chartDataModel?.importFromList(list)
    refreshChart()
  }

  @objc func ImportFromSpreadsheet(_ spreadsheet: Spreadsheet, _ xColumn: String, _ yColumn: String, _ useHeaders: Bool) {
    let dataColumns = spreadsheet.getDataValue([xColumn, yColumn] as NSArray, useHeaders)
    if dataSource === spreadsheet {
      updateCurrentDataSourceValue(spreadsheet, nil, nil)
    }
    _chartDataModel?.importFromColumns((dataColumns as? NSArray) ?? NSArray(), useHeaders)
    refreshChart()
  }

  @objc func ImportFromTinyDB(_ tinyDB: TinyDB, _ tag: String) {
    let list = tinyDB.getDataValue(tag as NSString)
    updateCurrentDataSourceValue(tinyDB, tag, list)
    refreshChart()
  }
  
  func onDataChange(){
    // update the chart with the chart data model's current data and refresh the chart itself
    _container._chartView?.refresh(model: _chartDataModel!)
  }

  // MARK: Private Implementation

  private func updateCurrentDataSourceValue(_ source: DataSource, _ key: String?, _ newValue: AnyObject?) {
    guard source === dataSource && isKeyValid(key) else {
      return
    }
    if let source = source as? Web {
      // TODO: Implement ImportFromWeb logic
    } else if let source = source as? Spreadsheet {
      let columns = source.getColumns(sheetColumns as NSArray, SpreadsheetUseHeaders)
      lastDataSourceValue = _chartDataModel?.getTuplesFromColumns(columns, SpreadsheetUseHeaders)
    } else {
      lastDataSourceValue = newValue
    }
  }

  private func isKeyValid(_ key: String?) -> Bool {
    return key == nil || dataSourceKey == key
  }
}
