// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import DGCharts

@objc public class ChartType: NSObject, OptionList {
  @objc public static let Line = ChartType(0)
  @objc public static let Scatter = ChartType(1)
  @objc public static let Area = ChartType(2)
  @objc public static let Bar = ChartType(3)
  @objc public static let Pie = ChartType(4)

  private static let LOOKUP: [Int32:ChartType] = [
    0: ChartType.Line,
    1: ChartType.Scatter,
    2: ChartType.Area,
    3: ChartType.Bar,
    4: ChartType.Pie
  ]

  let value: Int32

  @objc private init(_ value: Int32) {
    self.value = value
  }

  @objc class func fromUnderlyingValue(_ value: Int32) -> ChartType? {
    return LOOKUP[value]
  }

  @objc public func toUnderlyingValue() -> AnyObject {
    return value as AnyObject
  }
}

@objc open class Chart : ViewComponent, ComponentContainer, LifecycleDelegate, AbstractMethodsForViewComponent {
  public func getChildren() -> [Component] {
    return []
  }

  private var _initialized = false
  var _view = UIView()
  var _chartView: ChartView?
  var _type = ChartType.Line
  var _description = ""
  var _backgroundColor: UIColor
  var _pieRadius: Int32 = 100
  var _legendEnabled = true
  var _gridEnabled = true
  var _labels = [String]()
  var _dataComponents: Array<ChartComponent> = []
  
  var _axesTextColor: UIColor
  private var darkMode = false

  @objc public override init(_ parent: ComponentContainer) {
    XFromZero = false
    YFromZero = false
    darkMode = parent.form?.isDarkTheme ?? false
    _backgroundColor = darkMode ? UIColor.black : UIColor.white
    _axesTextColor = darkMode ? UIColor.white : UIColor.black
    super.init(parent)
    setDelegate(self)
    parent.add(self)
    Width = 176
    Height = 144
  }

  @objc open func Initialize() {
    guard !_initialized else {
      return
    }

    _initialized = true
    self.Type = self.Type
  }

  @objc open override var view: UIView {
    return _view
  }

  open var container: ComponentContainer? {
    return _container
  }

  open func add(_ component: ViewComponent) {
    // Not implemented
  }

  open func setChildWidth(of component: ViewComponent, to width: Int32) {
    // Not implemented
  }

  open func setChildHeight(of component: ViewComponent, to height: Int32) {
    // Not implemented
  }

  open func isVisible(component: ViewComponent) -> Bool {
    return true
  }

  open func setVisible(component: ViewComponent, to visibility: Bool) {

  }

  // MARK: Chart Properties

  @objc open var BackgroundColor: Int32 {
    get {
      return colorToArgb(_backgroundColor)
    }
    set {
      _backgroundColor = argbToColor(newValue)
      _chartView?.backgroundColor = _backgroundColor
    }
  }
  
  @objc open var AxesTextColor: Int32 {
    get {
      return colorToArgb(_axesTextColor)
    }
    set {
      if newValue == Color.default.int32 {
        _axesTextColor = darkMode ? UIColor.white : UIColor.black
      }
      _axesTextColor = argbToColor(newValue)
      if let chartView = _chartView?.chart as? BarLineChartViewBase {
        chartView.xAxisRenderer.axis.labelTextColor = _axesTextColor
        chartView.leftYAxisRenderer.axis.labelTextColor = _axesTextColor
      }
    }
  }

  @objc open var Description: String {
    get {
      return _description
    }
    set {
      _description = newValue
      _chartView?.chart?.chartDescription.text = _description
    }
  }

  @objc open var GridEnabled: Bool {
    get {
      return _gridEnabled
    }
    set {
      _gridEnabled = newValue
      if let chartView = _chartView as? AxisChartView {
        chartView.setGridEnabled(enabled: newValue)
      }
      /*if let chartView = _chartView as? BarLineChartViewBase {
        chartView.xAxis.drawGridLinesEnabled = newValue
        chartView.leftAxis.drawGridLinesEnabled = newValue
      }*/
    }
  }

  @objc open var Labels: [String] {
    get {
      return _labels
    }
    set {
      _labels = newValue
      if let chart = _chartView as? AxisChartView {
        chart.axisLabels = _labels
      }
    }
  }

  @objc open var LabelsFromString: String {
    get {
        return ""
    }
    set{
      Labels = newValue.split(",") as [String]
    }
  }

  @objc open var LegendEnabled: Bool {
    get {
      return _legendEnabled
    }
    set {
      _legendEnabled = newValue
      _chartView?.chart?.legend.enabled = _legendEnabled
    }
  }

  @objc open var PieRadius: Int32 {
    get {
      return _pieRadius
    }
    set {
      _pieRadius = newValue
      if let chartView = _chartView as? PieChartView {
        chartView.pieRadius = newValue
      }
    }
  }

  @objc open var `Type`: ChartType {
    get {
      return _type
    }
    set {
      _type = newValue

      guard _initialized else {
        return
      }

      let newChartView = createChartView(for: newValue)
      _chartView?.chart?.removeFromSuperview()
      _chartView = newChartView

      let chart = newChartView.getView()

      chart.translatesAutoresizingMaskIntoConstraints = false
      view.addSubview(chart)
      NSLayoutConstraint.activate([
        view.topAnchor.constraint(equalTo: chart.topAnchor),
        view.leadingAnchor.constraint(equalTo: chart.leadingAnchor),
        view.widthAnchor.constraint(equalTo: chart.widthAnchor),
        view.heightAnchor.constraint(equalTo: chart.heightAnchor)
      ])

      reinitializeChart()
    }
  }

  @objc open var XFromZero: Bool {
    didSet {
      if let chartView = (_chartView as AnyObject) as? AxisChartView {
        chartView.setXMinimum(zero: XFromZero)
      }
    }

  }

  @objc open var YFromZero: Bool {
    didSet {
      if let chartView = (_chartView as AnyObject) as? AxisChartView {
        chartView.setYMinimum(zero: YFromZero)
      }
    }
  }

  // MARK: Chart events

  @objc open func EntryClick(_ series: Component, _ x: AnyObject, _ y: Double) {
    EventDispatcher.dispatchEvent(of: self, called: "EntryClick",
                                  arguments: series, x, y as AnyObject)
  }

  @objc open func SetDomain(_ minimum: Double, _ maximum: Double) {
    self.XFromZero = minimum == 0.0
  }

  @objc open func SetRange(_ minimum: Double, _ maximum: Double) {
    self.YFromZero = minimum == 0.0
  }

  // MARK: Private Implementation


  public var chartView: ChartView? {
    return _chartView
  }

  private func createChartModel() -> ChartDataModel? {
    return _chartView?.createChartModel()
  }

  public func refresh() {
    _chartView?.chart?.data?.notifyDataChanged()
    _chartView?.chart?.notifyDataSetChanged()
  }

  private func createChartView(for type: ChartType) -> ChartView {
    switch type {
    case .Line:
      return LineChartView(self) as ChartView
    case .Scatter:
      return ScatterChartView(self)
    case .Area:
      return AreaChartView(self)
    case .Bar:
      return BarChartView(self) as ChartView
    case .Pie:
      return PieChartView(self)
    default:
      fatalError("Invalid chart type")
    }
  }

  private func reinitializeChart() {
    for dataComponent in _dataComponents {
      dataComponent.initChartData()
    }
    BackgroundColor = colorToArgb(_backgroundColor)
    Description = _description
    GridEnabled = _gridEnabled
    Labels = _labels
    LegendEnabled = _legendEnabled
    PieRadius = _pieRadius
    
    AxesTextColor = colorToArgb(_axesTextColor)
  }

  func addDataComponent(_ dataComponent: ChartComponent) {
    _dataComponents.append(dataComponent)
  }
}
