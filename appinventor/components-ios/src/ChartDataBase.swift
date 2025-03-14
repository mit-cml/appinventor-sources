// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

@objc open class ChartDataBase: DataCollection, Component, DataSourceChangeListener, ChartViewDelegate, ChartComponent {
  var _container: Chart {
    return container as! Chart
  }
  var _color: Int32 = AIComponentKit.Color.black.int32
  var _colors: [UIColor] = []
  var _label: String?
  
  var _dataLabelColor: Int32 = AIComponentKit.Color.black.int32

  var _lineType = AIComponentKit.LineType.Linear
  var _pointshape = PointStyle.Circle
  var sheetColumns: Array<String> = ["", ""]
  var colors: YailList<AnyObject>?
  var dataSource: DataSource?
  var lastDataSourceValue: AnyObject?

  var chartDataModel: ChartDataModel? {
    return dataModel as? ChartDataModel
  }

  public var dispatchDelegate: HandlesEventDispatching? {
    return container?.form
  }

  @objc public init(_ chartContainer: Chart) {
    super.init(chartContainer)
    chartContainer.addDataComponent(self)
    _dataLabelColor = chartContainer.form?.isDarkTheme ?? true ? AIComponentKit.Color.white.int32 : AIComponentKit.Color.black.int32
    initChartData()
    DataSourceKey("")
  }

  @objc open var Color: Int32 {
    get {
      return _color
    }
    set {
      _color = newValue
      chartDataModel?.setColor(argbToColor(newValue))
      refreshChart()
    }
  }

  @objc public func Initialize() {
    _initialized = true
    if dataSource != nil {
      // Source(dataSource)
    } else if let elements = _elements {
      ElementsFromPairs = elements
    }
    chartDataModel?.setColor(argbToColor(_color))
    chartDataModel?.setDataLabelColor(argbToColor(_dataLabelColor))
    if !_colors.isEmpty {
      chartDataModel?.setColors(_colors)
    }
  }

  @objc open var Colors: YailList<AnyObject> {
    get {
      return YailList(array: _colors.map({ colorToArgb($0) }))
    }
    set {
      var resultColors: [UIColor] = []
      for i in newValue {
        if i is SCMSymbol {
          continue
        }
        let color: NSString = "\(i)" as NSString
        var colorValue: CLong = CLong(color.longLongValue)
        let two: CLong = 2
        if colorValue > Int.max {
          colorValue = colorValue + two * CLong(Int.min)
        }
        resultColors.append(argbToColor(Int32(truncating: colorValue as NSNumber)))
      }
      _colors = resultColors
      chartDataModel?.dataset?.colors = _colors
      refreshChart()
    }
  }

  @objc open var Label: String {
    get {
      return _label!
    }
    set {
      _label = newValue
      chartDataModel?.setLabel(newValue)
     onDataChange()
    }
  }
  
  @objc open var DataLabelColor: Int32 {
    get {
      return _dataLabelColor
    }
    set {
      _dataLabelColor = newValue
      chartDataModel?.dataset?.valueTextColor = argbToColor(newValue)
      refreshChart()
    }
  }

  func initChartData() {
    dataModel = _container.chartView?.createChartModel()

    // set default values
    chartDataModel?.setColor(argbToColor(_color))
    chartDataModel?.setLabel(_label ?? "")
    chartDataModel?.setDataLabelColor(argbToColor(_dataLabelColor))
    chartDataModel?.view.chart?.delegate = self
  }

  @objc open var LineType: LineType {
    get {
      return _lineType
    }
    set {
      _lineType = newValue

      // Only change the Line Type if the Chart Data Model is a
      // LineChartBasechartDataModel (other models do not support changing the Line Type_
      if let chartDataModel = chartDataModel as? LineChartDataModel {
        chartDataModel.setLineType(_lineType)
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
      //    // LineChartBasechartDataModel (other models do not support changing the Line Type_
      if let chartDataModel = chartDataModel as? ScatterChartDataModel {
        chartDataModel.setPointShape(_pointshape)
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
  public func copy(with zone: NSZone? = nil) -> Any {
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
    _container.chartView?.refresh(model: chartDataModel!)
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
      self.chartDataModel?.setElements(elements)
      self.onDataChange()
    }
  }

  // Removes all the entries from the Data Series
  @objc func Clear(){
    self.chartDataModel?.clearEntries()
    // refresh chart with new data
    self.refreshChart()
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
      return self.chartDataModel?.findEntriesByCriterion(x, EntryCriterion.XValue)
    }
    // Undefined behavior: return empty list
    return []*/
    let group = DispatchGroup()
    group.enter()
    var holder: YailList<AnyObject> = []
    // avoid deadlocks by not using .main queue here
    DispatchQueue.global(qos: .default).async {
      holder = (self.chartDataModel?.findEntriesByCriterion(x, EntryCriterion.XValue))!
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
      return self.chartDataModel?.findEntriesByCriterion(y, EntryCriterion.YValue)
    }
     return holder
     */

    let group = DispatchGroup()
    group.enter()
    var holder: YailList<AnyObject> = []
    // avoid deadlocks by not using .main queue here
    DispatchQueue.global(qos: .default).async {
      holder = (self.chartDataModel?.findEntriesByCriterion(y, EntryCriterion.YValue))!
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
      holder = (self.chartDataModel?.getEntriesAsTuples())!
      group.leave()
    }
    group.wait()
    return holder

    /*
     Original:
     DispatchQueue.main.sync {
      return self.chartDataModel?.getEntriesAsTuples()
    }
    // Undefined behavior: return empty list
    return []*/
  }

  @objc func ImportFromList(_ list: [AnyObject]) {
    chartDataModel?.importFromList(list)
    refreshChart()
  }

  @objc func ImportFromSpreadsheet(_ spreadsheet: Spreadsheet, _ xColumn: String, _ yColumn: String, _ useHeaders: Bool) {
    let dataColumns = spreadsheet.getDataValue([xColumn, yColumn] as NSArray, useHeaders)
    if dataSource === spreadsheet {
      updateCurrentDataSourceValue(spreadsheet, nil, nil)
    }
    chartDataModel?.importFromColumns(dataColumns as NSArray, useHeaders)
    refreshChart()
  }

  @objc func ImportFromTinyDB(_ tinyDB: TinyDB, _ tag: String) {
    let list = tinyDB.getDataValue(tag as NSString)
    updateCurrentDataSourceValue(tinyDB, tag, list as NSArray)
    refreshChart()
  }

  // MARK: Events

  @objc open func EntryClick(_ x: AnyObject, _ y: Double) {
    EventDispatcher.dispatchEvent(of: self, called: "EntryClick", arguments: x, y as NSNumber)
    _container.EntryClick(self, x, y)
  }

  func onDataChange(){
    // update the chart with the chart data model's current data and refresh the chart itself
    _container._chartView?.refresh(model: chartDataModel!)
    for listener in listeners {
      if let listener = listener as? DataSourceChangeListener {
        listener.onDataSourceValueChange(self, nil, nil)
      }
    }
  }

  public func chartValueSelected(_ chartView: ChartViewBase, entry: ChartDataEntry, highlight: Highlight) {
    if let entry = entry as? PieChartDataEntry {
      EntryClick((entry.label ?? "") as NSString, entry.y)
    } else {
      EntryClick(entry.x as NSNumber, entry.y)
    }
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
      lastDataSourceValue = chartDataModel?.getTuplesFromColumns(columns, SpreadsheetUseHeaders)
    } else {
      lastDataSourceValue = newValue
    }
  }

  private func isKeyValid(_ key: String?) -> Bool {
    return key == nil || dataSourceKey == key
  }
}
